package com.trithuc.service.impl;

import com.trithuc.constant.TravelErrorConstant;
import com.trithuc.dto.NotificationDTO;
import com.trithuc.exception.TravelException;
import com.trithuc.model.ChatMessage;
import com.trithuc.model.Notification;
import com.trithuc.model.User;
import com.trithuc.repository.NotificationRepository;
import com.trithuc.repository.UserRepository;
import com.trithuc.response.MessageResponse;
import com.trithuc.service.NotificationService;
import com.trithuc.service.TravelContentService;
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
    private TravelContentService travelContentService;


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

    @Override
    public Notification saveNotificationForAddFriend( Notification notification) {
        return notificationRepository.save(notification);

    }

    @Override
    public NotificationDTO convertNotificationDTO(Notification notification) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(notification.getId());
        notificationDTO.setMessage(notification.getMessage());
        notificationDTO.setReceiverId(notification.getReceiver().getId());
        notificationDTO.setUserName(notification.getReceiver().getFirstname() + notification.getReceiver().getLastname());
        notificationDTO.setCreateAt(notification.getCreatedAt());
        if (notification.getRelatedChatMessage() != null){
            notificationDTO.setRelateChatMessageId(notification.getRelatedChatMessage().getId());
        }
        notificationDTO.setRead(notification.isRead());
        if (notification.getRelatedFriendship() != null){
            notificationDTO.setRelatedFriendShipId(notification.getRelatedFriendship().getUser().getId());
        }

        return notificationDTO;
    }

    @Override
    public void sendNotification(User receiver, Notification notification) {
        NotificationDTO notificationDTO = convertNotificationDTO(notification);
//        System.out.println("Sending notification to: " + receiver.getUsername());
//        System.out.println("Notification: " + notificationDTO);
        messagingTemplate.convertAndSend("/queue/" + receiver.getId() + "/notifications", notificationDTO);
    }


    private User getUserById(Long id){
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()){
            throw new TravelException(TravelErrorConstant.USER_NOT_FOUND);
        }
//        System.out.println(user.get() + "11");
        return user.get();
    }
    @Override
    public List<NotificationDTO> getNotificationsByUser(Long userId) {

        User user = getUserById(userId);
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

    @Override
    public MessageResponse deleteNotificationById(Long notificationId) {
        Optional<Notification> notificationOtp = notificationRepository.findById(notificationId);
        if (notificationOtp.isEmpty()){
            return travelContentService.setUpResponse("Không tìm thấy thông báo với id " + notificationId,"400",null);
        }
        notificationRepository.deleteById(notificationOtp.get().getId());
        return travelContentService.setUpResponse("Xóa thành công","200",null);
    }


}
