package com.va1err.habittracker.service;

import com.va1err.habittracker.dto.HabitDetailsResponse;
import com.va1err.habittracker.dto.HabitListItemResponse;
import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.entity.HabitCompletion;
import com.va1err.habittracker.exception.DuplicateHabitNameException;
import com.va1err.habittracker.exception.HabitNotFoundException;
import com.va1err.habittracker.exception.InvalidHabitNameException;
import com.va1err.habittracker.repository.HabitCompletionRepository;
import com.va1err.habittracker.repository.HabitRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HabitService {
    private final HabitRepository habitRepository;

    private final HabitCompletionRepository habitCompletionRepository;

    private final Clock clock;

    public HabitService(
            HabitRepository habitRepository,
            HabitCompletionRepository habitCompletionRepository,
            Clock clock
    ) {
        this.habitRepository = habitRepository;
        this.habitCompletionRepository = habitCompletionRepository;
        this.clock = clock;
    }

    @Transactional
    public Habit createHabit(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new InvalidHabitNameException();
        }

        String normalizedName = name.strip();

        if (habitRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateHabitNameException();
        }

        Habit habit = new Habit(normalizedName, description, true);

        try {
            return habitRepository.saveAndFlush(habit);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateHabitNameException();
        }
    }

    @Transactional(readOnly = true)
    public List<HabitListItemResponse> listHabits() {
        List<Habit> activeHabits = habitRepository.findAllByActiveTrue();

        LocalDate today = LocalDate.now(clock);

        List<HabitCompletion> habitCompletions =
                habitCompletionRepository.findAllByCompletionDate(today);

        Set<Long> completedHabitIds = habitCompletions.stream()
                .map(habitCompletion -> habitCompletion.getHabit().getId())
                .collect(Collectors.toSet());

        return activeHabits.stream().map(habit ->
                new HabitListItemResponse(
                        habit.getId(),
                        habit.getName(),
                        habit.getDescription(),
                        completedHabitIds.contains(habit.getId())
                )).toList();
    }

    @Transactional(readOnly = true)
    public HabitDetailsResponse getById(Long id) {
        Habit habit = habitRepository.findByIdAndActiveTrue(id)
                .orElseThrow(HabitNotFoundException::new);

        return new HabitDetailsResponse(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habitCompletionRepository.existsByHabitIdAndCompletionDate(id, LocalDate.now(clock))
                );
    }

}
