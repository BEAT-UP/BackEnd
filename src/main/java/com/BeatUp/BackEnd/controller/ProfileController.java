package com.BeatUp.BackEnd.controller;


import com.BeatUp.BackEnd.dto.request.ProfileUpdateRequest;
import com.BeatUp.BackEnd.entity.User.UserProfile;
import com.BeatUp.BackEnd.repository.User.UserProfileRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/profile")
public class ProfileController {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @GetMapping("/me")
    public Map<String, Object> getMyProfile(){
        UUID userId = getCurrentUserId();

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return createProfileResponse(profile);
    }

    @PatchMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMyProfile(@Valid @RequestBody ProfileUpdateRequest request){
        UUID userId = getCurrentUserId();

        // 1. 기존 프로필 조회
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        // 2. 나이 검증(비즈니스 로직)
        if(request.getAge() != null && (request.getAge() <= 10 || request.getAge() >= 80)) {
            throw new IllegalArgumentException("최소 나이는 10세, 최대 나이는 80세입니다.");
        }

        // 3. Gender enum 변환
        UserProfile.Gender gender = UserProfile.Gender.UNSPECIFIED;
        if(request.getGender() != null && !request.getGender().trim().isEmpty()){
            try{
                gender = UserProfile.Gender.valueOf(request.getGender().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("MALE, FEMALE 중 하나를 입력하세요");
            }
        }

        // 4. 프로필 업데이트
        profile.updateProfile(
                request.getNickname(),
                gender,
                request.getAge()
        );

        // 5. 저장
        userProfileRepository.save(profile);

        // 6. 응답 생성
        return ResponseEntity.ok(createProfileResponse(profile));
    }

    private UUID getCurrentUserId(){
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
