package com.BeatUp.BackEnd.User.entity;


import com.BeatUp.BackEnd.common.entity.BaseEntity;
import com.BeatUp.BackEnd.common.enums.AuthProvider;
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

    @Column(unique = true)
    private String firebaseUid;

    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL; // 가입 방식 (LOCAL, GOOGLE, KAKAO 등)

    protected UserAccount(){}

    public UserAccount(String email){
        this.email = email;
    }

    public void linkFirebase(String firebaseUid, String displayName, AuthProvider provider){
        this.firebaseUid = firebaseUid;
        this.displayName = displayName;
        this.provider = provider;
    }

    // 디버깅용
    @Override
    public String toString(){
        return "UserAccount{id=" + getId() + "email=" + email + ", provider=" + provider + "}";
    }

}
