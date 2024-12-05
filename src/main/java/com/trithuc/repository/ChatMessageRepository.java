package com.trithuc.repository;

import com.trithuc.model.ChatMessage;
import com.trithuc.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoom(ChatRoom chatRoom);

    Optional<ChatMessage> findChatRoomById(Long chatMessageId);
}
