package com.trithuc.controller;

import com.trithuc.dto.ChatMessageDTO;
import com.trithuc.dto.ChatRoomDTO;
import com.trithuc.model.ChatMessage;
import com.trithuc.model.ChatRoom;
import com.trithuc.request.ChatRoomRequest;
import com.trithuc.response.ChatMessageResponse;
import com.trithuc.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.SendTo;
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
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody ChatRoomRequest request){
        ChatRoom chatRoom = chatService.createChatRoom(request.getUserId1(), request.getUserId2());
        return ResponseEntity.ok(chatRoom);
    }

    @GetMapping("chat/chatroom/{userId}")
    public ResponseEntity<List<ChatRoomDTO>> getChatRoomsByUser(@PathVariable Long userId){
        System.out.println(userId);
        return ResponseEntity.ok(chatService.getChatRoomForUser(userId));
    }
    @PostMapping("/send")
//    @SendTo("/topic/chat/{chatRoomId}")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageDTO chatMessageDTO) {
        ChatMessageResponse sentMessage = chatService.sendMessage(chatMessageDTO);
        messagingTemplate.convertAndSend("/topic/chat/" + chatMessageDTO.getChatRoomId(), sentMessage);
        return ResponseEntity.ok(sentMessage);
    }

    @GetMapping("/chat/messages/{chatRoomId}")
    public ResponseEntity<List<ChatMessageResponse>> getChatMessages(@PathVariable Long chatRoomId) {
//        List<ChatMessage> messages =
//        System.out.println(messages);
        return ResponseEntity.ok(chatService.getChatMessages(chatRoomId));
    }

    @GetMapping("/messages/{messageId}")
    public ResponseEntity<ChatMessageResponse> getChatMessageById(@PathVariable Long messageId) {
//        List<ChatMessage> messages =
//        System.out.println(messages);
        return ResponseEntity.ok(chatService.getChatMessageById(messageId));
    }

}