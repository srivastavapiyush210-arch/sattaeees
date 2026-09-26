package com.sattaees.sattaees.worker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sattaees.sattaees.auth.dto.RegisterWorkerRequest;
import com.sattaees.sattaees.auth.service.AuthService;
import com.sattaees.sattaees.infrastructure.security.JwtAuthenticationFilter;
import com.sattaees.sattaees.infrastructure.security.JwtTokenProvider;
import com.sattaees.sattaees.worker.dto.WorkerResponseDto;
import com.sattaees.sattaees.worker.service.WorkerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkerController.class)
@AutoConfigureMockMvc(addFilters = false) // Focus on controller HTTP layer
class WorkerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkerService workerService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("GET /api/workers should return 200 and list of workers")
    void getAllWorkers_Returns200() throws Exception {
        WorkerResponseDto w1 = WorkerResponseDto.builder()
                .id(1L)
                .name("Ramesh")
                .skill("Electrician")
                .city("Delhi")
                .averageRating(4.8)
                .hourlyRate(40.0)
                .available(true)
                .build();

        when(workerService.getAllWorkers()).thenReturn(List.of(w1));

        mockMvc.perform(get("/api/workers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ramesh"))
                .andExpect(jsonPath("$[0].skill").value("Electrician"));
    }

    @Test
    @DisplayName("POST /api/workers with invalid data should return 400 Bad Request with validation errors")
    void createWorker_InvalidBody_Returns400() throws Exception {
        RegisterWorkerRequest invalidRequest = RegisterWorkerRequest.builder()
                .name("") // Blank name
                .email("not-an-email") // Invalid email
                .password("123") // Too short
                .build();

        mockMvc.perform(post("/api/workers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}
