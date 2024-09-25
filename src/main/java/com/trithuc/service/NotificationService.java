package com.trithuc.service;

import com.trithuc.dto.NotificationDTO;
import com.trithuc.model.ChatMessage;
import com.trithuc.model.Notification;
import com.trithuc.model.User;

import java.util.List;

public interface NotificationService {
    Notification createNotification(User receiver, User sender, ChatMessage chatMessage);

    void sendNotification(User receiver, Notification notification);

    List<NotificationDTO> getNotificationsByUser(Long userId);

    boolean readNotification(Long notificationId);
}
