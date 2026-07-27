package com.va1err.habittracker.controller;

import com.va1err.habittracker.dto.*;
import com.va1err.habittracker.entity.Habit;
import com.va1err.habittracker.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HabitResponse createHabit(@Valid @RequestBody CreateHabitRequest request) {
        Habit habit = habitService.createHabit(request.name(), request.description());

        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.isActive()
        );
    }

    @GetMapping
    public List<HabitListItemResponse> listHabits() {
        return habitService.listHabits();
    }

    @GetMapping("/{id}")
    public HabitDetailsResponse getById(@PathVariable Long id) {
        return habitService.getById(id);
    }

    @PostMapping("/{id}/completions")
    @ResponseStatus(HttpStatus.CREATED)
    public HabitCompletionResponse completeHabit(@PathVariable Long id) {
        return habitService.completeHabit(id);
    }

    @DeleteMapping("/{id}/completions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelHabitCompletion(@PathVariable Long id) {
        habitService.cancelHabitCompletion(id);
    }

}
