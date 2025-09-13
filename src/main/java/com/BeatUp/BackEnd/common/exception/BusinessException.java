package com.BeatUp.BackEnd.common.exception;

import com.BeatUp.BackEnd.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }

    // 예외 체이닝 지원을 위한 생성자 추가
    public BusinessException(ErrorCode errorCode, Throwable cause){
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause){
        super(message, cause);
        this.errorCode = errorCode;
    }
}
