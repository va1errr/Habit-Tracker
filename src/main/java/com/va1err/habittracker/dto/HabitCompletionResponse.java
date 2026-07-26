package com.va1err.habittracker.dto;

import java.time.LocalDate;

public record HabitCompletionResponse(Long id, Long habitId, LocalDate completionDate) {
}
