package com.BeatUp.BackEnd.FCM.dto.response;

import com.BeatUp.BackEnd.FCM.enums.DeviceType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class FcmTokenResponse {
    private UUID id;
    private UUID userId;
    private DeviceType deviceType;
    private String deviceId;
    private boolean isActive;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
