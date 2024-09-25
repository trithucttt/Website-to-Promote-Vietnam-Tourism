package com.trithuc.controller;
import com.trithuc.dto.NotificationDTO;
import com.trithuc.model.Notification;
import com.trithuc.model.User;
import com.trithuc.service.NotificationService;
import com.trithuc.service.UserService; // Giả sử bạn có dịch vụ để lấy thông tin người dùng
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;


    @GetMapping("/get/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUser(@PathVariable Long userId){
        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId));
    }


    @PutMapping("/read/{notificationId}")
    public ResponseEntity<Boolean> readNotification(@PathVariable Long notificationId){
        return ResponseEntity.ok(notificationService.readNotification(notificationId));
    }
}
