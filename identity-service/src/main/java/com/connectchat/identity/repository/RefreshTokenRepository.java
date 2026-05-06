package com.connectchat.identity.repository;

import com.connectchat.identity.entity.RefreshToken;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface RefreshTokenRepository
    extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshToken token where token.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(
        @Param("tokenHash") String tokenHash
    );

    List<RefreshToken> findAllByUserIdAndRevokedFalseAndExpiresAtAfter(
        UUID userId,
        Instant expiresAt
    );
}
