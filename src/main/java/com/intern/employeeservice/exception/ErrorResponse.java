package com.intern.employeeservice.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime time,
        int status,
        String message,
        List<String> details
) {
    public ErrorResponse(int status, String message) {
        this(LocalDateTime.now(), status, message, null);
    }

    public ErrorResponse(int status, String message, List<String> details) {
        this(LocalDateTime.now(), status, message, details);
    }

}
