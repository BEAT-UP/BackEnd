package com.BeatUp.BackEnd.controller;

import com.BeatUp.BackEnd.entity.UserAccount;
import com.BeatUp.BackEnd.entity.UserProfile;
import com.BeatUp.BackEnd.repository.UserAccountRepository;
import com.BeatUp.BackEnd.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class HealthController {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @GetMapping("/ping")
    public Map<String, Object> ping(){
        return Map.of(
                "service", "auth-service",
                "status", "ok",
                "timestamp", System.currentTimeMillis()
        );
    }

    @GetMapping("test/create-user")
    public Map<String, Object> createTestUser(){
        // 매번 고유한 이메일 생성(중복 방지)
        String uniqueEmail = "test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

        // 1. 새 사용자 계정 생성
        UserAccount account = new UserAccount("test@example.com");
        userAccountRepository.save(account);

        // 2. 해당 사용자의 프로필 생성
        UserProfile profile = new UserProfile(account.getId());
        profile.updateProfile("테스트유저", UserProfile.Gender.FEMALE, 20);
        userProfileRepository.save(profile);

        return Map.of(
                "message","테스트 생성 완료",
                "email", uniqueEmail,
                "accountId", account.getId().toString(),
                "profileCompleted", profile.isProfileCompleted()
        );
    }

    @GetMapping("test/list-users")
    public Map<String, Object> listUsers(){
        List<UserAccount> accounts = userAccountRepository.findAll();
        List<UserProfile> profiles = userProfileRepository.findAll();

        return Map.of(
                "accounts", accounts.size(),
                "profiles", profiles.size(),
                "accountList", accounts,
                "profileList", profiles
        );
    }
}
