package com.va1err.habittracker.integration;

import com.va1err.habittracker.config.PostgresTestContainerConfig;
import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.entity.HabitCompletion;
import com.va1err.habittracker.repository.HabitCompletionRepository;
import com.va1err.habittracker.repository.HabitRepository;
import jakarta.persistence.EntityManager;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(PostgresTestContainerConfig.class)
class HabitArchiveIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitCompletionRepository habitCompletionRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void cleanDatabase() {
        habitCompletionRepository.deleteAll();
        habitRepository.deleteAll();
    }

    @Test
    void archiveHabit_shouldArchiveActiveHabitAndPersistChanges() throws Exception {
        Habit habit = new Habit("Reading", null, true);
        Habit savedHabit = habitRepository.saveAndFlush(habit);

        mockMvc.perform(delete("/api/v1/habits/" + savedHabit.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        entityManager.flush();
        entityManager.clear();

        Habit archivedHabit = habitRepository.findById(savedHabit.getId())
                .orElseThrow();

        assertFalse(archivedHabit.isActive());
    }

    @Test
    void archiveHabit_shouldRemoveArchivedHabitFromActiveHabitList() throws Exception {
        Habit habit1 = new Habit("Reading", null, true);
        Habit habit2 = new Habit("Writing", null, true);

        Habit savedHabit1 = habitRepository.saveAndFlush(habit1);
        Habit savedHabit2 = habitRepository.saveAndFlush(habit2);

        mockMvc.perform(delete("/api/v1/habits/" + savedHabit1.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/v1/habits"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[?(@.id == " + savedHabit1.getId() + ")]").doesNotExist())
                .andExpect(jsonPath("$[?(@.id == " + savedHabit2.getId() + ")]").exists());
    }

    @Test
    void archiveHabit_shouldReturnNotFoundWhenArchivedTwice() throws Exception {
        Habit habit = new Habit("Reading", null, true);
        Habit savedHabit = habitRepository.saveAndFlush(habit);

        mockMvc.perform(delete("/api/v1/habits/" + savedHabit.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(delete("/api/v1/habits/" + savedHabit.getId()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Habit not found"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void archiveHabit_shouldPreserveHabitCompletions() throws Exception {
        Habit habit = new Habit("Reading", null, true);
        Habit savedHabit = habitRepository.saveAndFlush(habit);

        LocalDate date1 = LocalDate.of(2026, 8, 14);
        LocalDate date2 = LocalDate.of(2026, 8, 15);

        HabitCompletion habitCompletion1 = new HabitCompletion(savedHabit, date1);
        HabitCompletion habitCompletion2 = new HabitCompletion(savedHabit, date2);

        habitCompletionRepository.saveAndFlush(habitCompletion1);
        habitCompletionRepository.saveAndFlush(habitCompletion2);

        mockMvc.perform(delete("/api/v1/habits/" + savedHabit.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        entityManager.flush();
        entityManager.clear();

        assertTrue(habitCompletionRepository.existsByHabitIdAndCompletionDate(savedHabit.getId(), date1));
        assertTrue(habitCompletionRepository.existsByHabitIdAndCompletionDate(savedHabit.getId(), date2));
    }

    @Test
    void archiveHabit_shouldKeepArchivedHabitNameReserved() throws Exception {
        Habit habit = new Habit("Reading", null, true);
        Habit savedHabit = habitRepository.saveAndFlush(habit);

        mockMvc.perform(delete("/api/v1/habits/" + savedHabit.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/v1/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "rEaDiNg"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Habit name already exists!"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        assertEquals(1, habitRepository.count());
    }

    @Test
    void archiveHabit_shouldReturnNotFoundWhenHabitDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/v1/habits/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Habit not found"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());
    }

}
