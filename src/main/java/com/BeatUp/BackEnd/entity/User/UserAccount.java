package com.BeatUp.BackEnd.entity.User;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_account")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String provider = "LOCAL"; // 가입 방식 (LOCAL, GOOGLE, KAKAO 등)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected UserAccount(){}

    public UserAccount(String email){
        this.email = email;
    }

    // Getter 메서드들(외부에서 값을 얻을 때 사용)
    public UUID getId(){return id;}
    public String getEmail(){return email;}
    public String getProvider() {return provider;}
    public LocalDateTime getCreatedAt() {return createdAt;}

    // 디버깅용
    @Override
    public String toString(){
        return "UserAccount{id=" + id + "email=" + email + "}";
    }

}
