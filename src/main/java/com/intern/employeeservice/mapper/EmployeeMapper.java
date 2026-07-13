package com.intern.employeeservice.mapper;

import com.intern.employeeservice.dto.EmployeeResponse;
import com.intern.employeeservice.entity.Employee;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeResponse toResponse(@NonNull Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getDateOfBirth(),
                employee.getGender()
        );
    }
}
