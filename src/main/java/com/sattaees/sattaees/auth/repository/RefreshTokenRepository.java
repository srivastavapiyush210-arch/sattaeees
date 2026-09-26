package com.sattaees.sattaees.auth.repository;

import com.sattaees.sattaees.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.userEmail = :userEmail")
    void revokeAllByUserEmail(@Param("userEmail") String userEmail);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.userEmail = :userEmail")
    void deleteByUserEmail(@Param("userEmail") String userEmail);
}
