package com.sattaees.sattaees;

import com.sattaees.sattaees.config.TestConfig;
import com.sattaees.sattaees.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Public endpoint /hello should be accessible without credentials")
    void publicEndpoint_Hello_Returns200() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Public worker catalog /api/workers should be accessible without token")
    void publicEndpoint_WorkerCatalog_Returns200() throws Exception {
        mockMvc.perform(get("/api/workers"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Actuator health endpoint /actuator/health should be accessible")
    void actuatorHealth_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Protected mutating endpoint should return 401 Unauthorized when token is missing")
    void protectedEndpoint_MissingToken_Returns401() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Protected endpoint should permit access when valid Bearer JWT is presented")
    void protectedEndpoint_WithValidToken_PermitsAccess() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, "demo@customer.com", "CUSTOMER");

        mockMvc.perform(get("/api/customers/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("demo@customer.com"));
    }
}
