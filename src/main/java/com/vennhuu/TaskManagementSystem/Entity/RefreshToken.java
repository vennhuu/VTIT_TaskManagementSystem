package com.vennhuu.TaskManagementSystem.Entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name="refresh_token",
    indexes = {
        @Index(name="idx_refresh_token", columnList="token")
    }
)
@Getter
@Setter
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id ;

    private String device ;

    @Column(columnDefinition = "TEXT")
    private String token ;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    private Instant createdAt;

    private Instant expiredAt;

    private boolean revoked = false;

    private Instant revokedAt;
}
