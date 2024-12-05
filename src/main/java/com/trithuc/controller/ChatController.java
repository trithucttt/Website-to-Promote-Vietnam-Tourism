package com.trithuc.controller;

import com.trithuc.dto.ChatMessageDTO;
import com.trithuc.dto.ChatRoomDTO;

import com.trithuc.request.ChatRoomRequest;
import com.trithuc.request.EditChatRequest;
import com.trithuc.response.ChatMessageResponse;
import com.trithuc.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("chat/create")
    public ResponseEntity<Long> createChatRoom(@RequestBody ChatRoomRequest request){
        return ResponseEntity.ok(chatService.createChatRoom(request.getUserId1(), request.getUserId2()));
    }

    @GetMapping("chat/chatroom/{userId}")
    public ResponseEntity<List<ChatRoomDTO>> getChatRoomsByUser(@PathVariable Long userId){
        System.out.println(userId);
        return ResponseEntity.ok(chatService.getChatRoomForUser(userId));
    }
    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageDTO chatMessageDTO) {
        ChatMessageResponse sentMessage = chatService.sendMessage(chatMessageDTO);
        messagingTemplate.convertAndSend("/topic/chat/" + chatMessageDTO.getChatRoomId(), sentMessage);
        return ResponseEntity.ok(sentMessage);
    }

    @PostMapping("/edit")
    public ResponseEntity<ChatMessageResponse> editMessage(@RequestBody EditChatRequest editChatRequest) {
        ChatMessageResponse editMessage = chatService.editChatMessage(editChatRequest);
        messagingTemplate.convertAndSend("/topic/chat/" + editMessage.getChatRoomId(), editMessage);
        return ResponseEntity.ok(editMessage);
    }

    @PostMapping("/delete/{messageId}")
    public ResponseEntity<ChatMessageResponse> deleteMessage(@PathVariable Long messageId) {
        ChatMessageResponse editMessage = chatService.deleteChatMessageById(messageId);
        messagingTemplate.convertAndSend("/topic/chat/" + editMessage.getChatRoomId(), editMessage);
        return ResponseEntity.ok(editMessage);
    }

    @GetMapping("/chat/messages/{chatRoomId}")
    public ResponseEntity<List<ChatMessageResponse>> getChatMessages(@PathVariable Long chatRoomId) {
        return ResponseEntity.ok(chatService.getChatMessages(chatRoomId));
    }

    @GetMapping("/messages/{messageId}")
    public ResponseEntity<ChatMessageResponse> getChatMessageById(@PathVariable Long messageId) {
        return ResponseEntity.ok(chatService.getChatMessageById(messageId));
    }

}