package com.va1err.habittracker.dto;

public record HabitListItemResponse(Long id, String name, String description, boolean completedToday) {
}
