package com.va1err.habittracker.integration;

import com.va1err.habittracker.config.PostgresTestContainerConfig;
import com.va1err.habittracker.config.TestClockConfig;
import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.entity.HabitCompletion;
import com.va1err.habittracker.repository.HabitCompletionRepository;
import com.va1err.habittracker.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import({
        TestClockConfig.class,
        PostgresTestContainerConfig.class
})
class HabitDetailsIntegrationTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 21);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitCompletionRepository habitCompletionRepository;

    @BeforeEach
    void cleanDatabase() {
        habitCompletionRepository.deleteAll();
        habitRepository.deleteAll();
    }

    @Test
    void getById_shouldReturnActiveHabitWithCompletedTodayTrue() throws Exception {
        Habit habit = new Habit(
                "Read books",
                "Reading improves memory",
                true
        );

        Habit savedHabit = habitRepository.saveAndFlush(habit);

        HabitCompletion habitCompletion = new HabitCompletion(
                savedHabit,
                TODAY
        );

        habitCompletionRepository.saveAndFlush(habitCompletion);

        mockMvc.perform(get("/api/v1/habits/" + savedHabit.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedHabit.getId()))
                .andExpect(jsonPath("$.name").value("Read books"))
                .andExpect(jsonPath("$.description").value("Reading improves memory"))
                .andExpect(jsonPath("$.completedToday").value(true));
    }

    @Test
    void getById_shouldReturnNotFoundWhenHabitIsArchived() throws Exception {
        Habit habit = new Habit(
                "Read books",
                "Reading improves memory",
                false
        );

        Habit savedHabit = habitRepository.saveAndFlush(habit);

        mockMvc.perform(get("/api/v1/habits/" + savedHabit.getId()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Habit not found"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());
    }

}
