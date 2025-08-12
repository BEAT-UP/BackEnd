package com.BeatUp.BackEnd.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다")
    private String nickname;

    private String gender; // "MALE", "FEMALE", "UNSPECIFIED"

    @Min(value = 10, message = "최소 나이는 10세입니다")
    @Max(value = 80, message = "최대 나이는 80세입니다")
    private Integer age;

    // 기본 생성자
    public ProfileUpdateRequest(){}

    // Getter & Setter
    public String getNickname(){
        return nickname;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public String getGender(){
        return gender;
    }

    public void setGender(String gender){
        this.gender = gender;
    }

    public Integer getAge(){
        return age;
    }

    public void setAge(Integer age){
        this.age = age;
    }

}
