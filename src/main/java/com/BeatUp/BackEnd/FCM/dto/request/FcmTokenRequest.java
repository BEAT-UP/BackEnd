package com.BeatUp.BackEnd.FCM.dto.request;


import com.BeatUp.BackEnd.FCM.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FcmTokenRequest {

    @NotBlank(message = "FCM 토큰은 필수입니다")
    private String fcmToken;

    @NotNull(message = "디바이스 타입은 필수입니다")
    private DeviceType deviceType;

    private String deviceId;
}
