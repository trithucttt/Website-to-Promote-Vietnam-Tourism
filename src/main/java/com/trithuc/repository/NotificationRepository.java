package com.trithuc.repository;

import com.trithuc.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {


    List<Notification> findByReceiverId(Long receiverId);

}
