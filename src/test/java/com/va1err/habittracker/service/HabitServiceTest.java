package com.va1err.habittracker.service;

import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.exception.InvalidHabitNameException;
import com.va1err.habittracker.repository.HabitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        verify(habitRepository).save(habitCaptor.capture());

        Habit savedHabit = habitCaptor.getValue();

        assertEquals("Read books", savedHabit.getName());
        assertTrue(savedHabit.isActive());
    }

    @Test
    void createHabit_shouldRejectBlankName() {
        assertThrows(InvalidHabitNameException.class, () -> habitService.createHabit("    ", null));
        verifyNoInteractions(habitRepository);
    }

}
