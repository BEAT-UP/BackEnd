package com.BeatUp.BackEnd.User.controller;

import com.BeatUp.BackEnd.User.dto.request.ProfileUpdateRequest;
import com.BeatUp.BackEnd.User.entity.UserProfile;
import com.BeatUp.BackEnd.User.repository.UserProfileRepository;
import com.BeatUp.BackEnd.common.dto.ApiResponse;
import com.BeatUp.BackEnd.common.enums.ErrorCode;
import com.BeatUp.BackEnd.common.enums.Gender;
import com.BeatUp.BackEnd.common.exception.BusinessException;
import com.BeatUp.BackEnd.common.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/profile")
public class ProfileController {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyProfile(){
        UUID userId = SecurityUtil.getCurrentUserId();

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "프로필을 찾을 수 없습니다"));

        Map<String, Object> profileData = createProfileResponse(profile);
        ApiResponse<Map<String, Object>> response = ApiResponse.success(profileData, "프로필 조회 성공");
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateMyProfile(@Valid @RequestBody ProfileUpdateRequest request){
        UUID userId = SecurityUtil.getCurrentUserId();

        // 1. 기존 프로필 조회
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "프로필을 찾을 수 없습니다"));

        // 2. 닉네임 중복 체크
        if(request.getNickname() != null && !request.getNickname().trim().isEmpty()){
            boolean nicknameExists = userProfileRepository.existsByNicknameAndUserIdNot(
                    request.getNickname().trim(), userId
            );
            if(nicknameExists){
                throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS, "이미 사용중인 닉네임입니다.");
            }
        }

        // 3. Gender enum 변환
        Gender gender = Gender.UNSPECIFIED;
        if(request.getGender() != null && !request.getGender().trim().isEmpty()){
            try{
                gender = Gender.valueOf(request.getGender().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "MALE, FEMALE 중 하나를 입력하세요");
            }
        }

        // 4. 프로필 업데이트
        profile.updateProfile(
                request.getNickname() != null && !request.getNickname().trim().isEmpty() ? request.getNickname().trim() : profile.getNickname(),
                gender != Gender.UNSPECIFIED ? gender : profile.getGender(),
                request.getAge() != null ? request.getAge() : profile.getAge()
        );

        // 5. 저장
        userProfileRepository.save(profile);

        // 6. 응답 생성
        Map<String, Object> profileData = createProfileResponse(profile);
        ApiResponse<Map<String, Object>> response = ApiResponse.success(profileData, "프로필 업데이트 성공");
        
        return ResponseEntity.ok(response);
    }

    // 프로필 응답 생성(중복 코드 제거)
    private Map<String, Object> createProfileResponse(UserProfile profile){
        return Map.of(
                "userId", profile.getUserId(),
                "nickname", profile.getNickname() != null ? profile.getNickname(): "",
                "gender", profile.getGender().name(),
                "age", profile.getAge() != null ? profile.getAge() : 0,
                "profileCompleted", profile.isProfileCompleted()
        );
    }
}
