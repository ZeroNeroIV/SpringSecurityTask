package com.zerowhisper.secondtask.repository;

import com.zerowhisper.secondtask.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("SELECT r FROM RefreshToken r WHERE r.userAccount.id = ?1")
    List<RefreshToken> findAllByUserId(Long userId);

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM RefreshToken r WHERE r.userAccount.id = ?1")
    void deleteRefreshTokenByRefreshTokenId(Long tokenId);

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM RefreshToken r WHERE r.tokenId = ?1")
    void deleteAllTokensByUserId(Long id);
}
