package com.trithuc.service;

import com.trithuc.dto.ChatMessageDTO;
import com.trithuc.dto.ChatRoomDTO;
import com.trithuc.model.ChatMessage;
import com.trithuc.model.ChatRoom;
import com.trithuc.response.ChatMessageResponse;

import java.util.List;

public interface ChatService {

    public ChatRoom createChatRoom(Long userId1, Long userId2);
    public ChatMessageResponse sendMessage(ChatMessageDTO chatMessageDTO);
    public List<ChatMessageResponse> getChatMessages(Long chatRoomId);

    List<ChatRoomDTO> getChatRoomForUser(Long userId);

    ChatMessageResponse getChatMessageById(Long chatMessageId);
}