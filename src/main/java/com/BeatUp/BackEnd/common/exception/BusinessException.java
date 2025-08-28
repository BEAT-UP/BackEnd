package com.BeatUp.BackEnd.common.exception;

import com.BeatUp.BackEnd.common.enums.ErrorCode;

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
}
