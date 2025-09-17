package com.BeatUp.BackEnd.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 에러 응답 상세 정보 클래스
 * 클라이언트에게 구체적인 에러 정보를 제공
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorInfo {
    
    /**
     * HTTP 상태 코드
     */
    private int status;
    
    /**
     * 에러 코드 (비즈니스 로직 구분용)
     */
    private String code;
    
    /**
     * 에러 메시지
     */
    private String message;
    
    /**
     * 상세 에러 정보 (선택적)
     */
    private String details;
    
    /**
     * 추가 에러 정보 (필드별 에러 등)
     */
    private Map<String, Object> additionalInfo;
    
    /**
     * 에러 발생 필드 (유효성 검증 실패 시)
     */
    private String field;
    
    /**
     * 에러 발생 시간
     */
    private String timestamp;
    
    /**
     * 에러 추적을 위한 요청 ID (선택적)
     */
    private String requestId;
}
