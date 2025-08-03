package com.BeatUp.BackEnd.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "user_seq_generator",   // generator 이름
        sequenceName = "user_seq",     // 실제 DB 시퀀스 이름
        initialValue = 1,
        allocationSize = 1
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "phone_number", length = 20, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public User(String passwordHash, String email, String phoneNumber, UserStatus status){
        this.passwordHash = passwordHash;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status != null ? status : UserStatus.ACTIVE;
    }


}
