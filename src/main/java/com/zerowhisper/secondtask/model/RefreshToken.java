package com.zerowhisper.secondtask.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@Table(name = "refresh_Token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;


    // Why we need ManyToOne ? because maybe more than one device might access the same account
    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private UserAccount user;
    @Column(nullable = false, updatable = false)
    private String refreshToken;
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(nullable = false)
    private Boolean isLoggedIn;
    @Column(nullable = false)
    private Timestamp updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Timestamp.from(Instant.now());
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Timestamp.from(Instant.now());
        updatedAt = createdAt;
        isLoggedIn = true;
    }

}
