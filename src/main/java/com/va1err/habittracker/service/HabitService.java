package com.va1err.habittracker.service;

import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.repository.HabitRepository;

public class HabitService {

    private final HabitRepository habitRepository;

    public HabitService(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public Habit createHabit(String name, String description) {
        String normalizedName = name.strip();

        Habit habit = new Habit(normalizedName, description, true);
        return habitRepository.save(habit);
    }

}
