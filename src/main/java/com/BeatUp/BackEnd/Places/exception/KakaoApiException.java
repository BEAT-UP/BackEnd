package com.BeatUp.BackEnd.Places.exception;

import com.BeatUp.BackEnd.common.enums.ErrorCode;
import com.BeatUp.BackEnd.common.exception.BusinessException;

public class KakaoApiException extends BusinessException {

    public KakaoApiException(ErrorCode errorCode){
        super(errorCode);
    }

    public KakaoApiException(ErrorCode errorCode, String message){
        super(errorCode, message);
    }

    public KakaoApiException(ErrorCode errorCode, String message, Throwable cause){
        super(errorCode, message, cause);
    }

    // 정적 팩토리 메서드들
    public static KakaoApiException timeout(){
        return new KakaoApiException(ErrorCode.KAKAO_API_TIMEOUT);
    }

    public static KakaoApiException rateLimitExceeded(){
        return new KakaoApiException(ErrorCode.KAKAO_API_RATE_LIMIT);
    }

    public static KakaoApiException badRequest(String detail){
        return new KakaoApiException(
                ErrorCode.KAKAO_API_BAD_REQUEST,
                "카카오맵 API 요청 오류: " + detail
        );
    }

    public static KakaoApiException serverError(int statusCode){
        return new KakaoApiException(
                ErrorCode.KAKAO_API_SERVER_ERROR,
                String.format("카카오맵 서버 오류 (HTTP %d)", statusCode)
        );
    }

    public static KakaoApiException generalError(String detail){
        return new KakaoApiException(ErrorCode.KAKAO_API_SERVER_ERROR, detail);
    }

    public static KakaoApiException generalError(String detail, Throwable cause){
        return new KakaoApiException(ErrorCode.KAKAO_API_SERVER_ERROR, detail, cause);
    }
}
