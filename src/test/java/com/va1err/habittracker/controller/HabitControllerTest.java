package com.va1err.habittracker.controller;

import com.va1err.habittracker.dto.HabitListItemResponse;
import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.exception.DuplicateHabitNameException;
import com.va1err.habittracker.service.HabitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("must not be blank"));

        verifyNoInteractions(habitService);
    }

    @Test
    void createHabit_shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "  ",
                                    "description": "Reading improves memory"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("must not be blank"));

        verifyNoInteractions(habitService);
    }

    @Test
    void createHabit_shouldReturnConflictWhenNameAlreadyExists() throws Exception {
        when(habitService.createHabit("Reading", null)).thenThrow(new DuplicateHabitNameException());

        mockMvc.perform(post("/api/v1/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Reading"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Habit name already exists!"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        verify(habitService).createHabit("Reading", null);
    }

    @Test
    void listHabits_shouldReturnHabitList() throws Exception {
        HabitListItemResponse habitListItemResponse = new HabitListItemResponse(
                1L,
                "Read books",
                "Reading improves memory",
                true
        );

        when(habitService.listHabits()).thenReturn(List.of(habitListItemResponse));

        mockMvc.perform(get("/api/v1/habits"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Read books"))
                .andExpect(jsonPath("$[0].description").value("Reading improves memory"))
                .andExpect(jsonPath("$[0].completedToday").value(true));

        verify(habitService).listHabits();
    }

    @Test
    void listHabits_shouldReturnEmptyArrayWhenNoHabits() throws Exception {
        when(habitService.listHabits()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/habits"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(habitService).listHabits();
    }

    @Test
    void listHabits_shouldReturnNullDescriptionAndCompletedTodayFalse() throws Exception {
        HabitListItemResponse habitListItemResponse = new HabitListItemResponse(
                1L,
                "Read books",
                null,
                false
        );

        when(habitService.listHabits()).thenReturn(List.of(habitListItemResponse));

        mockMvc.perform(get("/api/v1/habits"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Read books"))
                .andExpect(jsonPath("$[0].description").value(nullValue()))
                .andExpect(jsonPath("$[0].completedToday").value(false));

        verify(habitService).listHabits();
    }

}
