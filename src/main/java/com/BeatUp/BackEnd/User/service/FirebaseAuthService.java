package com.BeatUp.BackEnd.User.service;

import com.BeatUp.BackEnd.User.entity.UserAccount;
import com.BeatUp.BackEnd.User.entity.UserProfile;
import com.BeatUp.BackEnd.User.repository.UserAccountRepository;
import com.BeatUp.BackEnd.User.repository.UserProfileRepository;
import com.BeatUp.BackEnd.common.enums.AuthProvider;
import com.BeatUp.BackEnd.common.enums.ErrorCode;
import com.BeatUp.BackEnd.common.exception.BusinessException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class FirebaseAuthService {

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);

    public FirebaseAuthService(UserAccountRepository userAccountRepository, UserProfileRepository userProfileRepository){
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Firebase ID 토큰 검증
     */
    public FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException{
        return FirebaseAuth.getInstance().verifyIdToken(idToken, true);
    }

    /**
     * Firebase 토큰에서 AuthProvider 결정
     */
    private AuthProvider resolveProvider(FirebaseToken token){
        try{
            Object signInProvider = token.getClaims().get("sign_in_provider");
            if(signInProvider == null)
                return AuthProvider.OIDC;

            String provider = signInProvider.toString().toLowerCase();
            return switch (provider){
                case "google.com" -> AuthProvider.GOOGLE;
                case "oidc.kakao", "kakao.com" -> AuthProvider.KAKAO;
                case "apple.com" -> AuthProvider.APPLE;
                case "password" -> AuthProvider.LOCAL;
                default -> AuthProvider.OIDC;
            };
        } catch (Exception e) {
            return AuthProvider.OIDC;
        }
    }

    /**
     * Firebase 사용자 정보를 기반으로 UserAccount 찾기 또는 생성
     */
    @Transactional
    public UserAccount findOrCreateUser(FirebaseToken token){
        String firebaseUid = token.getUid();
        String email = token.getEmail();
        String displayName = token.getName();
        AuthProvider provider = resolveProvider(token);

        // 1.Firebase UID로 기존 사용자 찾기
        Optional<UserAccount> existingById = userAccountRepository.findByFirebaseUid(firebaseUid);
        if(existingById.isPresent()){
            logger.info("기존 Firebase UID로 사용자 찾음 - userId: {}", existingById.get().getId());
            return existingById.get();
        }

        // 2. 이메일로 기존 사용자 찾기
        if(email != null && !email.isBlank()){
            Optional<UserAccount> existingByEmail = userAccountRepository.findByEmail(email);
            if(existingByEmail.isPresent()){
                UserAccount user = existingByEmail.get();

                // Firebase UID가 이미 다른 사용자에게 연결되어 잇는지 확인
                if(user.getFirebaseUid() != null && !user.getFirebaseUid().equals(firebaseUid)){
                    logger.warn("이미 다른 Firebase UID가 연결된 계정 - userId: {}, existingUid: {}, newUid: {}",
                            user.getId(), user.getFirebaseUid(), firebaseUid);
                    throw new BusinessException(ErrorCode.FIREBASE_UID_ALREADY_LINKED,
                            "이미 다른 Firebase 계정에 연결된 이메일입니다.");
                }

                user.linkFirebase(firebaseUid, displayName, provider);
                return userAccountRepository.save(user);
            }
        }

        // 3. 새 사용자 생성
        UserAccount newUser = new UserAccount(email);
        newUser.linkFirebase(firebaseUid, displayName, provider);
        userAccountRepository.save(newUser);

        // 기본 프로필 생성
        UserProfile profile = new UserProfile(newUser.getId());
        if(displayName != null && !displayName.isBlank()){
            // Firebase displayName를 닉네임으로 사용(명시)
            profile.updateProfile(displayName, null, null);
        }
        userProfileRepository.save(profile);

        return newUser;
    }
}
