package com.intern.employeeservice.controller;

import com.intern.employeeservice.dto.EmployeeCreateRequest;
import com.intern.employeeservice.dto.EmployeeResponse;
import com.intern.employeeservice.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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

}
