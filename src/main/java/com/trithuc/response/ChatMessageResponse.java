package com.trithuc.response;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ChatMessageResponse {

        private Long id;
        private String content;
        private Long senderId;
        private Long chatRoomId;
        private Timestamp timestamp;
        private String senderName;
}
