package com.va1err.habittracker.controller;

import com.va1err.habittracker.dto.CreateHabitRequest;
import com.va1err.habittracker.dto.HabitResponse;
import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.service.HabitService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HabitResponse createHabit(@RequestBody CreateHabitRequest request) {
        Habit habit = habitService.createHabit(request.name(), request.description());

        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.isActive()
        );
    }

}
