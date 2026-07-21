package com.va1err.habittracker.exception;

import com.va1err.habittracker.dto.ApiErrorResponse;
import com.va1err.habittracker.dto.ApiFieldError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateHabitNameException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateHabitName(DuplicateHabitNameException exception) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                List.of()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationFailed(MethodArgumentNotValidException exception) {
        List<ApiFieldError> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiFieldError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HabitNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleHabitNotFound(HabitNotFoundException exception) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                List.of()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request parameter",
                List.of()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
