package com.va1err.habittracker.dto;

import java.util.List;

public record ApiErrorResponse(int status, String message, List<ApiFieldError> errors) {
}
