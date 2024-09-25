package com.trithuc.exception;

import com.trithuc.constant.TravelErrorConstant;
import lombok.Getter;

@Getter
public class TravelException extends  RuntimeException{
    private  final String errorCode;
    private  final  String message;
    private final  Integer httpStatusCode;

    public TravelException(TravelErrorConstant constant) {
        this.errorCode =constant.getErrorCode();
        this.message = constant.getMessage();
        this.httpStatusCode = constant.getHttpStatusCode();
    }
}
