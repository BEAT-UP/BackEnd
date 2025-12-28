package com.BeatUp.BackEnd.FCM.repository;

import com.BeatUp.BackEnd.FCM.entity.FcmToken;
import com.BeatUp.BackEnd.common.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FcmTokenRepository extends BaseRepository<FcmToken> {

    // 사용자별 활성 토큰 조회
    List<FcmToken> findByUserIdAndIsActiveTrue(UUID userId);

    // 토큰으로 조회 (업데이트 시 사용)
    Optional<FcmToken> findByFcmToken(String fcmToken);

    // 사용자와 토큰으로 조회 (중복 확인)
    Optional<FcmToken> findByUserIdAndFcmToken(UUID userId, String fcmToken);

    // 사용자별 모든 토큰 조회
    List<FcmToken> findByUserId(UUID userId);

    @Override
    default String getEntityName() {
        return "FcmToken";
    }
}
