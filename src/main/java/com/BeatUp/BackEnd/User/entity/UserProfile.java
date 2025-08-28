package com.BeatUp.BackEnd.User.entity;


import com.BeatUp.BackEnd.common.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.UNSPECIFIED;

    @Column(name = "age")
    private Integer age;

    @Column(name = "profile_completed")
    private boolean profileCompleted = false;

    // 기본 생성자
    protected UserProfile(){};

    // 새 프로필 생성용 생성자
    public UserProfile(UUID userId){
        this.userId = userId;
    }

    // 프로필 업데이트 메서드
    public void updateProfile(String nickname, Gender gender, Integer age){
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;

        // 프로필 완성 여부 자동 계산
        this.profileCompleted = isValidProfile();
    }

    // 프로필 완성 여부 확인 메서드
    private boolean isValidProfile(){
        return nickname != null && !nickname.trim().isEmpty()
                && gender != Gender.UNSPECIFIED
                && age != null;
    }

    @Override
    public String toString(){
        return "UserProfile{userId=" + userId + ", nickname=" + nickname +
                ", completed=" + profileCompleted + "}";
    }
}
