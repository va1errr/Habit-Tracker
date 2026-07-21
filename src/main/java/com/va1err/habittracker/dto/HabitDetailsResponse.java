package com.va1err.habittracker.dto;

public record HabitDetailsResponse(Long id, String name, String description, boolean completedToday) {
}
