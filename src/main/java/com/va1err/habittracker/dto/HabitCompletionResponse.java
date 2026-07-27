package com.va1err.habittracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Данные о выполнении привычки")
public record HabitCompletionResponse(
        @Schema(
                description = "ID факта выполнения",
                example = "1"
        )
        Long id,
        @Schema(
                description = "ID выполненной привычки",
                example = "1"
        )
        Long habitId,
        @Schema(
                description = "Дата выполнения привычки",
                example = "2026-07-28",
                format = "date"
        )
        LocalDate completionDate
) {
}
