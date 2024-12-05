package com.trithuc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private String message;
    private boolean isRead;
    private LocalDateTime createAt;
    private Long relateChatMessageId;
//    private Long relatedCommentId;
//    private Long relatedPostId;

    private Long receiverId;
    private String userName;
    private Long relatedFriendShipId;
}