package com.va1err.habittracker.repository;

import com.va1err.habittracker.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Habit> findAllByActiveTrue();
}
