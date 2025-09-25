package com.BeatUp.BackEnd.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common Errors
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid Input Value"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "Internal Server Error"),

    // Resource Related
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "Resource not found"),

    // User Related
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "Email already exists"),

    // Firebase Related(Authorization)
    FIREBASE_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "F001", "Firebase 인증에 실패했습니다"),
    FIREBASE_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "F002", "유효하지 않은 Firebase 토큰입니다"),
    FIREBASE_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "F003", "Firebase 토큰이 만료되었습니다"),

    // Firebase UID Conflict
    FIREBASE_UID_ALREADY_LINKED(HttpStatus.CONFLICT, "F004", "이미 다른 계정에 연결된 Firebase UID입니다"),

    // Chat Related
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "Chat room not found"),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CH002", "Chat message not found"),

    // Concert Related
    CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "CO001", "Concert not found"),

    // Post Related
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "Post not found"),

    // RideRequest Related
    RIDE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "RR001", "Ride request not found"),
    DUPLICATE_RIDE_REQUEST(HttpStatus.CONFLICT, "RR002", "Duplicate ride request"),

    // KAKAOMAP API Related
    KAKAO_API_ERROR(HttpStatus.BAD_GATEWAY, "KM001", "카카오맵 서비스 연동 중 오류가 발생했습니다"),
    KAKAO_API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "KM002", "카카오맵 API 응답 시간이 초과되었습니다."),
    KAKAO_API_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "KM003", "카카오맵 API 호출 제한을 초과했습니다."),
    KAKAO_API_BAD_REQUEST(HttpStatus.BAD_REQUEST, "KM004", "카카오맵 API 요청이 올바르지 않습니다."),
    KAKAO_API_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "KM005", "카카오맵 서버에서 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
