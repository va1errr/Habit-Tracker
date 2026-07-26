package com.va1err.habittracker.exception;

public class HabitAlreadyCompletedTodayException extends RuntimeException {
    public HabitAlreadyCompletedTodayException() {
        super("Habit is already completed today");
    }
}
