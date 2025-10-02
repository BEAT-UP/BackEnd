package com.BeatUp.BackEnd.User.controller;


import com.BeatUp.BackEnd.User.entity.UserAccount;
import com.BeatUp.BackEnd.User.entity.UserProfile;
import com.BeatUp.BackEnd.User.repository.UserProfileRepository;
import com.BeatUp.BackEnd.User.service.FirebaseAuthService;
import com.BeatUp.BackEnd.common.dto.ApiResponse;
import com.BeatUp.BackEnd.common.enums.ErrorCode;
import com.BeatUp.BackEnd.common.exception.BusinessException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(FirebaseAuthService firebaseAuthService,
                          UserProfileRepository userProfileRepository){
        this.firebaseAuthService = firebaseAuthService;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Firebase 로그인(JWT 발급 없이 사용자 정보만 반환)
     * @param request
     * @return
     */
    @PostMapping("/firebase-login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> firebaselogin(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");

        if (idToken == null || idToken.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "idToken이 필요합니다.");
        }

        try {
            // Firebase ID 토큰 검증
            FirebaseToken decodedToken = firebaseAuthService.verifyIdToken(idToken);

            // 사용자 찾기 또는 생성
            UserAccount user = firebaseAuthService.findOrCreateUser(decodedToken);

            // 프로필 완성도 확인
            UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
            boolean profileCompleted = profile != null && profile.isProfileCompleted();

            Map<String, Object> userData = Map.of(
                    "userId", user.getId(),
                    "email", user.getEmail() != null ? user.getEmail() : "",
                    "displayName", user.getDisplayName() != null ? user.getDisplayName() : "",
                    "provider", user.getProvider().name(),
                    "profileCompleted", profileCompleted
            );

            logger.info("Firebase 로그인 성공 - userId: {}, email: {}", user.getId(), user.getEmail());
            return  ResponseEntity.ok(ApiResponse.success(userData, "로그인 성공"));
        } catch (FirebaseAuthException e) {
            logger.warn("Firebase 인증 실패 - error: {}", e.getMessage());

            // Firebase 에러 타입별로 구분
            if(e.getMessage().contains("expired")){
                throw new BusinessException(ErrorCode.FIREBASE_TOKEN_EXPIRED, "firebase 토큰이 필요합니다.");
            }else if(e.getMessage().contains("invalid")){
                throw new BusinessException(ErrorCode.FIREBASE_TOKEN_INVALID, "유효하지 않은 Firebase 토큰입니다");
            }else{
                throw new BusinessException(ErrorCode.FIREBASE_AUTH_FAILED, "Firebase 인증에 실패했습니다");
            }
        } catch (BusinessException e) {
            // BusinessException은 그대로 재throw
            throw e;
        }catch(Exception e){
            logger.error("Firebase 로그인 중 예상치 못한 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
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
