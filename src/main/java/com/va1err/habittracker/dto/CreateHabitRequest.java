package com.va1err.habittracker.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateHabitRequest(@NotBlank String name, String description) {

}
