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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import({
        PostgresTestContainerConfig.class,
        TestClockConfig.class
})
class HabitCompletionIntegrationTest {

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
    void completeHabit_shouldPersistCompletionAndMarkHabitCompletedToday() throws Exception {
        Habit savedHabit = habitRepository.saveAndFlush(new Habit(
                "Reading",
                null,
                true
        ));

        mockMvc.perform(post("/api/v1/habits/" + savedHabit.getId() + "/completions"))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.habitId").value(savedHabit.getId()))
                .andExpect(jsonPath("$.completionDate").value(TODAY.toString()));

        List<HabitCompletion> habitCompletions = habitCompletionRepository.findAll();

        assertEquals(1, habitCompletions.size());
        assertNotNull(habitCompletions.getFirst().getId());
        assertEquals(savedHabit.getId(), habitCompletions.getFirst().getHabit().getId());
        assertEquals(TODAY, habitCompletions.getFirst().getCompletionDate());

        mockMvc.perform(get("/api/v1/habits/" + savedHabit.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.completedToday").value(true));
    }

    @Test
    void completeHabit_shouldReturnConflictWhenCompletedTwiceToday() throws Exception {
        Habit savedHabit = habitRepository.saveAndFlush(new Habit(
                "Reading",
                null,
                true
        ));

        mockMvc.perform(post("/api/v1/habits/" + savedHabit.getId() + "/completions"))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/v1/habits/" + savedHabit.getId() + "/completions"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        List<HabitCompletion> habitCompletions = habitCompletionRepository.findAll();

        assertEquals(1, habitCompletions.size());
    }

}
