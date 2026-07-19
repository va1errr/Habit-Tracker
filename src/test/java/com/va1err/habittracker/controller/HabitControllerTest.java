package com.va1err.habittracker.controller;

import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.service.HabitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HabitController.class)
class HabitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HabitService habitService;

    @Test
    void createHabit_shouldReturnCreatedHabit() throws Exception {
        Habit createdHabit = mock(Habit.class);

        when(createdHabit.getId()).thenReturn(1L);
        when(createdHabit.getName()).thenReturn("Read books");
        when(createdHabit.getDescription()).thenReturn("Reading improves memory");
        when(createdHabit.isActive()).thenReturn(true);

        when(habitService.createHabit("Read books", "Reading improves memory")).thenReturn(createdHabit);

        mockMvc.perform(post("/api/v1/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Read books",
                                    "description": "Reading improves memory"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Read books"))
                .andExpect(jsonPath("$.description").value("Reading improves memory"))
                .andExpect(jsonPath("$.active").value(true));

        verify(habitService).createHabit("Read books", "Reading improves memory");
    }

    @Test
    void createHabit_shouldReturnBadRequestWhenNameIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "description": "Reading improves memory"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitService);
    }

    @Test
    void createHabit_shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "  "
                                    "description": "Reading improves memory"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitService);
    }

}
