package com.va1err.habittracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Структурированный ответ API об ошибке")
public record ApiErrorResponse(
        @Schema(
                description = "HTTP-статус ошибки",
                example = "400"
        )
        int status,
        @Schema(
                description = "Краткое описание ошибки",
                example = "Validation failed"
        )
        String message,
        @Schema(
                description = "Список ошибок отдельных полей; пустой для ошибок, не связанных с валидацией"
        )
        List<ApiFieldError> errors
) {
}
