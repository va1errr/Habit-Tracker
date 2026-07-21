package com.va1err.habittracker.repository;

import com.va1err.habittracker.entity.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {
    List<HabitCompletion> findAllByCompletionDate(LocalDate completionDate);
}
