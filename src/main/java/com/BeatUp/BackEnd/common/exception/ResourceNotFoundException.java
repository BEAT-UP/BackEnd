package com.BeatUp.BackEnd.common.exception;

import com.BeatUp.BackEnd.common.enums.ErrorCode;

public class ResourceNotFoundException extends BusinessException{

    public ResourceNotFoundException(String message){
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(){
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
