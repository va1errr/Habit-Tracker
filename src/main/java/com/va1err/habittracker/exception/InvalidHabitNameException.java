package com.va1err.habittracker.exception;

public class InvalidHabitNameException extends RuntimeException {
    public InvalidHabitNameException() {
        super("Habit name must not be blank!");
    }
}
