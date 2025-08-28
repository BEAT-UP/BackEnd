package com.BeatUp.BackEnd.common.exception;

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

    // Chat Related
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "Chat room not found"),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CH002", "Chat message not found"),

    // Concert Related
    CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "CO001", "Concert not found"),

    // Post Related
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "Post not found"),

    // RideRequest Related
    RIDE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "RR001", "Ride request not found"),
    DUPLICATE_RIDE_REQUEST(HttpStatus.CONFLICT, "RR002", "Duplicate ride request");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
