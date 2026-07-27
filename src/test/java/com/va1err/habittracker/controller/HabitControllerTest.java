package com.va1err.habittracker.controller;

import com.va1err.habittracker.dto.HabitCompletionResponse;
import com.va1err.habittracker.dto.HabitDetailsResponse;
import com.va1err.habittracker.dto.HabitListItemResponse;
import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.exception.DuplicateHabitNameException;
import com.va1err.habittracker.exception.HabitAlreadyCompletedTodayException;
import com.va1err.habittracker.exception.HabitNotCompletedTodayException;
import com.va1err.habittracker.exception.HabitNotFoundException;
import com.va1err.habittracker.service.HabitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HabitController.class)
class HabitControllerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 21);

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

    @Test
    void getById_shouldReturnHabitDetails() throws Exception {
        HabitDetailsResponse habitDetailsResponse = new HabitDetailsResponse(
                1L,
                "Read books",
                "Reading improves memory",
                true
        );

        when(habitService.getById(1L)).thenReturn(habitDetailsResponse);

        mockMvc.perform(get("/api/v1/habits/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Read books"))
                .andExpect(jsonPath("$.description").value("Reading improves memory"))
                .andExpect(jsonPath("$.completedToday").value(true));

        verify(habitService).getById(1L);
    }

    @Test
    void getById_shouldReturnNotFoundWhenNoActiveHabitExists() throws Exception {
        when(habitService.getById(1L)).thenThrow(new HabitNotFoundException());

        mockMvc.perform(get("/api/v1/habits/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Habit not found"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        verify(habitService).getById(1L);
    }

    @Test
    void getById_shouldReturnBadRequestWhenIdIsNotNumber() throws Exception {
        mockMvc.perform(get("/api/v1/habits/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        verifyNoInteractions(habitService);
    }

    @Test
    void completeHabit_shouldReturnCreatedCompletion() throws Exception {
        HabitCompletionResponse createdHabitCompletion = new HabitCompletionResponse(
                1L,
                1L,
                TODAY
        );

        when(habitService.completeHabit(1L)).thenReturn(createdHabitCompletion);

        mockMvc.perform(post("/api/v1/habits/1/completions"))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.habitId").value(1))
                .andExpect(jsonPath("$.completionDate").value(TODAY.toString()));

        verify(habitService).completeHabit(1L);
    }

    @Test
    void completeHabit_shouldReturnNotFoundWhenNoActiveHabitExists() throws Exception {
        when(habitService.completeHabit(1L)).thenThrow(new HabitNotFoundException());

        mockMvc.perform(post("/api/v1/habits/1/completions"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Habit not found"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        verify(habitService).completeHabit(1L);
    }

    @Test
    void completeHabit_shouldReturnConflictWhenCompletionAlreadyExistsForToday() throws Exception {
        when(habitService.completeHabit(1L)).thenThrow(new HabitAlreadyCompletedTodayException());

        mockMvc.perform(post("/api/v1/habits/1/completions"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Habit is already completed today"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        verify(habitService).completeHabit(1L);
    }

    @Test
    void completeHabit_shouldReturnBadRequestWhenIdIsNotNumber() throws Exception {
        mockMvc.perform(post("/api/v1/habits/abc/completions"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        verifyNoInteractions(habitService);
    }

    @Test
    void cancelHabitCompletion_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/habits/1/completions"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(habitService).cancelHabitCompletion(1L);
    }

    @Test
    void cancelHabitCompletion_shouldReturnNotFoundWhenNoActiveHabitExists() throws Exception {
        doThrow(new HabitNotFoundException())
                .when(habitService).cancelHabitCompletion(1L);

        mockMvc.perform(delete("/api/v1/habits/1/completions"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Habit not found"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        verify(habitService).cancelHabitCompletion(1L);
    }

    @Test
    void cancelHabitCompletion_shouldReturnConflictWhenCompletionDoesNotExistForToday() throws Exception {
        doThrow(new HabitNotCompletedTodayException())
                .when(habitService).cancelHabitCompletion(1L);

        mockMvc.perform(delete("/api/v1/habits/1/completions"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Habit is not completed today"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        verify(habitService).cancelHabitCompletion(1L);
    }

    @Test
    void cancelHabitCompletion_shouldReturnBadRequestWhenIdIsNotNumber() throws Exception {
        mockMvc.perform(delete("/api/v1/habits/abc/completions"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());

        verifyNoInteractions(habitService);
    }

}
