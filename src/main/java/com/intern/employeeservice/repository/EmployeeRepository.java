package com.intern.employeeservice.repository;

import com.intern.employeeservice.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsBySocialSecurityNumberHash (String socialSecurityNumberHash);
}
