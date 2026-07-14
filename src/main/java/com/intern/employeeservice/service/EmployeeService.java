package com.intern.employeeservice.service;

import com.intern.employeeservice.dto.EmployeeCreateRequest;
import com.intern.employeeservice.dto.EmployeeResponse;
import com.intern.employeeservice.entity.Employee;
import com.intern.employeeservice.exception.EmployeeAlreadyExistsException;
import com.intern.employeeservice.exception.EmployeeNotFoundException;
import com.intern.employeeservice.mapper.EmployeeMapper;
import com.intern.employeeservice.repository.EmployeeRepository;
import com.intern.employeeservice.security.SsnHashingService;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SsnHashingService ssnHashingService;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeRepository employeeRepository,
                           SsnHashingService ssnHashingService,
                           EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.ssnHashingService = ssnHashingService;
        this.employeeMapper = employeeMapper;
    }

    public EmployeeResponse createEmployee(@NonNull EmployeeCreateRequest request) {
        String ssnHash = ssnHashingService.hash(request.socialSecurityNumber());

        if (employeeRepository.existsBySocialSecurityNumberHash(ssnHash)) {
            throw new EmployeeAlreadyExistsException("Employee with this SSN number already exists");
        }

        Employee employee = new Employee();
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setDateOfBirth(request.dateOfBirth());
        employee.setGender(request.gender());
        employee.setSocialSecurityNumberHash(ssnHash);

        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toResponse(saved);
    }

    public EmployeeResponse findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with id " + id + " not found"));
        return employeeMapper.toResponse(employee);
    }

}
