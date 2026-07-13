package com.intern.employeeservice.dto;

import com.intern.employeeservice.entity.Gender;

import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender
) {
}
