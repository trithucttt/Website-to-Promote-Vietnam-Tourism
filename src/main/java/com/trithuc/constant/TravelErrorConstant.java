package com.trithuc.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TravelErrorConstant {
    
    ERROR_CODE_POST_NOT_FOUND(400,"Post not found"),
    ERROR_CODE_TOUR_NOT_FOUND(400,"Tour not found"),
    USER_NOT_FOUND(400,"User not found" ),
    FRIEND_NOT_FOUND(400,"Friend not found" ),
    NOTIFICATION_NOT_FOUND(400,"Notification not found" ),
    CHAT_MESSAGE_NOT_FOUND(400,"Chat not found" ),
    CREATE_TOUR_FAILED(400, "Error create tour"),
    DELETE_FAILED(400,"Tour not found");

    private final Integer httpStatusCode;

    private final String message;

    public  String getErrorCode(){
        return  this.name().toLowerCase();
    }
}
