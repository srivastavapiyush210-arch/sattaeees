package com.sattaees.sattaees.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sattaees.sattaees.infrastructure.security.JwtAuthenticationFilter;
import com.sattaees.sattaees.infrastructure.security.JwtTokenProvider;
import com.sattaees.sattaees.job.dto.CreateJobRequestDto;
import com.sattaees.sattaees.job.dto.JobResponseDto;
import com.sattaees.sattaees.job.entity.JobStatus;
import com.sattaees.sattaees.job.service.JobRequestService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobRequestService jobRequestService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("GET /api/job-requests should return 200 and list of jobs")
    void getAllJobs_Returns200() throws Exception {
        JobResponseDto job = JobResponseDto.builder()
                .id(1L)
                .serviceType("Plumbing")
                .location("Delhi")
                .status(JobStatus.REQUESTED)
                .build();

        when(jobRequestService.getAllJobRequests()).thenReturn(List.of(job));

        mockMvc.perform(get("/api/job-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].serviceType").value("Plumbing"));
    }

    @Test
    @DisplayName("POST /api/job-requests should return 201 Created on valid job creation")
    void createJob_Success_Returns201() throws Exception {
        CreateJobRequestDto request = CreateJobRequestDto.builder()
                .serviceType("Electrician")
                .location("Mumbai")
                .customerId(1L)
                .workerId(2L)
                .build();

        JobResponseDto createdJob = JobResponseDto.builder()
                .id(5L)
                .serviceType("Electrician")
                .location("Mumbai")
                .status(JobStatus.REQUESTED)
                .build();

        when(jobRequestService.createJobRequest(any(), any())).thenReturn(createdJob);

        mockMvc.perform(post("/api/job-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    @DisplayName("PUT /api/job-requests/{id}/status?status=ACCEPTED should return 200 OK")
    void updateJobStatus_Returns200() throws Exception {
        JobResponseDto updated = JobResponseDto.builder()
                .id(5L)
                .status(JobStatus.ACCEPTED)
                .build();

        when(jobRequestService.updateJobStatus(eq(5L), eq(JobStatus.ACCEPTED), any())).thenReturn(updated);

        mockMvc.perform(put("/api/job-requests/5/status")
                        .param("status", "ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }
}
