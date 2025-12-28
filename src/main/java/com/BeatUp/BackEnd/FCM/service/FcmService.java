package com.BeatUp.BackEnd.FCM.service;

import com.BeatUp.BackEnd.FCM.entity.FcmToken;
import com.BeatUp.BackEnd.FCM.repository.FcmTokenRepository;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;
    private final FirebaseMessaging firebaseMessaging;

    /**
     * 단일 사용자에게 FCM 알림 전송
     */
    public void sendNotification(UUID userId, String title, String body, Map<String, String> data){
        List<FcmToken> tokens = fcmTokenRepository.findByUserIdAndIsActiveTrue(userId);

        if(tokens.isEmpty()){
            log.debug("FCM 토큰 없음 - userId: {}", userId);
            return;
        }

        List<String> tokenStrings = tokens.stream()
                .map(FcmToken::getFcmToken)
                .collect(Collectors.toList());


    }

    /**
     * 실패한 토큰 처리(무효한 토큰 비활성화)
     */
    private void handleFailedTokens(BatchResponse response, List<String> tokens, UUID userId){
        List<SendResponse> responses = response.getResponses();

        for(int i = 0; i < responses.size(); i++){
            SendResponse sendResponse = responses.get(i);
            if(!sendResponse.isSuccessful()){
                String failedToken = tokens.get(i);
                FirebaseMessagingException exception = sendResponse.getException();

                if(exception != null){
                    // 무효한 토큰인 경우 비활성화
                    if(isInvalidToken(exception)){
                        deactivateToken(failedToken, userId);
                        log.warn("무효한 FCM 토큰 비활성화 - userId: {}, token: {}", userId, failedToken);
                    }else{
                        log.warn("FCM 알림 전송 실패 - userId: {}, error: {}", userId, exception.getMessage());
                    }
                }
            }
        }
    }

    // 무효한 토큰인지 확인
    private boolean isInvalidToken(FirebaseMessagingException e){
        return e.getErrorCode() != null && (
                e.getErrorCode().equals("invalid-argument") ||
                        e.getErrorCode().equals("registration-token-not-expired") ||
                        e.getErrorCode().equals("unregistered")
                );
    }

    /**
     * 성공한 토큰의 lastUsedAt 업데이트
     */
    private void updateSuccessfulTokens(BatchResponse response, List<String> tokens, UUID userId) {
        List<SendResponse> responses = response.getResponses();

        for (int i = 0; i < responses.size(); i++) {
            if (responses.get(i).isSuccessful()) {
                String token = tokens.get(i);
                fcmTokenRepository.findByUserIdAndFcmToken(userId, token)
                        .ifPresent(t -> {
                            t.markAsUsed();
                            fcmTokenRepository.save(t);
                        });
            }
        }
    }


    /**
     * 토큰 비활성화
     */
    private void deactivateToken(String fcmToken, UUID userId){
        fcmTokenRepository.findByUserIdAndFcmToken(userId, fcmToken)
                .ifPresent(token -> {
                    token.deactivate();
                    fcmTokenRepository.save(token);
                });
    }
}
