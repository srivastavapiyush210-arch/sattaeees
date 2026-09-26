package com.sattaees.sattaees.auth.service;

import com.sattaees.sattaees.auth.dto.AuthResponse;
import com.sattaees.sattaees.auth.dto.LoginRequest;
import com.sattaees.sattaees.auth.dto.RefreshTokenRequest;
import com.sattaees.sattaees.auth.entity.RefreshToken;
import com.sattaees.sattaees.common.exception.UnauthorizedException;
import com.sattaees.sattaees.customer.entity.Customer;
import com.sattaees.sattaees.customer.repository.CustomerRepository;
import com.sattaees.sattaees.infrastructure.security.JwtTokenProvider;
import com.sattaees.sattaees.worker.entity.Worker;
import com.sattaees.sattaees.worker.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private Customer sampleCustomer;
    private Worker sampleWorker;
    private RefreshToken sampleRefreshToken;

    @BeforeEach
    void setUp() {
        sampleCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@customer.com")
                .password("hashed_secret")
                .phoneNumber("+91 9999999999")
                .address("Delhi")
                .build();

        sampleWorker = Worker.builder()
                .id(2L)
                .name("Test Worker")
                .email("test@worker.com")
                .password("hashed_secret")
                .phoneNumber("+91 8888888888")
                .skill("Plumber")
                .city("Delhi")
                .build();

        sampleRefreshToken = RefreshToken.builder()
                .id(100L)
                .token("mock-refresh-token")
                .userEmail("test@customer.com")
                .userRole("CUSTOMER")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
    }

    @Test
    @DisplayName("Should successfully login customer with valid credentials")
    void loginCustomer_ValidCredentials_Success() {
        LoginRequest req = LoginRequest.builder()
                .email("test@customer.com")
                .password("secret123")
                .build();

        when(customerRepository.findByEmail("test@customer.com")).thenReturn(Optional.of(sampleCustomer));
        when(passwordEncoder.matches("secret123", "hashed_secret")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(1L, "test@customer.com", "CUSTOMER")).thenReturn("access-token-xyz");
        when(refreshTokenService.createRefreshToken(1L, "test@customer.com", "CUSTOMER")).thenReturn(sampleRefreshToken);
        when(jwtTokenProvider.getAccessTokenExpirationMillis()).thenReturn(86400000L);

        AuthResponse response = authService.loginCustomer(req);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("access-token-xyz");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("test@customer.com");
        assertThat(response.getUser().getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("Should throw BadCredentialsException on invalid customer password")
    void loginCustomer_WrongPassword_ThrowsBadCredentials() {
        LoginRequest req = LoginRequest.builder()
                .email("test@customer.com")
                .password("wrongpassword")
                .build();

        when(customerRepository.findByEmail("test@customer.com")).thenReturn(Optional.of(sampleCustomer));
        when(passwordEncoder.matches("wrongpassword", "hashed_secret")).thenReturn(false);

        assertThatThrownBy(() -> authService.loginCustomer(req))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when email not found")
    void loginCustomer_EmailNotFound_ThrowsBadCredentials() {
        LoginRequest req = LoginRequest.builder()
                .email("unknown@customer.com")
                .password("secret123")
                .build();

        when(customerRepository.findByEmail("unknown@customer.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loginCustomer(req))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Should exchange valid refresh token for new access token")
    void refreshToken_Valid_Success() {
        RefreshTokenRequest req = RefreshTokenRequest.builder()
                .refreshToken("mock-refresh-token")
                .build();

        when(refreshTokenService.findByToken("mock-refresh-token")).thenReturn(Optional.of(sampleRefreshToken));
        when(refreshTokenService.verifyExpiration(sampleRefreshToken)).thenReturn(sampleRefreshToken);
        when(jwtTokenProvider.extractUserId("mock-refresh-token")).thenReturn(1L);
        when(customerRepository.findByEmail("test@customer.com")).thenReturn(Optional.of(sampleCustomer));
        when(jwtTokenProvider.generateAccessToken(1L, "test@customer.com", "CUSTOMER")).thenReturn("new-access-token");
        when(jwtTokenProvider.getAccessTokenExpirationMillis()).thenReturn(86400000L);

        AuthResponse response = authService.refreshToken(req);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getUser().getEmail()).isEqualTo("test@customer.com");
    }

    @Test
    @DisplayName("Should call revoke on logout")
    void logout_Success() {
        authService.logout("test@customer.com");
        verify(refreshTokenService).revokeAllForUser("test@customer.com");
    }
}
