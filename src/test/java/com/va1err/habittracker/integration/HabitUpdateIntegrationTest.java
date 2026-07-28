package com.va1err.habittracker.integration;

import com.va1err.habittracker.config.PostgresTestContainerConfig;
import com.va1err.habittracker.entity.Habit;
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

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(PostgresTestContainerConfig.class)
class HabitUpdateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void cleanDatabase() {
        habitRepository.deleteAll();
    }

    @Test
    void updateHabit_shouldUpdateActiveHabitAndPersistChanges() throws Exception {
        Habit habit = new Habit(
                "Read books",
                "Reading improves memory",
                true
        );

        Habit savedHabit = habitRepository.saveAndFlush(habit);

        mockMvc.perform(patch("/api/v1/habits/" + savedHabit.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  Write poems ",
                                  "description": "Writing improves motorics"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedHabit.getId()))
                .andExpect(jsonPath("$.name").value("Write poems"))
                .andExpect(jsonPath("$.description").value("Writing improves motorics"))
                .andExpect(jsonPath("$.active").value(savedHabit.isActive()));

        entityManager.clear();

        Habit updatedHabit = habitRepository.findById(savedHabit.getId())
                .orElseThrow();

        assertEquals(savedHabit.getId(), updatedHabit.getId());
        assertEquals("Write poems", updatedHabit.getName());
        assertEquals("Writing improves motorics", updatedHabit.getDescription());
        assertEquals(savedHabit.isActive(), updatedHabit.isActive());
    }

    @Test
    void updateHabit_shouldPreserveDescriptionWhenDescriptionIsAbsent() throws Exception {
        Habit habit = new Habit(
                "Read books",
                "Reading improves memory",
                true
        );

        Habit savedHabit = habitRepository.saveAndFlush(habit);

        mockMvc.perform(patch("/api/v1/habits/" + savedHabit.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  Write poems "
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedHabit.getId()))
                .andExpect(jsonPath("$.name").value("Write poems"))
                .andExpect(jsonPath("$.description").value("Reading improves memory"))
                .andExpect(jsonPath("$.active").value(savedHabit.isActive()));

        entityManager.clear();

        Habit updatedHabit = habitRepository.findById(savedHabit.getId())
                .orElseThrow();

        assertEquals(savedHabit.getId(), updatedHabit.getId());
        assertEquals("Write poems", updatedHabit.getName());
        assertEquals("Reading improves memory", updatedHabit.getDescription());
        assertEquals(savedHabit.isActive(), updatedHabit.isActive());
    }

    @Test
    void updateHabit_shouldRemoveDescriptionWhenExplicitlyNull() throws Exception {
        Habit habit = new Habit(
                "Read books",
                "Reading improves memory",
                true
        );

        Habit savedHabit = habitRepository.saveAndFlush(habit);

        mockMvc.perform(patch("/api/v1/habits/" + savedHabit.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Read books",
                                  "description": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedHabit.getId()))
                .andExpect(jsonPath("$.name").value("Read books"))
                .andExpect(jsonPath("$.description").value(nullValue()))
                .andExpect(jsonPath("$.active").value(savedHabit.isActive()));

        entityManager.clear();

        Habit updatedHabit = habitRepository.findById(savedHabit.getId())
                .orElseThrow();

        assertEquals(savedHabit.getId(), updatedHabit.getId());
        assertEquals("Read books", updatedHabit.getName());
        assertNull(updatedHabit.getDescription());
        assertEquals(savedHabit.isActive(), updatedHabit.isActive());
    }

    @Test
    void updateHabit_shouldReturnConflictWhenArchivedHabitHasSameNameIgnoringCase() throws Exception {
        Habit activeHabit = new Habit(
                "Read books",
                "Reading improves memory",
                true
        );
        Habit archivedHabit = new Habit(
                "Write poems",
                null,
                false
        );

        Habit savedActiveHabit = habitRepository.saveAndFlush(activeHabit);
        habitRepository.saveAndFlush(archivedHabit);

        mockMvc.perform(patch("/api/v1/habits/" + savedActiveHabit.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "wRiTe PoEmS"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Habit name already exists!"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        entityManager.clear();

        Habit updatedHabit = habitRepository.findById(savedActiveHabit.getId())
                .orElseThrow();

        assertEquals("Read books", updatedHabit.getName());
        assertEquals("Reading improves memory", updatedHabit.getDescription());
    }

    @Test
    void updateHabit_shouldReturnNotFoundWhenHabitIsArchived() throws Exception {
        Habit archivedHabit = new Habit(
                "Read books",
                "Reading improves memory",
                false
        );

        Habit savedArchivedHabit = habitRepository.saveAndFlush(archivedHabit);

        mockMvc.perform(patch("/api/v1/habits/" + savedArchivedHabit.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Write poems"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Habit not found"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        entityManager.clear();

        Habit updatedHabit = habitRepository.findById(savedArchivedHabit.getId())
                .orElseThrow();

        assertEquals("Read books", updatedHabit.getName());
        assertEquals("Reading improves memory", updatedHabit.getDescription());
        assertFalse(updatedHabit.isActive());
    }

}
