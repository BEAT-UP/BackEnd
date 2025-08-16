package com.BeatUp.BackEnd.controller;


import com.BeatUp.BackEnd.entity.User.UserAccount;
import com.BeatUp.BackEnd.entity.User.UserProfile;
import com.BeatUp.BackEnd.repository.User.UserAccountRepository;
import com.BeatUp.BackEnd.repository.User.UserProfileRepository;
import com.BeatUp.BackEnd.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request){
        String email = request.get("email");

        // 사용자 찾기 또는 생성
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserAccount newUser = new UserAccount(email);
                    userAccountRepository.save(newUser);

                    // 기본 프로필도 생성
                    UserProfile newProfile = new UserProfile(newUser.getId());
                    userProfileRepository.save(newProfile);

                    return newUser;
                });

        // JWT 토큰 생성
        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return Map.of(
                "accessToken", token,
                "expiresIn", jwtService.getExpirationSeconds()
        );
    }
}
