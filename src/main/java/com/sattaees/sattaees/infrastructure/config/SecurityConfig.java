package com.sattaees.sattaees.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sattaees.sattaees.common.dto.ErrorResponse;
import com.sattaees.sattaees.common.filter.TraceIdFilter;
import com.sattaees.sattaees.infrastructure.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ObjectMapper objectMapper,
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:3000,http://localhost:80,http://localhost}") String allowedOriginsStr) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
        this.allowedOrigins = Arrays.stream(allowedOriginsStr.split(","))
                .map(String::trim)
                .toList();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) // For H2 Console
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(unauthorizedEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            )
            .authorizeHttpRequests(auth -> auth
                // Public authentication endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/customers", "/api/customers/login", "/api/customers/signup").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/workers", "/api/workers/login", "/api/workers/signup").permitAll()
                
                // Public catalog/search endpoints (read-only)
                .requestMatchers(HttpMethod.GET, "/api/workers", "/api/workers/*", "/api/workers/search", "/api/workers/search-by-city", "/api/workers/paged").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews/worker/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/hello").permitAll()

                // API Documentation & Swagger UI
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // Observability & Health endpoints
                .requestMatchers("/actuator/**").permitAll()

                // Local H2 console
                .requestMatchers("/h2-console/**").permitAll()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Trace-ID", "X-Correlation-ID"));
        configuration.setExposedHeaders(List.of("X-Trace-ID", "X-Correlation-ID"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            String traceId = MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY);
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error("Unauthorized")
                    .message("Authentication required to access this resource.")
                    .path(request.getRequestURI())
                    .traceId(traceId)
                    .build();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), error);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            String traceId = MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY);
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.FORBIDDEN.value())
                    .error("Forbidden")
                    .message("You lack sufficient permissions to perform this operation.")
                    .path(request.getRequestURI())
                    .traceId(traceId)
                    .build();

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), error);
        };
    }
}
