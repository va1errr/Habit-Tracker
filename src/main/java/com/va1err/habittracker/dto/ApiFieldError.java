package com.va1err.habittracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Детали ошибки отдельного поля")
public record ApiFieldError(
        @Schema(
                description = "Имя поля, в котором произошла ошибка",
                example = "name"
        )
        String field,
        @Schema(
                description = "Описание ошибки поля",
                example = "must not be blank"
        )
        String message
) {
}
