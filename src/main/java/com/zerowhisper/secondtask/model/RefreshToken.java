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
    /*
    Understanding Cascade Types in @ManyToOne
    - CascadeType.ALL: Propagates all operations (save, update, delete) to associated entities. You generally do not want this when deleting one row without affecting related rows.
    - CascadeType.REMOVE: Deletes the associated entities when the owning entity is deleted. You do not want this if you don’t want to delete associated entities.
    - CascadeType.PERSIST: Propagates the save operation to associated entities.
    - CascadeType.MERGE: Propagates the merge (update) operation.
    - CascadeType.REFRESH: Refreshes the associated entity.
    - CascadeType.DETACH: Detaches the associated entity.
     */
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_account", nullable = false) // ??????????
    private UserAccount userAccount;
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
