package com.va1err.habittracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateHabitNameException.class)
    public ResponseEntity<Void> handleDuplicateHabitName(DuplicateHabitNameException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

}
