package com.trithuc.controller;

import com.trithuc.dto.ChatMessageDTO;
import com.trithuc.model.ChatMessage;
import com.trithuc.response.ChatMessageResponse;
import com.trithuc.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/chat/{chatRoomId}")
    public void sendMessage(ChatMessageDTO chatMessageDTO) {
        ChatMessageResponse sentMessage = chatService.sendMessage(chatMessageDTO);
        // Broadcast the message to the specific chat room
        messagingTemplate.convertAndSend("/topic/chat/" + chatMessageDTO.getChatRoomId(), sentMessage);
    }
}
