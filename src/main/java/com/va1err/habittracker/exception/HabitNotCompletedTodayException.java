package com.va1err.habittracker.exception;

public class HabitNotCompletedTodayException extends RuntimeException {
    public HabitNotCompletedTodayException() {
        super("Habit is not completed today");
    }
}
