package com.va1err.habittracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на создание привычки")
public record CreateHabitRequest(
        @Schema(
                description = "Название привычки",
                example = "Чтение",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        String name,
        @Schema(
                description = "Описание привычки",
                example = "Читать не менее 30 страниц",
                nullable = true
        )
        String description
) {
}
