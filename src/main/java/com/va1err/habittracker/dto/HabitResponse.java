package com.va1err.habittracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с данными созданной привычки")
public record HabitResponse(
        @Schema(
                description = "ID созданной привычки",
                example = "1"
        )
        Long id,
        @Schema(
                description = "Название привычки",
                example = "Чтение"
        )
        String name,
        @Schema(
                description = "Описание привычки",
                example = "Читать не менее 30 страниц",
                nullable = true
        )
        String description,
        @Schema(
                description = "Признак активности привычки",
                example = "true"
        )
        boolean active
) {
}
