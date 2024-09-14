package com.zerowhisper.secondtask.Repository;

import com.zerowhisper.secondtask.model.RefreshToken;
import com.zerowhisper.secondtask.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(UserAccount user);
}
