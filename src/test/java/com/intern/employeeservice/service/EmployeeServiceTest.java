package com.intern.employeeservice.service;


import com.intern.employeeservice.dto.EmployeeCreateRequest;
import com.intern.employeeservice.dto.EmployeeResponse;
import com.intern.employeeservice.entity.Employee;
import com.intern.employeeservice.entity.Gender;
import com.intern.employeeservice.exception.EmployeeAlreadyExistsException;
import com.intern.employeeservice.mapper.EmployeeMapper;
import com.intern.employeeservice.repository.EmployeeRepository;
import com.intern.employeeservice.security.SsnHashingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SsnHashingService ssnHashingService;

    @Mock
    private EmployeeMapper employeeMapper;

    private EmployeeService employeeService;

    private EmployeeCreateRequest request;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository, ssnHashingService, employeeMapper);
        request = new EmployeeCreateRequest(
                "Jan",
                "Kowalski",
                LocalDate.of(1993, 4, 12),
                Gender.MALE,
                "123456789"
        );
    }


    @Test
    void shouldCreateEmployeeWhenSsnDoesNotExist() {
        String fakeHash = "hashed-value";
        Employee savedEmployee = new Employee();
        savedEmployee.setId(1L);

        EmployeeResponse expectedResponse = new EmployeeResponse(
                1L, "Jan", "Kowalski", LocalDate.of(1993, 4, 12), Gender.MALE
        );

        when(ssnHashingService.hash("123456789")).thenReturn(fakeHash);
        when(employeeRepository.existsBySocialSecurityNumberHash(fakeHash)).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);
        when(employeeMapper.toResponse(savedEmployee)).thenReturn(expectedResponse);

        EmployeeResponse result = employeeService.createEmployee(request);

        assertEquals(result, expectedResponse);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void shouldThrowExceptionWhenSsnAlreadyExists() {
        String fakeHash = "hashed-value";

        when(ssnHashingService.hash("123456789")).thenReturn(fakeHash);
        when(employeeRepository.existsBySocialSecurityNumberHash(fakeHash)).thenReturn(true);

        assertThrows(EmployeeAlreadyExistsException.class,
                () -> employeeService.createEmployee(request));

        verify(employeeRepository, never()).save(any(Employee.class));
        verify(employeeMapper, never()).toResponse(any(Employee.class));
    }

    @Test
    void shouldStoreHashedSsnNotPlaintext() {
        String fakeHash = "hashed-value";
        when(ssnHashingService.hash("123456789")).thenReturn(fakeHash);
        when(employeeRepository.existsBySocialSecurityNumberHash(fakeHash)).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(
                new EmployeeResponse(1L, "Jan", "Kowalski",
                        LocalDate.of(1993, 4, 12), Gender.MALE));

        employeeService.createEmployee(request);

        verify(employeeRepository).save(argThat(employee ->
                employee.getSocialSecurityNumberHash().equals(fakeHash)
                && !request.socialSecurityNumber().equals(employee.getSocialSecurityNumberHash())
        ));

    }
}
