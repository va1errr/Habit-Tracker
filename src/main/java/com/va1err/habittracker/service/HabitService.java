package com.va1err.habittracker.service;

import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.exception.InvalidHabitNameException;
import com.va1err.habittracker.repository.HabitRepository;

public class HabitService {

    private final HabitRepository habitRepository;

    public HabitService(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public Habit createHabit(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new InvalidHabitNameException();
        }

        String normalizedName = name.strip();

        Habit habit = new Habit(normalizedName, description, true);
        return habitRepository.save(habit);
    }

}
