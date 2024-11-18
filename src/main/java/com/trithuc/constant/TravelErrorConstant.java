package com.trithuc.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TravelErrorConstant {
    
    ERROR_CODE_POST_NOT_FOUND(404,"Post not found"),
    ERROR_CODE_TOUR_NOT_FOUND(404,"Tour not found"),
    USER_NOT_FOUND(404,"User not found" ),
    FRIEND_NOT_FOUND(400,"Friend not found" ),
    NOTIFICATION_NOT_FOUND(404,"Notification not found" ),
    CHAT_MESSAGE_NOT_FOUND(404,"Chat not found" ),
    CREATE_TOUR_FAILED(401, "Error create tour"),
    DELETE_FAILED(404,"Tour not found"),
    SAVE_IMAGE_FAILED(500,"Save image Failed" ),
    IMAGE_NOT_FOUND(404,"Image not found" ),
    DESTINATION_NOT_FOUND(404,"Destination not found" ),
    WARD_NOT_FOUND(404,"Ward not found" ),
    UPDATE_DESTINATION_FAILED_SERVER_ERROR(500,"Update destination failed internal server" );

    private final Integer httpStatusCode;

    private final String message;

    public  String getErrorCode(){
        return  this.name().toLowerCase();
    }
}
