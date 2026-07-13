package com.intern.employeeservice.dto;

import com.intern.employeeservice.entity.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record EmployeeCreateRequest (
        @NotBlank(message = "First name must not be blank")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name must not be blank")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotNull(message = "Gender is required")
        Gender gender,

        @NotBlank(message = "Social security number must not be blank")
        @Pattern(regexp = "\\d{9}",
                message = "Social security number must contain 9 digits")
        String socialSecurityNumber
) {

}