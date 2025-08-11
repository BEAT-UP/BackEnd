package com.BeatUp.BackEnd.controller;


import com.BeatUp.BackEnd.entity.UserProfile;
import com.BeatUp.BackEnd.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        return Map.of(
                "userId", profile.getUserId(),
                "nickname", profile.getNickname() != null ? profile.getNickname() : "",
                "gender", profile.getGender().name(),
                "age", profile.getAge() != null ? profile.getAge(): 0,
                "profileCompleted", profile.isProfileCompleted()
        );
    }

    private UUID getCurrentUserId(){
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
