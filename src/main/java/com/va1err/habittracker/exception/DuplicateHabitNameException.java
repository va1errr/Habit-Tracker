package com.va1err.habittracker.exception;

public class DuplicateHabitNameException extends RuntimeException {
    public DuplicateHabitNameException() {
        super("Habit name already exists!");
    }
}
