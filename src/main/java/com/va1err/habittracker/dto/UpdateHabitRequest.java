package com.va1err.habittracker.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateHabitRequest {

    @NotBlank
    private String name;

    private String description;

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
