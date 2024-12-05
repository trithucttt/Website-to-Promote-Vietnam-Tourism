package com.trithuc.controller;

import com.trithuc.dto.ChatMessageDTO;
import com.trithuc.model.ChatMessage;
import com.trithuc.request.EditChatRequest;
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
        sentMessage.setType("ADD");
        messagingTemplate.convertAndSend("/topic/chat/" + chatMessageDTO.getChatRoomId(), sentMessage);
    }

    @MessageMapping("/chat.editMessage")
    @SendTo("/topic/chat/{chatRoomId}")
    public void editMessage(EditChatRequest editChatRequest) {
        ChatMessageResponse editMessage = chatService.editChatMessage(editChatRequest);
        editMessage.setType("EDIT");
        messagingTemplate.convertAndSend("/topic/chat/" + editMessage.getChatRoomId(), editMessage);
    }

    @MessageMapping("/chat.deleteMessage/{messageId}")
    @SendTo("/topic/chat/{chatRoomId}")
    public void deleteMessage(Long messageId) {
        ChatMessageResponse deleteMessage = chatService.deleteChatMessageById(messageId);
        System.out.println("Deleted message broadcasted: " + deleteMessage);
        deleteMessage.setType("DELETE");
        messagingTemplate.convertAndSend("/topic/chat/" + deleteMessage.getChatRoomId(), deleteMessage);
    }
}
