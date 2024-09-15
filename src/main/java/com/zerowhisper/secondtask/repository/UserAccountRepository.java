package com.zerowhisper.secondtask.repository;

import com.zerowhisper.secondtask.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    //u = *
    @Query("SELECT u FROM UserAccount u WHERE u.username = ?1")
    Optional<UserAccount> findByUsername(String username);

    @Query("SELECT u FROM UserAccount u WHERE u.email = ?1")
    Optional<UserAccount> findByEmail(String email);
}
