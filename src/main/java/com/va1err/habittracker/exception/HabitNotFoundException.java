package com.va1err.habittracker.exception;

public class HabitNotFoundException extends RuntimeException {
    public HabitNotFoundException() {
        super("Habit not found");
    }
}
