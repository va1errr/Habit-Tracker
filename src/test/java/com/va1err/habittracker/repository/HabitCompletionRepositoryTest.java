package com.va1err.habittracker.repository;

import com.va1err.habittracker.config.PostgresTestContainerConfig;
import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.entity.HabitCompletion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(PostgresTestContainerConfig.class)
class HabitCompletionRepositoryTest {

    @Autowired
    private HabitCompletionRepository habitCompletionRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Test
    void save_shouldGenerateId() {
        Habit habit = new Habit("Reading", null, true);

        habitRepository.saveAndFlush(habit);

        LocalDate date = LocalDate.of(2026, 7, 21);
        HabitCompletion habitCompletion = new HabitCompletion(habit, date);

        HabitCompletion savedHabitCompletion = habitCompletionRepository.saveAndFlush(habitCompletion);

        assertNotNull(savedHabitCompletion.getId());
    }

    @Test
    void save_shouldRejectDuplicateCompletionForSameHabitAndDate() {
        Habit habit = new Habit("Reading", null, true);

        habitRepository.saveAndFlush(habit);

        LocalDate date = LocalDate.of(2026, 7, 21);

        HabitCompletion habitCompletion1 = new HabitCompletion(habit, date);
        HabitCompletion habitCompletion2 = new HabitCompletion(habit, date);

        habitCompletionRepository.saveAndFlush(habitCompletion1);

        assertThrows(DataIntegrityViolationException.class,
                () -> habitCompletionRepository.saveAndFlush(habitCompletion2));
    }

    @Test
    void save_shouldAllowCompletionsForDifferentHabitsOnSameDate() {
        Habit habit1 = new Habit("Reading", null, true);
        Habit habit2 = new Habit("Writing", null, true);

        habitRepository.saveAndFlush(habit1);
        habitRepository.saveAndFlush(habit2);

        LocalDate date = LocalDate.of(2026, 7, 21);

        HabitCompletion habitCompletion1 = new HabitCompletion(habit1, date);
        HabitCompletion habitCompletion2 = new HabitCompletion(habit2, date);

        habitCompletionRepository.saveAndFlush(habitCompletion1);

        assertDoesNotThrow(() -> habitCompletionRepository.saveAndFlush(habitCompletion2));
    }

    @Test
    void save_shouldAllowSameHabitOnDifferentDates() {
        Habit habit = new Habit("Reading", null, true);

        habitRepository.saveAndFlush(habit);

        LocalDate date1 = LocalDate.of(2026, 7, 20);
        LocalDate date2 = LocalDate.of(2026, 7, 21);

        HabitCompletion habitCompletion1 = new HabitCompletion(habit, date1);
        HabitCompletion habitCompletion2 = new HabitCompletion(habit, date2);

        habitCompletionRepository.saveAndFlush(habitCompletion1);
        assertDoesNotThrow(() -> habitCompletionRepository.saveAndFlush(habitCompletion2));
    }

    @Test
    void findAllByCompletionDate_shouldReturnOnlyCompletionsForSpecifiedDate() {
        Habit habit = new Habit("Reading", null, true);

        habitRepository.saveAndFlush(habit);

        LocalDate date1 = LocalDate.of(2026, 7, 20);
        LocalDate date2 = LocalDate.of(2026, 7, 21);

        HabitCompletion habitCompletion1 = new HabitCompletion(habit, date1);
        HabitCompletion habitCompletion2 = new HabitCompletion(habit, date2);

        habitCompletionRepository.saveAndFlush(habitCompletion1);
        habitCompletionRepository.saveAndFlush(habitCompletion2);

        assertEquals(List.of(habitCompletion1),
                habitCompletionRepository.findAllByCompletionDate(date1));
    }

}
