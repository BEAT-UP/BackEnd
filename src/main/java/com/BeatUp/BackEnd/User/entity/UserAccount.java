package com.BeatUp.BackEnd.User.entity;


import com.BeatUp.BackEnd.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "user_account")
public class UserAccount extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String provider = "LOCAL"; // 가입 방식 (LOCAL, GOOGLE, KAKAO 등)

    protected UserAccount(){}

    public UserAccount(String email){
        this.email = email;
    }

    // 디버깅용
    @Override
    public String toString(){
        return "UserAccount{id=" + getId() + "email=" + email + "}";
    }

}
