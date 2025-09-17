package com.BeatUp.BackEnd.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 표준화된 API 응답 래퍼 클래스
 * 모든 API 응답을 일관된 형식으로 제공
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    /**
     * 응답 성공 여부
     */
    private boolean success;
    
    /**
     * HTTP 상태 코드
     */
    private int status;
    
    /**
     * 응답 메시지
     */
    private String message;
    
    /**
     * 실제 데이터
     */
    private T data;
    
    /**
     * 에러 정보 (실패 시에만 포함)
     */
    private ErrorInfo error;
    
    /**
     * 응답 생성 시간
     */
    private LocalDateTime timestamp;
    
    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message("Success")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 성공 응답 생성 (커스텀 메시지)
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 성공 응답 생성 (커스텀 상태 코드)
     */
    public static <T> ApiResponse<T> success(T data, int status, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(ErrorInfo error) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(error.getStatus())
                .message(error.getMessage())
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 실패 응답 생성 (커스텀 메시지)
     */
    public static <T> ApiResponse<T> error(ErrorInfo error, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(error.getStatus())
                .message(message)
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 실패 응답 생성 (간단한 에러 정보)
     */
    public static <T> ApiResponse<T> error(int status, String code, String message) {
        ErrorInfo errorInfo = ErrorInfo.builder()
                .status(status)
                .code(code)
                .message(message)
                .build();
        
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .error(errorInfo)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
