package com.trithuc.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long chatRoomId;
    private Long senderId;
    private String content;
}
