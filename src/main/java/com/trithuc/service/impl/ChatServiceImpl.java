package com.trithuc.service.impl;

import com.trithuc.constant.TravelErrorConstant;
import com.trithuc.dto.ChatMessageDTO;
import com.trithuc.dto.ChatRoomDTO;
import com.trithuc.dto.UserDTO;
import com.trithuc.exception.TravelException;
import com.trithuc.model.ChatMessage;
import com.trithuc.model.ChatRoom;
import com.trithuc.model.Notification;
import com.trithuc.model.User;
import com.trithuc.repository.ChatMessageRepository;
import com.trithuc.repository.ChatRoomRepository;
import com.trithuc.repository.UserRepository;
import com.trithuc.response.ChatMessageResponse;
import com.trithuc.service.ChatService;
import com.trithuc.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    @Override
    public ChatRoom createChatRoom(Long userId1, Long userId2){

        User user1 = userRepository.findById(userId1).orElseThrow();
        User user2 = userRepository.findById(userId1).orElseThrow();
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByUsers(user1,user2);
        if (existingChatRoom.isPresent()){
            return existingChatRoom.get();
        }

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.getUsers().add(user1);
        chatRoom.getUsers().add(user2);
        return chatRoomRepository.save(chatRoom);

    }
    @Override
    public ChatMessageResponse sendMessage(ChatMessageDTO chatMessageDTO) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatMessageDTO.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        User sender = userRepository.findById(chatMessageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setChatRoom(chatRoom);
        chatMessage.setSender(sender);
        chatMessage.setContent(chatMessageDTO.getContent());
        chatMessage.setTimestamp(new Timestamp(System.currentTimeMillis()));

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        ChatMessageResponse response = convertToChatMessageResponse(chatMessage);
        // Gửi tin nhắn qua WebSocket đến tất cả người dùng trong phòng chat
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getId(), response);
        Notification notification = notificationService.createNotification(getReceiverFromChatRoom(chatRoom.getId(), sender),sender,savedMessage);
        notificationService.sendNotification(getReceiverFromChatRoom(chatRoom.getId(),sender),notification);
        return response;
    }
    public ChatRoomDTO convertToDto(ChatRoom chatRoom, List<UserDTO> userDTOS) {

        ChatRoomDTO chatRoomDTO = new ChatRoomDTO();
        chatRoomDTO.setChatRoomId(chatRoom.getId());
        chatRoomDTO.setUserDTOS(userDTOS);
        return chatRoomDTO;
    }

    @Override
    public List<ChatMessageResponse> getChatMessages(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoom(chatRoom);

        // Convert each ChatMessage to ChatMessageResponse
        return chatMessages.stream().map(this::convertToChatMessageResponse).collect(Collectors.toList());
    }

    private ChatMessageResponse convertToChatMessageResponse(ChatMessage chatMessage) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(chatMessage.getId());
        response.setContent(chatMessage.getContent());
        response.setSenderId(chatMessage.getSender().getId());
        response.setChatRoomId(chatMessage.getChatRoom().getId());
        response.setSenderName(chatMessage.getSender().getLastname() + " " + chatMessage.getSender().getFirstname());
        response.setTimestamp(chatMessage.getTimestamp());
        return response;
    }

    @Override
    public List<ChatRoomDTO> getChatRoomForUser(Long userId) {
        // Kiểm tra tồn tại của người dùng
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        // Tìm các phòng chat mà người dùng tham gia
        List<ChatRoom> chatRooms = chatRoomRepository.findByUsersContaining(user);
//         System.out.println(chatRooms);
        List<ChatRoomDTO> chatRoomDTOS = new ArrayList<>();
         for (ChatRoom chatRoom : chatRooms){
//
             List<User> users = chatRoomRepository.findAllUsersInChatRoom(chatRoom.getId());
//             System.out.println(users);

             List<UserDTO> userDTOS = users.stream()
                     .filter(u -> !u.getId().equals(userId))  // Loại bỏ user hiện tại
                     .map(u -> new UserDTO(u.getId(), u.getLastname() + " " + u.getFirstname(), u.getProfileImage()))  // Kết hợp firstname, lastname và avatar
                     .toList();


             chatRoomDTOS.add(convertToDto(chatRoom, userDTOS));
         }

        return chatRoomDTOS;
    }

    /**
     * Lấy ra danh sách người nhận tin nhắn từ phòng chat
     *
     * @param: senderUser - thông tin người gửi
     * @return : allUserDTOS Danh sách thông tin người nhận tin nhắn từ phòng chat đươc chuyển đổi sang DTO
     * */
    private List<UserDTO> getReceiversFromChatRoom(User senderUser) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByUsersContaining(senderUser);
//        System.out.println(chatRooms);
        // Danh sách UserDTO để lưu các user trong tất cả phòng chat trừ senderUser
        List<UserDTO> allUserDTOS = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            List<User> users = chatRoomRepository.findAllUsersInChatRoom(chatRoom.getId());
//            System.out.println(users);

            // Lọc ra những user không phải là senderUser và chuyển đổi thành UserDTO
            List<UserDTO> userDTOS = users.stream()
                    .filter(u -> !u.getId().equals(senderUser.getId()))  // Loại bỏ user hiện tại
                    .map(u -> new UserDTO(u.getId(), u.getFirstname() + " " + u.getLastname(), u.getProfileImage()))  // Kết hợp firstname, lastname và avatar
                    .toList();
            allUserDTOS.addAll(userDTOS);
        }

        return allUserDTOS;
    }

    /**
     * Lấy ra thông tin người nhận tin nhắn từ phòng chat
     *
     * @param: chatRoomId - ID của phòng chat
     * @param: senderUser - thông tin người gửi
     * @return : receiver thông tin người nhận
     */
    private User getReceiverFromChatRoom(Long chatRoomId, User senderUser) {
        // Lấy danh sách người dùng trong phòng chat dựa trên chatRoomId
        List<User> users = chatRoomRepository.findAllUsersInChatRoom(chatRoomId);
//        System.out.println(users);

        // Tìm người nhận (người không phải là senderUser)
        User receiver = users.stream()
                .filter(u -> !u.getId().equals(senderUser.getId()))  // Lọc người nhận
                .findFirst()  // Tìm người nhận đầu tiên
                .orElse(null);  // Nếu không có thì trả về null

        // Trả về người nhận nếu tìm thấy, ngược lại trả về null
        return receiver;
    }

    /**
     * Lấy ra thông tin của tin nhắn với id tin nhắn
     *
     * @param: chatMessageId - ID của tin nhắn
     * @return : chatMessageDTO thông tin tin nhắn được chuyển đổi qua DTO
     */
    @Override
    public ChatMessageResponse getChatMessageById(Long chatMessageId){
        Optional<ChatMessage> chatMessage = chatMessageRepository.findById(chatMessageId);
        if (chatMessage.isEmpty()){
            throw new TravelException(TravelErrorConstant.CHAT_MESSAGE_NOT_FOUND);
        }
        return convertToChatMessageResponse(chatMessage.get());
    }

}