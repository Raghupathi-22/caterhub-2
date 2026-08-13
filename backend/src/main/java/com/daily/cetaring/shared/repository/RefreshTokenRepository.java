package com.daily.cetaring.shared.repository;

import com.daily.cetaring.shared.entity.RefreshToken;
import com.daily.cetaring.shared.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    void deleteByUserAndRevokedTrue(User user);

    void deleteByExpiresAtBefore(LocalDateTime expiresAt);

    @Modifying
    @Query("update RefreshToken rt set rt.revoked = true, rt.revokedAt = :revokedAt where rt.user = :user and rt.revoked = false")
    int revokeAllActiveTokensForUser(@Param("user") User user, @Param("revokedAt") LocalDateTime revokedAt);
}
