package com.va1err.habittracker.repository;

import com.va1err.habittracker.entity.Habit;

public interface HabitRepository {
    Habit save(Habit habit);
    boolean existsByNameIgnoreCase(String name);
}
