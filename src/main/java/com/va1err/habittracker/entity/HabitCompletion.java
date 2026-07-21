package com.va1err.habittracker.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "habit_completions")
public class HabitCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    @Column(name = "completion_date", nullable = false)
    private LocalDate completionDate;

    protected HabitCompletion() {

    }

    public HabitCompletion(Habit habit, LocalDate completionDate) {
        this.habit = habit;
        this.completionDate = completionDate;
    }

    public Long getId() {
        return id;
    }

    public Habit getHabit() {
        return habit;
    }
}
