package com.intern.employeeservice.controller;

import com.intern.employeeservice.dto.EmployeeCreateRequest;
import com.intern.employeeservice.dto.EmployeeResponse;
import com.intern.employeeservice.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/employees")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);

        return ResponseEntity.created(URI.create("/employees/" + response.id())).body(response);
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable @Positive Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        List<EmployeeResponse> employeeResponses = employeeService.getAllEmployees();

        return ResponseEntity.ok(employeeResponses);
    }


}