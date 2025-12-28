package com.BeatUp.BackEnd.FCM.service;


import com.BeatUp.BackEnd.FCM.dto.request.FcmTokenRequest;
import com.BeatUp.BackEnd.FCM.dto.response.FcmTokenResponse;
import com.BeatUp.BackEnd.FCM.entity.FcmToken;
import com.BeatUp.BackEnd.FCM.repository.FcmTokenRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    /**
     * FCM 토큰 등록 또는 업데이트
     */
    @Transactional
    public FcmTokenResponse saveOrUpdateToken(UUID userId, FcmTokenRequest request){
        // 기존 토큰 확인
        Optional<FcmToken> existing = fcmTokenRepository.findByUserIdAndFcmToken(userId, request.getFcmToken());

        FcmToken token;
        if(existing.isPresent()){
            // 기존 토큰 업데이트
            token = existing.get();
            token.updateToken(request.getFcmToken());
            if(request.getDeviceId() != null){
                token.setDeviceId(request.getDeviceId());
            }
            log.debug("FCM 토큰 업데이트 - userId: {}, tokenId: {}", userId, token.getId());
        }else{
            // 새 토큰 생성
            token = new FcmToken(userId, request.getFcmToken(), request.getDeviceType());
            if(request.getDeviceId() != null){
                token.setDeviceId(request.getDeviceId());
            }
            log.debug("FCM 토큰 등록 - userId: {}, deviceType: {}", userId, request.getDeviceType());
        }

        FcmToken saved = fcmTokenRepository.save(token);
        return mapToResponse(saved);
    }

    /**
     * 사용자의 활성 토큰 조회
     */
    public List<FcmTokenResponse> getActiveTokens(UUID userId){
        List<FcmToken> tokens = fcmTokenRepository.findByUserIdAndIsActiveTrue(userId);
        return tokens.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * FCM 토큰 삭제(비활성화)
     */
    @Transactional
    public void deleteToken(UUID userId, String fcmToken){
        fcmTokenRepository.findByUserIdAndFcmToken(userId, fcmToken)
                .ifPresentOrElse(
                        token -> {
                            token.deactivate();
                            fcmTokenRepository.save(token);
                            log.debug("FCM 토큰 비활성화 - userId: {}, tokenId: {}", userId, token.getId());
                        },
                        () -> log.warn("삭제할 FCM 토큰을 찾을 수 없음 - userId: {}, token: {}", userId, fcmToken)
                );
    }


    /**
     * 사용자의 모든 토큰 삭제(로그아웃 시)
     */
    @Transactional
    public void deleteAllTokens(UUID userId){
        List<FcmToken> tokens = fcmTokenRepository.findByUserId(userId);
        tokens.forEach(FcmToken::deactivate);
        fcmTokenRepository.saveAll(tokens);
        log.debug("사용자 모든 FCM 토큰 비활성화 - userId: {}", userId);
    }

    private FcmTokenResponse mapToResponse(FcmToken token){
        return FcmTokenResponse.builder()
                .id(token.getId())
                .userId(token.getUserId())
                .deviceType(token.getDeviceType())
                .deviceId(token.getDeviceId())
                .isActive(token.isActive())
                .lastUsedAt(token.getLastUsedAt())
                .createdAt(token.getCreatedAt())
                .updatedAt(token.getUpdatedAt())
                .build();
    }

}
