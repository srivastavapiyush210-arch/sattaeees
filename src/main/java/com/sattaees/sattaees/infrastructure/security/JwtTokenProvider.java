package com.sattaees.sattaees.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT provider for generating, signing, and parsing access and refresh tokens.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;

    public JwtTokenProvider(
            @Value("${jwt.secret:dGhpc2lzYXN1cGVyc2VjcmV0a2V5dGhhdG5lZWRzdG9iZWF0bGVhc3QyNTZiaXRzbG9uZ2ZvcmhtYWNzaGEyNTYxMjM0NQ==}") String secret,
            @Value("${jwt.access-token-expiration:86400000}") long accessTokenExpirationMillis,
            @Value("${jwt.refresh-token-expiration:604800000}") long refreshTokenExpirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
    }

    public String generateAccessToken(Long userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("role", role.toUpperCase());
        claims.put("type", "ACCESS");
        return buildToken(claims, email, accessTokenExpirationMillis);
    }

    public String generateRefreshToken(Long userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("role", role.toUpperCase());
        claims.put("type", "REFRESH");
        return buildToken(claims, email, refreshTokenExpirationMillis);
    }

    private String buildToken(Map<String, Object> claims, String subject, long expirationMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Number id = extractAllClaims(token).get("id", Number.class);
        return id != null ? id.longValue() : null;
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public long getAccessTokenExpirationMillis() {
        return accessTokenExpirationMillis;
    }

    public long getRefreshTokenExpirationMillis() {
        return refreshTokenExpirationMillis;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
