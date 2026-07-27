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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import({
        PostgresTestContainerConfig.class,
        TestClockConfig.class
})
public class HabitCompletionCancellationIntegrationTest {

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
    void cancelHabitCompletion_shouldDeleteTodayCompletionAndMarkHabitAsNotCompleted() throws Exception {
        Habit savedHabit = habitRepository.saveAndFlush(new Habit(
                "Reading",
                null,
                true
        ));

        habitCompletionRepository.saveAndFlush(new HabitCompletion(
                savedHabit,
                TODAY
        ));

        mockMvc.perform(delete("/api/v1/habits/" + savedHabit.getId() + "/completions"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertEquals(0, habitCompletionRepository.count());

        mockMvc.perform(get("/api/v1/habits/" + savedHabit.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.completedToday").value(false));
    }

    @Test
    void cancelHabitCompletion_shouldReturnConflictWhenCancelledTwice() throws Exception {
        Habit savedHabit = habitRepository.saveAndFlush(new Habit(
                "Reading",
                null,
                true
        ));

        habitCompletionRepository.saveAndFlush(new HabitCompletion(
                savedHabit,
                TODAY
        ));

        mockMvc.perform(delete("/api/v1/habits/" + savedHabit.getId() + "/completions"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertEquals(0, habitCompletionRepository.count());

        mockMvc.perform(delete("/api/v1/habits/" + savedHabit.getId() + "/completions"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Habit is not completed today"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        assertEquals(0, habitCompletionRepository.count());
    }

}
