package com.intern.employeeservice.controller;

import com.intern.employeeservice.dto.EmployeeCreateRequest;
import com.intern.employeeservice.dto.EmployeeResponse;
import com.intern.employeeservice.entity.Gender;
import com.intern.employeeservice.exception.EmployeeAlreadyExistsException;
import com.intern.employeeservice.exception.EmployeeNotFoundException;
import com.intern.employeeservice.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;


    @Test
    void shouldCreateEmployee() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Anna",
                "Nowak",
                LocalDate.of(1995, 4, 12),
                Gender.FEMALE,
                "123456789"
        );

        EmployeeResponse response = new EmployeeResponse(
                1L,
                "Anna",
                "Nowak",
                LocalDate.of(1995,4,12),
                Gender.FEMALE
        );

        when(employeeService.createEmployee(any(EmployeeCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/employees")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/employees/1"))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.firstName").value("Anna"))
        .andExpect(jsonPath("$.lastName").value("Nowak"))
        .andExpect(jsonPath("$.dateOfBirth").value("1995-04-12"))
        .andExpect(jsonPath("$.gender").value("FEMALE"))
        .andExpect(jsonPath("$.socialSecurityNumber").doesNotExist())
        .andExpect(jsonPath("$.socialSecurityNumberHash").doesNotExist());

        verify(employeeService).createEmployee(any(EmployeeCreateRequest.class));


    }

    @Test
    void shouldGetEmployeeById() throws Exception {

        EmployeeResponse response = new EmployeeResponse(
                1L,
                "Anna",
                "Nowak",
                LocalDate.of(1995,4,12),
                Gender.FEMALE
        );

        when(employeeService.getEmployeeById(1L)).thenReturn(response);

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Anna"))
                .andExpect(jsonPath("$.lastName").value("Nowak"))
                .andExpect(jsonPath("$.dateOfBirth").value("1995-04-12"))
                .andExpect(jsonPath("$.gender").value("FEMALE"))
                .andExpect(jsonPath("$.socialSecurityNumber").doesNotExist())
                .andExpect(jsonPath("$.socialSecurityNumberHash").doesNotExist());

        verify(employeeService).getEmployeeById(1L);
    }

    @Test
    void shouldGetAllEmployees() throws Exception {

        EmployeeResponse response1 = new EmployeeResponse(
                1L,
                "Anna",
                "Nowak",
                LocalDate.of(1995,4,12),
                Gender.FEMALE
        );

        EmployeeResponse response2 = new EmployeeResponse(
                2L,
                "Adam",
                "Kowalski",
                LocalDate.of(1983,7,4),
                Gender.MALE
        );

        when(employeeService.getAllEmployees()).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[0].gender").value("FEMALE"))
                .andExpect(jsonPath("$[1].gender").value("MALE"))
                .andExpect(jsonPath("$[0].firstName").value("Anna"))
                .andExpect(jsonPath("$[1].firstName").value("Adam"))
                .andExpect(jsonPath("$[0].lastName").value("Nowak"))
                .andExpect(jsonPath("$[1].lastName").value("Kowalski"))
                .andExpect(jsonPath("$[0].dateOfBirth").value("1995-04-12"))
                .andExpect(jsonPath("$[1].dateOfBirth").value("1983-07-04"))
                .andExpect(jsonPath("$[0].socialSecurityNumberHash").doesNotExist())
                .andExpect(jsonPath("$[1].socialSecurityNumberHash").doesNotExist())
                .andExpect(jsonPath("$[0].socialSecurityNumber").doesNotExist())
                .andExpect(jsonPath("$[1].socialSecurityNumber").doesNotExist());

        verify(employeeService).getAllEmployees();
    }


    @Test
    void shouldReturn404WhenEmployeeNotFound() throws Exception {

        when(employeeService.getEmployeeById(999999L)).thenThrow(new EmployeeNotFoundException("Employee with id 999999 not found"));

        mockMvc.perform(get("/employees/999999"))
                        .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee with id 999999 not found"));

        verify(employeeService).getEmployeeById(999999L);

    }

    @Test
    void shouldReturn409WhenSsnAlreadyExists() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Anna",
                "Nowak",
                LocalDate.of(1995, 4, 12),
                Gender.FEMALE,
                "123456789"
        );

        when(employeeService.createEmployee(any(EmployeeCreateRequest.class)))
                .thenThrow(new EmployeeAlreadyExistsException("Employee with this SSN already exists"));

        mockMvc.perform(post("/employees").
                contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.message").value("Employee with this SSN already exists"))
                .andExpect(status().isConflict());

        verify(employeeService).createEmployee(any(EmployeeCreateRequest.class));
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {

        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "",
                "",
                LocalDate.now().plusDays(1),
                null,
                "abc"
        );

        mockMvc.perform(post("/employees")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray());


    }

    @Test
    void shouldReturn400WhenIdIsNotANumber() throws Exception {
        mockMvc.perform(get("/employees/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));
    }
}
