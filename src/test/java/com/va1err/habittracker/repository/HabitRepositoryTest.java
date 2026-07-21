package com.va1err.habittracker.repository;

import com.va1err.habittracker.config.PostgresTestContainerConfig;
import com.va1err.habittracker.entity.Habit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(PostgresTestContainerConfig.class)
class HabitRepositoryTest {

    @Autowired
    private HabitRepository habitRepository;

    @Test
    void save_shouldGenerateId() {
        Habit habit = new Habit("Reading", null, true);
        Habit savedHabit = habitRepository.saveAndFlush(habit);

        assertNotNull(savedHabit.getId());
    }

    @Test
    void existsByNameIgnoreCase_shouldIgnoreCase() {
        Habit habit = new Habit("Reading", null, true);
        habitRepository.saveAndFlush(habit);

        assertTrue(habitRepository.existsByNameIgnoreCase("rEaDiNg"));
    }

    @Test
    void save_shouldRejectDuplicateNameIgnoreCase() {
        Habit existingHabit = new Habit("Reading", null, true);
        habitRepository.saveAndFlush(existingHabit);

        Habit duplicateHabit = new Habit("reading", null, true);
        assertThrows(DataIntegrityViolationException.class, () -> habitRepository.saveAndFlush(duplicateHabit));
    }

    @Test
    void existsByNameIgnoreCase_shouldReturnFalseWhenNameDoesNotExist() {
        assertFalse(habitRepository.existsByNameIgnoreCase("Reading"));
    }

    @Test
    void findAllByActiveTrue_shouldReturnOnlyActiveHabits() {
        Habit activeHabit = new Habit("Reading", null, true);
        Habit archiveHabit = new Habit("Writing", null, false);

        habitRepository.saveAndFlush(activeHabit);
        habitRepository.saveAndFlush(archiveHabit);

        List<Habit> result = habitRepository.findAllByActiveTrue();

        assertTrue(result.contains(activeHabit));
        assertFalse(result.contains(archiveHabit));
        assertTrue(result.stream().allMatch(Habit::isActive));
    }

    @Test
    void findByIdAndActiveTrue_shouldReturnEmptyWhenHabitIsArchived() {
        Habit archiveHabit = new Habit("Writing", null, false);

        Habit savedHabit = habitRepository.saveAndFlush(archiveHabit);

        Optional<Habit> result = habitRepository.findByIdAndActiveTrue(savedHabit.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdAndActiveTrue_shouldReturnActiveHabit() {
        Habit activeHabit = new Habit("Reading", null, true);

        Habit savedHabit = habitRepository.saveAndFlush(activeHabit);

        Optional<Habit> result = habitRepository.findByIdAndActiveTrue(savedHabit.getId());

        assertTrue(result.isPresent());
        assertEquals(savedHabit, result.get());
    }

}
