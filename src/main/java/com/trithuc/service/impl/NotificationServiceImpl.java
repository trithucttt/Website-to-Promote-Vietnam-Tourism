package com.trithuc.service.impl;

import com.trithuc.constant.TravelErrorConstant;
import com.trithuc.dto.NotificationDTO;
import com.trithuc.exception.TravelException;
import com.trithuc.model.ChatMessage;
import com.trithuc.model.Notification;
import com.trithuc.model.User;
import com.trithuc.repository.NotificationRepository;
import com.trithuc.repository.UserRepository;
import com.trithuc.service.NotificationService;
import com.trithuc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public Notification createNotification(User receiver, User sender, ChatMessage chatMessage) {
        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setRelatedChatMessage(chatMessage);
        notification.setRead(false);
        notification.setMessage("Bạn có một tin nhắn mới từ " + sender.getLastname() + " " + sender.getFirstname());
        LocalDateTime createAt = LocalDateTime.now();
        notification.setCreatedAt(createAt);
        return notificationRepository.save(notification);

    }

    private NotificationDTO convertNotificationDTO(Notification notification) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(notification.getId());
        notificationDTO.setMessage(notification.getMessage());
        notificationDTO.setUserId(notification.getReceiver().getId());
        notificationDTO.setUserName(notification.getReceiver().getFirstname() + notification.getReceiver().getLastname());
        notificationDTO.setCreateAt(notification.getCreatedAt());
        notificationDTO.setRelateChatMessageId(notification.getRelatedChatMessage().getId());
        notificationDTO.setRead(notification.isRead());
        return notificationDTO;
    }

    @Override
    public void sendNotification(User receiver, Notification notification) {
        NotificationDTO notificationDTO = convertNotificationDTO(notification);
//        System.out.println("Sending notification to: " + receiver.getUsername());
//        System.out.println("Notification: " + notificationDTO);
        messagingTemplate.convertAndSend("/queue/" + receiver.getId() + "/notifications", notificationDTO);
    }


    @Override
    public List<NotificationDTO> getNotificationsByUser(Long userId) {

        User user = userService.getUserById(userId);
        List<Notification> notifications = notificationRepository.findByReceiverId(user.getId());
        return notifications.stream().map(this::convertNotificationDTO).toList();
    }

    @Override
    public boolean readNotification(Long notificationId) {
        Optional<Notification> notificationOptional = notificationRepository.findById(notificationId);
        if (notificationOptional.isEmpty()) {
            throw new TravelException(TravelErrorConstant.NOTIFICATION_NOT_FOUND);
        }
        Notification notification = notificationOptional.get();
        notification.setRead(true);
        notificationRepository.save(notification);
        return true;
    }
}
