package com.intern.employeeservice.repository;

import com.intern.employeeservice.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsBySocialSecurityNumberHash(String socialSecurityNumberHash);
}
