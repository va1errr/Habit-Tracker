package com.va1err.habittracker.service;

import com.va1err.habittracker.dto.HabitCompletionResponse;
import com.va1err.habittracker.dto.HabitDetailsResponse;
import com.va1err.habittracker.dto.HabitListItemResponse;
import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.entity.HabitCompletion;
import com.va1err.habittracker.exception.*;
import com.va1err.habittracker.repository.HabitCompletionRepository;
import com.va1err.habittracker.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 21);

    private HabitService habitService;

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private HabitCompletionRepository habitCompletionRepository;

    @Captor
    private ArgumentCaptor<Habit> habitCaptor;

    @Captor
    private ArgumentCaptor<HabitCompletion> habitCompletionCaptor;

    @BeforeEach
    void createHabitService() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-07-21T09:00:00Z"),
                ZoneId.of("Europe/Moscow")
        );

        habitService = new HabitService(
                habitRepository,
                habitCompletionRepository,
                fixedClock
        );
    }

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

    @Test
    void listHabits_shouldReturnEmptyListWhenNoActiveHabits() {
        when(habitRepository.findAllByActiveTrue()).thenReturn(List.of());

        var result = habitService.listHabits();

        assertTrue(result.isEmpty());
        verify(habitRepository).findAllByActiveTrue();
    }

    @Test
    void listHabits_shouldReturnHabitWithCompletedTodayFalseWhenNoCompletionForCurrentDate() {
        Habit habit = mock(Habit.class);

        when(habit.getId()).thenReturn(1L);
        when(habit.getName()).thenReturn("Reading");
        when(habit.getDescription()).thenReturn(null);

        HabitListItemResponse expected = new HabitListItemResponse(
                1L,
                "Reading",
                null,
                false
        );

        when(habitRepository.findAllByActiveTrue()).thenReturn(List.of(habit));
        when(habitCompletionRepository.findAllByCompletionDate(TODAY)).thenReturn(List.of());

        var result = habitService.listHabits();

        assertEquals(List.of(expected), result);
        verify(habitCompletionRepository).findAllByCompletionDate(TODAY);
    }

    @Test
    void listHabits_shouldReturnHabitWithCompletedTodayTrueWhenCompletionExistsForCurrentDate() {
        Habit habit = mock(Habit.class);

        when(habit.getId()).thenReturn(1L);
        when(habit.getName()).thenReturn("Reading");
        when(habit.getDescription()).thenReturn(null);

        HabitCompletion habitCompletion = new HabitCompletion(
                habit,
                TODAY
        );

        when(habitRepository.findAllByActiveTrue()).thenReturn(List.of(habit));
        when(habitCompletionRepository.findAllByCompletionDate(TODAY)).thenReturn(List.of(habitCompletion));

        HabitListItemResponse expected = new HabitListItemResponse(
                1L,
                "Reading",
                null,
                true
        );
        var result = habitService.listHabits();

        assertEquals(List.of(expected), result);
        verify(habitCompletionRepository).findAllByCompletionDate(TODAY);
    }

    @Test
    void listHabits_shouldSetCompletedTodayOnlyForHabitWithCurrentDateCompletion() {
        Habit habit1 = mock(Habit.class);
        Habit habit2 = mock(Habit.class);

        when(habit1.getId()).thenReturn(1L);
        when(habit1.getName()).thenReturn("Reading");
        when(habit1.getDescription()).thenReturn(null);

        when(habit2.getId()).thenReturn(2L);
        when(habit2.getName()).thenReturn("Writing");
        when(habit2.getDescription()).thenReturn(null);

        HabitCompletion habitCompletion = new HabitCompletion(
                habit1,
                TODAY
        );

        when(habitRepository.findAllByActiveTrue()).thenReturn(List.of(habit1, habit2));
        when(habitCompletionRepository.findAllByCompletionDate(TODAY)).thenReturn(List.of(habitCompletion));

        List<HabitListItemResponse> expected = List.of(
                new HabitListItemResponse(
                        1L,
                        "Reading",
                        null,
                        true
                ),
                new HabitListItemResponse(
                        2L,
                        "Writing",
                        null,
                        false
                )
        );
        var result = habitService.listHabits();

        assertThat(result).containsExactlyInAnyOrderElementsOf(expected);
        verify(habitCompletionRepository).findAllByCompletionDate(TODAY);
    }

    @Test
    void getById_shouldReturnActiveHabitWithCompletedTodayFalseWhenNoCompletionExistsForToday() {
        Habit habit = mock(Habit.class);

        when(habit.getId()).thenReturn(1L);
        when(habit.getName()).thenReturn("Reading");
        when(habit.getDescription()).thenReturn(null);

        when(habitRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(habit));
        when(habitCompletionRepository.existsByHabitIdAndCompletionDate(1L, TODAY)).thenReturn(false);

        HabitDetailsResponse expected = new HabitDetailsResponse(
                1L,
                "Reading",
                null,
                false
        );

        var result = habitService.getById(1L);

        assertEquals(expected, result);
        verify(habitRepository).findByIdAndActiveTrue(1L);
        verify(habitCompletionRepository).existsByHabitIdAndCompletionDate(1L, TODAY);
    }

    @Test
    void getById_shouldReturnCompletedTodayTrueWhenCompletionExistsForToday() {
        Habit habit = mock(Habit.class);

        when(habit.getId()).thenReturn(1L);
        when(habit.getName()).thenReturn("Reading");
        when(habit.getDescription()).thenReturn(null);

        when(habitRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(habit));
        when(habitCompletionRepository.existsByHabitIdAndCompletionDate(1L, TODAY)).thenReturn(true);

        HabitDetailsResponse expected = new HabitDetailsResponse(
                1L,
                "Reading",
                null,
                true
        );

        var result = habitService.getById(1L);

        assertEquals(expected, result);
        verify(habitRepository).findByIdAndActiveTrue(1L);
        verify(habitCompletionRepository).existsByHabitIdAndCompletionDate(1L, TODAY);
    }

    @Test
    void getById_shouldThrowHabitNotFoundExceptionWhenNoActiveHabitExists() {
        when(habitRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(HabitNotFoundException.class, () -> habitService.getById(1L));
        verify(habitRepository).findByIdAndActiveTrue(1L);
        verify(habitCompletionRepository, never()).existsByHabitIdAndCompletionDate(any(), any());
    }

    @Test
    void completeHabit_shouldCreateAndReturnCompletionForCurrentDate() {
        Habit habit = mock(Habit.class);
        HabitCompletion habitCompletion = mock(HabitCompletion.class);

        when(habit.getId()).thenReturn(1L);

        when(habitCompletion.getId()).thenReturn(1L);
        when(habitCompletion.getHabit()).thenReturn(habit);
        when(habitCompletion.getCompletionDate()).thenReturn(TODAY);

        when(habitRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(habit));
        when(habitCompletionRepository.saveAndFlush(any(HabitCompletion.class))).thenReturn(habitCompletion);

        HabitCompletionResponse result = habitService.completeHabit(1L);
        verify(habitCompletionRepository).saveAndFlush(habitCompletionCaptor.capture());

        HabitCompletion savedHabitCompletion = habitCompletionCaptor.getValue();

        assertEquals(
                new HabitCompletionResponse(1L, 1L, TODAY),
                result
        );
        assertEquals(1L, savedHabitCompletion.getHabit().getId());
        assertEquals(TODAY, savedHabitCompletion.getCompletionDate());
    }

    @Test
    void completeHabit_shouldThrowHabitNotFoundExceptionWhenNoActiveHabitExists() {
        when(habitRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(HabitNotFoundException.class, () -> habitService.completeHabit(1L));
        verify(habitRepository).findByIdAndActiveTrue(1L);
        verifyNoInteractions(habitCompletionRepository);
    }

    @Test
    void completeHabit_shouldThrowHabitAlreadyCompletedTodayExceptionWhenCompletionExistsForToday() {
        Habit habit = mock(Habit.class);

        when(habitRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(habit));
        when(habitCompletionRepository.existsByHabitIdAndCompletionDate(1L, TODAY)).thenReturn(true);

        assertThrows(HabitAlreadyCompletedTodayException.class,
                () -> habitService.completeHabit(1L));

        verify(habitCompletionRepository, never()).saveAndFlush(any(HabitCompletion.class));
    }

    @Test
    void completeHabit_shouldThrowHabitAlreadyCompletedTodayExceptionWhenDatabaseConstraintIsViolated() {
        Habit habit = mock(Habit.class);

        when(habitRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(habit));
        when(habitCompletionRepository.existsByHabitIdAndCompletionDate(1L, TODAY)).thenReturn(false);
        when(habitCompletionRepository.saveAndFlush(any(HabitCompletion.class)))
                .thenThrow(new DataIntegrityViolationException("Unique data constraint violation"));

        assertThrows(HabitAlreadyCompletedTodayException.class,
                () -> habitService.completeHabit(1L));
    }

    @Test
    void cancelHabitCompletion_shouldDeleteCompletionForCurrentDate() {
        Habit habit = mock(Habit.class);
        Long testHabitId = 11L;

        when(habitRepository.findByIdAndActiveTrue(testHabitId)).thenReturn(Optional.of(habit));
        when(habitCompletionRepository.deleteByHabitIdAndCompletionDate(testHabitId, TODAY)).thenReturn(1);

        habitService.cancelHabitCompletion(testHabitId);

        verify(habitRepository).findByIdAndActiveTrue(testHabitId);
        verify(habitCompletionRepository).deleteByHabitIdAndCompletionDate(testHabitId, TODAY);
    }

    @Test
    void cancelHabitCompletion_shouldThrowHabitNotFoundExceptionWhenNoActiveHabitExists() {
        Long testHabitId = 11L;

        when(habitRepository.findByIdAndActiveTrue(testHabitId)).thenReturn(Optional.empty());

        assertThrows(HabitNotFoundException.class,
                () -> habitService.cancelHabitCompletion(testHabitId));
        verify(habitRepository).findByIdAndActiveTrue(testHabitId);
        verifyNoInteractions(habitCompletionRepository);
    }

    @Test
    void cancelHabitCompletion_shouldThrowHabitNotCompletedTodayExceptionWhenNoCompletionExistsForToday() {
        Habit habit = mock(Habit.class);
        Long testHabitId = 11L;

        when(habitRepository.findByIdAndActiveTrue(testHabitId)).thenReturn(Optional.of(habit));
        when(habitCompletionRepository.deleteByHabitIdAndCompletionDate(testHabitId, TODAY)).thenReturn(0);

        assertThrows(HabitNotCompletedTodayException.class,
                () -> habitService.cancelHabitCompletion(testHabitId));
        verify(habitRepository).findByIdAndActiveTrue(testHabitId);
        verify(habitCompletionRepository).deleteByHabitIdAndCompletionDate(testHabitId, TODAY);
    }

    @Test
    void updateHabit_shouldThrowHabitNotFoundExceptionWhenHabitDoesNotExist() {
        Long testHabitId = 11L;

        when(habitRepository.findByIdAndActiveTrue(testHabitId)).thenReturn(Optional.empty());

        assertThrows(HabitNotFoundException.class, () -> habitService.updateHabit(testHabitId, "reading", null, false));
        verify(habitRepository).findByIdAndActiveTrue(testHabitId);
        verifyNoMoreInteractions(habitRepository);
    }

    @Test
    void updateHabit_shouldThrowInvalidHabitNameExceptionWhenNameIsBlank() {
        Long testHabitId = 11L;

        assertThrows(InvalidHabitNameException.class, () -> habitService.updateHabit(testHabitId, "  ", null, false));
        verifyNoInteractions(habitRepository);
    }

    @Test
    void updateHabit_shouldThrowInvalidHabitNameExceptionWhenNameIsNull() {
        Long testHabitId = 11L;

        assertThrows(InvalidHabitNameException.class, () -> habitService.updateHabit(testHabitId, null, null, false));
        verifyNoInteractions(habitRepository);
    }

    @Test
    void updateHabit_shouldThrowDuplicateHabitNameExceptionWhenNameBelongsToAnotherHabit() {
        Habit habit = mock(Habit.class);
        Long testHabitId = 11L;

        when(habitRepository.findByIdAndActiveTrue(testHabitId)).thenReturn(Optional.of(habit));
        when(habitRepository.existsByNameIgnoreCaseAndIdNot("Reading", testHabitId)).thenReturn(true);

        assertThrows(DuplicateHabitNameException.class, () -> habitService.updateHabit(testHabitId, "Reading", null, false));
        verify(habitRepository).findByIdAndActiveTrue(testHabitId);
        verify(habitRepository).existsByNameIgnoreCaseAndIdNot("Reading", testHabitId);
        verifyNoMoreInteractions(habitRepository);
    }

    @Test
    void updateHabit_shouldUpdateNameAndDescriptionWhenHabitIsActive() {
        Habit habit = new Habit("Read books", "Reading improves memory", true);
        Long testHabitId = 11L;

        when(habitRepository.findByIdAndActiveTrue(testHabitId)).thenReturn(Optional.of(habit));
        when(habitRepository.existsByNameIgnoreCaseAndIdNot("Write poems", testHabitId)).thenReturn(false);

        Habit updatedHabit = habitService.updateHabit(testHabitId, "Write poems", "Writing improves motorics", true);

        assertSame(habit, updatedHabit);
        assertEquals("Write poems", updatedHabit.getName());
        assertEquals("Writing improves motorics", updatedHabit.getDescription());
        verify(habitRepository).findByIdAndActiveTrue(testHabitId);
        verify(habitRepository).existsByNameIgnoreCaseAndIdNot("Write poems", testHabitId);
    }

    @Test
    void updateHabit_shouldRemoveDescriptionWhenDescriptionIsExplicitlyNull() {
        Habit habit = new Habit("Read books", "Reading improves memory", true);
        Long testHabitId = 11L;

        when(habitRepository.findByIdAndActiveTrue(testHabitId)).thenReturn(Optional.of(habit));
        when(habitRepository.existsByNameIgnoreCaseAndIdNot("Write poems", testHabitId)).thenReturn(false);

        Habit updatedHabit = habitService.updateHabit(testHabitId, "Write poems", null, true);

        assertSame(habit, updatedHabit);
        assertEquals("Write poems", updatedHabit.getName());
        assertNull(updatedHabit.getDescription());
        verify(habitRepository).findByIdAndActiveTrue(testHabitId);
        verify(habitRepository).existsByNameIgnoreCaseAndIdNot("Write poems", testHabitId);
    }

    @Test
    void updateHabit_shouldPreserveExistingDescriptionWhenDescriptionIsAbsent() {
        Habit habit = new Habit("Read books", "Reading improves memory", true);
        Long testHabitId = 11L;

        when(habitRepository.findByIdAndActiveTrue(testHabitId)).thenReturn(Optional.of(habit));
        when(habitRepository.existsByNameIgnoreCaseAndIdNot("Write poems", testHabitId)).thenReturn(false);

        Habit updatedHabit = habitService.updateHabit(testHabitId, "Write poems", null, false);

        assertSame(habit, updatedHabit);
        assertEquals("Write poems", updatedHabit.getName());
        assertEquals("Reading improves memory", updatedHabit.getDescription());
        verify(habitRepository).findByIdAndActiveTrue(testHabitId);
        verify(habitRepository).existsByNameIgnoreCaseAndIdNot("Write poems", testHabitId);
    }

    @Test
    void updateHabit_shouldThrowDuplicateHabitNameExceptionWhenDatabaseConstraintIsViolated() {
        Habit habit = mock(Habit.class);
        Long testHabitId = 11L;

        when(habitRepository.findByIdAndActiveTrue(testHabitId)).thenReturn(Optional.of(habit));
        when(habitRepository.existsByNameIgnoreCaseAndIdNot("Reading", testHabitId)).thenReturn(false);
        doThrow(new DataIntegrityViolationException("Unique constraint violation")).when(habitRepository).flush();

        assertThrows(DuplicateHabitNameException.class, () -> habitService.updateHabit(testHabitId, "Reading", null, false));
        verify(habitRepository).findByIdAndActiveTrue(testHabitId);
        verify(habitRepository).existsByNameIgnoreCaseAndIdNot("Reading", testHabitId);
        verify(habitRepository).flush();
    }

    @Test
    void updateHabit_shouldTrimNameBeforeCheckingUniquenessAndUpdating() {
        Habit habit = new Habit("Read books", "Reading improves memory", true);
        Long testHabitId = 11L;

        when(habitRepository.findByIdAndActiveTrue(testHabitId)).thenReturn(Optional.of(habit));
        when(habitRepository.existsByNameIgnoreCaseAndIdNot("Write poems", testHabitId)).thenReturn(false);

        Habit updatedHabit = habitService.updateHabit(testHabitId, "  Write poems ", null, true);

        assertSame(habit, updatedHabit);
        assertEquals("Write poems", updatedHabit.getName());
        verify(habitRepository).findByIdAndActiveTrue(testHabitId);
        verify(habitRepository).existsByNameIgnoreCaseAndIdNot("Write poems", testHabitId);
    }

    @Test
    void archiveHabit_shouldArchiveActiveHabit() {
        Habit habit = new Habit("Reading", null, true);
        Long testHabitId = 11L;

        when(habitRepository.findByIdAndActiveTrue(testHabitId)).thenReturn(Optional.of(habit));

        habitService.archiveHabit(testHabitId);

        assertFalse(habit.isActive());
    }

    @Test
    void archiveHabit_shouldThrowHabitNotFoundExceptionWhenNoActiveHabitExists() {
        Long testHabitId = 11L;

        when(habitRepository.findByIdAndActiveTrue(testHabitId)).thenReturn(Optional.empty());

        assertThrows(HabitNotFoundException.class,
                () -> habitService.archiveHabit(testHabitId));
    }

}
