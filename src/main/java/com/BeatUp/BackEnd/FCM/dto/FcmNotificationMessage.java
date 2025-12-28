package com.BeatUp.BackEnd.FCM.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmNotificationMessage implements Serializable {

    private UUID userId;
    private String title;
    private String body;
    private Map<String, String> data;

    @Builder.Default
    private int retryCount = 0;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // 알림 타입
    private String type; // Chat, Match,,,

    // 우선순위
    @Builder.Default
    private int priority = 0;
}
