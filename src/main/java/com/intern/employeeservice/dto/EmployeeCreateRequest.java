package com.intern.employeeservice.dto;

import com.intern.employeeservice.entity.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record EmployeeCreateRequest (
        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotNull
        @Past
        LocalDate dateOfBirth,

        @NotNull
        Gender gender,

        @NotBlank
        @Pattern(regexp = "\\d{9}")
        String socialSecurityNumber
) {

}