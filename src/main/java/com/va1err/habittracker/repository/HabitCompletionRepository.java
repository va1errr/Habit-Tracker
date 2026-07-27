package com.va1err.habittracker.repository;

import com.va1err.habittracker.entity.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {
    List<HabitCompletion> findAllByCompletionDate(LocalDate completionDate);
    boolean existsByHabitIdAndCompletionDate(Long habitId, LocalDate completionDate);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            DELETE FROM HabitCompletion hc
            WHERE hc.habit.id = :habitId
            AND hc.completionDate = :completionDate
            """)
    int deleteByHabitIdAndCompletionDate(
            @Param(value = "habitId") Long habitId,
            @Param(value = "completionDate") LocalDate completionDate
    );
}
