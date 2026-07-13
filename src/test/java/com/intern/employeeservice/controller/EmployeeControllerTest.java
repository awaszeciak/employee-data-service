package com.intern.employeeservice.controller;

import com.intern.employeeservice.dto.EmployeeCreateRequest;
import com.intern.employeeservice.dto.EmployeeResponse;
import com.intern.employeeservice.entity.Gender;
import com.intern.employeeservice.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

}
