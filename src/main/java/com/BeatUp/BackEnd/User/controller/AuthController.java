package com.BeatUp.BackEnd.User.controller;


import com.BeatUp.BackEnd.User.entity.UserAccount;
import com.BeatUp.BackEnd.User.entity.UserProfile;
import com.BeatUp.BackEnd.User.repository.UserAccountRepository;
import com.BeatUp.BackEnd.User.repository.UserProfileRepository;
import com.BeatUp.BackEnd.User.service.FirebaseAuthService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final FirebaseAuthService firebaseAuthService;
    private final UserProfileRepository userProfileRepository;
    private final UserAccountRepository userAccountRepository;

    public AuthController(FirebaseAuthService firebaseAuthService,
                          UserProfileRepository userProfileRepository,
                          UserAccountRepository userAccountRepository){
        this.firebaseAuthService = firebaseAuthService;
        this.userProfileRepository = userProfileRepository;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Firebase 로그인(JWT 발급 없이 사용자 정보만 반환)
     * @param request
     * @return
     */
    @PostMapping("/firebase-login")
    public ResponseEntity<Map<String, Object>> firebaselogin(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");

        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "idToken이 필요합니다."));
        }

        try {
            // Firebase ID 토큰 검증
            FirebaseToken decodedToken = firebaseAuthService.verifyIdToken(idToken);

            // 사용자 찾기 또는 생성
            UserAccount user = firebaseAuthService.findOrCreateUser(decodedToken);

            // 프로필 완성도 확인
            UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
            boolean profileCompleted = profile != null && profile.isProfileCompleted();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "profileCompleted", profileCompleted,
                    "user", Map.of(
                            "id", user.getId(),
                            "email", user.getEmail() != null ? user.getEmail() : "",
                            "displayName", user.getDisplayName() != null ? user.getDisplayName() : "",
                            "provider", user.getProvider().name()
                    )
            ));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401)
                    .body(Map.of(
                            "error", "Firebase 인증 실패",
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "error", "서버 오류",
                            "message", e.getMessage()
                    ));
        }
    }

    // 기존 /login 엔드포인트는 유지(레거시 지원)
//    @PostMapping("/login")
//    public Map<String, Object> login(@RequestBody Map<String, String> request){
//        String email = request.get("email");
//
//        UserAccount user = userAccountRepository.findByEmail(email)
//                .orElseGet(() -> {
//                    UserAccount newUser = new UserAccount(email);
//                    userAccountRepository.save(newUser);
//
//                    UserProfile newProfile = new UserProfile(newUser.getId());
//                    userProfileRepository.save(newProfile);
//
//                    return newUser;
//                });
//
//        String token = jwtService.generateToken(user.getId(), user.getEmail());
//
//        return Map.of(
//                "accessToken", token,
//                "expiresIn", jwtService.getExpirationSeconds()
//        );
//    }

}
