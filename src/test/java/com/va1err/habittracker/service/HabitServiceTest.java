package com.va1err.habittracker.service;

import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.exception.DuplicateHabitNameException;
import com.va1err.habittracker.exception.InvalidHabitNameException;
import com.va1err.habittracker.repository.HabitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {

    @InjectMocks
    private HabitService habitService;

    @Mock
    private HabitRepository habitRepository;

    @Captor
    private ArgumentCaptor<Habit> habitCaptor;

    @Test
    void createHabit_shouldTrimNameAndActivateHabit() {
        habitService.createHabit("  Read books ", null);

        verify(habitRepository).saveAndFlush(habitCaptor.capture());

        Habit savedHabit = habitCaptor.getValue();

        assertEquals("Read books", savedHabit.getName());
        assertNull(savedHabit.getDescription());
        assertTrue(savedHabit.isActive());
    }

    @Test
    void createHabit_shouldPreserveDescription() {
        habitService.createHabit("Read books", "Reading improves memory");

        verify(habitRepository).saveAndFlush(habitCaptor.capture());

        Habit savedHabit = habitCaptor.getValue();

        assertEquals("Reading improves memory", savedHabit.getDescription());
    }

    @Test
    void createHabit_shouldRejectBlankName() {
        assertThrows(InvalidHabitNameException.class, () -> habitService.createHabit("    ", null));
        verifyNoInteractions(habitRepository);
    }

    @Test
    void createHabit_shouldRejectEmptyName() {
        assertThrows(InvalidHabitNameException.class, () -> habitService.createHabit("", null));
        verifyNoInteractions(habitRepository);
    }

    @Test
    void createHabit_shouldRejectNullName() {
        assertThrows(InvalidHabitNameException.class, () -> habitService.createHabit(null, null));
        verifyNoInteractions(habitRepository);
    }

    @Test
    void createHabit_shouldRejectDuplicateName() {
        when(habitRepository.existsByNameIgnoreCase("Reading")).thenReturn(true);

        assertThrows(DuplicateHabitNameException.class, () -> habitService.createHabit("  Reading ", null));
        verify(habitRepository).existsByNameIgnoreCase("Reading");
        verify(habitRepository, never()).saveAndFlush(any(Habit.class));
    }

    @Test
    void createHabit_shouldRejectDuplicateWhenDatabaseConstraintIsViolated() {
        when(habitRepository.saveAndFlush(any(Habit.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Unique constraint violation"
                ));

        assertThrows(DuplicateHabitNameException.class, () -> habitService.createHabit("Reading", null));
    }

}
