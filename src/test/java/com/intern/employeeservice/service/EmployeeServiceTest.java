package com.intern.employeeservice.service;


import com.intern.employeeservice.dto.EmployeeCreateRequest;
import com.intern.employeeservice.dto.EmployeeResponse;
import com.intern.employeeservice.entity.Employee;
import com.intern.employeeservice.entity.Gender;
import com.intern.employeeservice.exception.EmployeeAlreadyExistsException;
import com.intern.employeeservice.exception.EmployeeNotFoundException;
import com.intern.employeeservice.mapper.EmployeeMapper;
import com.intern.employeeservice.repository.EmployeeRepository;
import com.intern.employeeservice.security.SsnHashingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

        assertEquals(expectedResponse, result);
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

    @Test
    void shouldReturnEmployeeWhenEmployeeExists() {
        Employee employee = new Employee();

        Long employeeId = 1L;

        employee.setId(employeeId);
        employee.setFirstName("Jan");
        employee.setLastName("Kowalski");
        employee.setGender(Gender.MALE);
        employee.setDateOfBirth(LocalDate.of(1993, 4, 12));
        employee.setSocialSecurityNumberHash("123456789");

        EmployeeResponse expectedResponse = new EmployeeResponse(
                1L, "Jan", "Kowalski", LocalDate.of(1993, 4, 12), Gender.MALE
        );

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(expectedResponse);

        EmployeeResponse result = employeeService.getEmployeeById(employeeId);

        assertEquals(expectedResponse, result);
        verify(employeeRepository).findById(employeeId);
        verify(employeeMapper).toResponse(employee);

    }

    @Test
    void shouldThrowExceptionWhenEmployeeDoesNotExists() {
        Long employeeId = 999999L;

        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.empty());

        EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(employeeId));

        assertEquals("Employee with id 999999 not found", exception.getMessage());

        verify(employeeRepository).findById(employeeId);
        verify(employeeMapper, never()).toResponse(any(Employee.class));
    }

    @Test
    void shouldReturnAllEmployees() {
        Employee firstEmployee = new Employee();
        firstEmployee.setId(1L);
        firstEmployee.setFirstName("Jan");
        firstEmployee.setLastName("Kowalski");
        firstEmployee.setGender(Gender.MALE);
        firstEmployee.setDateOfBirth(LocalDate.of(1993, 4, 12));
        firstEmployee.setSocialSecurityNumberHash("123456789");

        Employee secondEmployee = new Employee();
        secondEmployee.setId(2L);
        secondEmployee.setFirstName("Anna");
        secondEmployee.setLastName("Nowak");
        secondEmployee.setGender(Gender.FEMALE);
        secondEmployee.setDateOfBirth(LocalDate.of(1998, 8, 20));
        secondEmployee.setSocialSecurityNumberHash("987654321");


        EmployeeResponse firstResponse = new EmployeeResponse(
                1L,
                "Jan",
                "Kowalski",
                LocalDate.of(1993, 4, 12),
                Gender.MALE
        );

        EmployeeResponse secondResponse = new EmployeeResponse(
                2L,
                "Anna",
                "Nowak",
                LocalDate.of(1998, 8, 20),
                Gender.FEMALE
        );

        when(employeeRepository.findAll()).thenReturn(List.of(firstEmployee, secondEmployee));

        when(employeeMapper.toResponse(firstEmployee)).thenReturn(firstResponse);
        when(employeeMapper.toResponse(secondEmployee)).thenReturn(secondResponse);

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertEquals(List.of(firstResponse, secondResponse), result);
        assertEquals(2, result.size());

        verify(employeeRepository).findAll();
        verify(employeeMapper).toResponse(firstEmployee);
        verify(employeeMapper).toResponse(secondEmployee);

    }

    @Test
    void shouldReturnEmptyListWhenNoEmployeesExist() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertEquals(List.of(), result);

        verify(employeeRepository).findAll();
        verify(employeeMapper, never()).toResponse(any(Employee.class));
    }
}
