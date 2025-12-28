package com.BeatUp.BackEnd.FCM.entity;


import com.BeatUp.BackEnd.FCM.enums.DeviceType;
import com.BeatUp.BackEnd.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "fcm_token",
        uniqueConstraints = @UniqueConstraint(
        name = "uq_fcm_token_user_token",
        columnNames = {"user_id", "fcm_token"})
)
@NoArgsConstructor
public class FcmToken extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "fcm_token", nullable = false, length = 255)
    private String fcmToken;

    @Column(name = "device_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    // 생성자
    public FcmToken(UUID userId, String fcmToken, DeviceType deviceType) {
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
        this.isActive = true;
    }

    public FcmToken(UUID userId, String fcmToken, DeviceType deviceType, String deviceId) {
        this(userId, fcmToken, deviceType);
        this.deviceId = deviceId;
    }

    // 비즈니스 메서드
    public void updateToken(String newToken) {
        this.fcmToken = newToken;
        this.isActive = true;
        this.lastUsedAt = null;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void markAsUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}
