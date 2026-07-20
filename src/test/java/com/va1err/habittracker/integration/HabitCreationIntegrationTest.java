package com.va1err.habittracker.integration;

import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HabitCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HabitRepository habitRepository;

    @BeforeEach
    void cleanDatabase() {
        habitRepository.deleteAll();
    }

    @Test
    void createHabit_shouldCreateHabit() throws Exception {
        mockMvc.perform(post("/api/v1/habits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "  Read books "
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Read books"))
                .andExpect(jsonPath("$.description").hasJsonPath())
                .andExpect(jsonPath("$.description").value(nullValue()))
                .andExpect(jsonPath("$.active").value(true));

        List<Habit> habits = habitRepository.findAll();

        assertEquals(1, habits.size());
        assertNotNull(habits.getFirst().getId());
        assertEquals("Read books", habits.getFirst().getName());
        assertNull(habits.getFirst().getDescription());
        assertTrue(habits.getFirst().isActive());
    }

}
