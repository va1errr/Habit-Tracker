package com.va1err.habittracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на изменение привычки")
public class UpdateHabitRequest {

    @Schema(
            description = "Новое название привычки. Пробелы в начале и конце удаляются",
            example = "Чтение",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private String name;

    @Schema(
            description = "Новое описание привычки: поле отсутствует — сохранить текущее описание; null — удалить; строка — заменить",
            example = "Читать не менее 30 страниц",
            nullable = true
    )
    private String description;

    @Schema(hidden = true)
    private boolean descriptionPresent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionPresent = true;
    }

    public boolean descriptionPresent() {
        return descriptionPresent;
    }

}
