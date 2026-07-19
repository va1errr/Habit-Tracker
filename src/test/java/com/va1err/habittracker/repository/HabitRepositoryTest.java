package com.va1err.habittracker.repository;

import com.va1err.habittracker.entity.Habit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
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
    
}
