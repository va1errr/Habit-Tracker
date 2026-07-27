package com.va1err.habittracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Краткие данные привычки в списке")
public record HabitListItemResponse(
        @Schema(
                description = "ID привычки",
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
                description = "Признак выполнения привычки за текущий день",
                example = "true"
        )
        boolean completedToday
) {
}
