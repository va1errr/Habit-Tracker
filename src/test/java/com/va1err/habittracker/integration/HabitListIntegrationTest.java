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
class HabitListIntegrationTest {

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
    void listHabits_shouldReturnOnlyActiveHabitsWithCorrectCompletionStatus() throws Exception {
        Habit habit1 = new Habit("Reading", null, true);
        Habit habit2 = new Habit("Writing", null, true);
        Habit habit3 = new Habit("Sleeping", null, false);

        HabitCompletion habitCompletion = new HabitCompletion(habit1, TODAY);

        habitRepository.saveAndFlush(habit1);
        habitRepository.saveAndFlush(habit2);
        habitRepository.saveAndFlush(habit3);

        habitCompletionRepository.saveAndFlush(habitCompletion);

        mockMvc.perform(get("/api/v1/habits"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.name == 'Reading')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Reading')].completedToday").value(true))
                .andExpect(jsonPath("$[?(@.name == 'Writing')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Writing')].completedToday").value(false))
                .andExpect(jsonPath("$[?(@.name == 'Sleeping')]").doesNotExist());
    }

}
