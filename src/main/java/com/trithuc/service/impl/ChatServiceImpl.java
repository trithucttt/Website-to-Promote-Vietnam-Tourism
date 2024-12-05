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
import com.trithuc.request.EditChatRequest;
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
    public Long createChatRoom(Long userId1, Long userId2) {
        // Lấy user1 và user2 từ cơ sở dữ liệu
        User user1 = userRepository.findById(userId1).orElseThrow(() -> new RuntimeException("User 1 not found"));
        User user2 = userRepository.findById(userId2).orElseThrow(() -> new RuntimeException("User 2 not found"));

        // Lấy danh sách các phòng chat mà user1 và user2 tham gia
        List<ChatRoom> user1ChatRooms = chatRoomRepository.findChatRoomsByUser(user1);
        List<ChatRoom> user2ChatRooms = chatRoomRepository.findChatRoomsByUser(user2);

        // Bước 2: Nếu không có phòng chat nào cho user1 hoặc user2, tạo phòng chat mới
        if (user1ChatRooms.isEmpty() || user2ChatRooms.isEmpty()) {
            // Tạo phòng chat mới
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.getUsers().add(user1);
            chatRoom.getUsers().add(user2);
            ChatRoom saveChatRoom = chatRoomRepository.save(chatRoom);
            return saveChatRoom.getId(); // Trả về ID của phòng chat mới
        }

        // Kiểm tra xem có phòng chat chung giữa user1 và user2 không
        for (ChatRoom room1 : user1ChatRooms) {
            for (ChatRoom room2 : user2ChatRooms) {
                if (room1.getId().equals(room2.getId())) {
                    // Nếu có phòng chat chung, trả về ID của phòng chat đó
                    return room1.getId();
                }
            }
        }

        // Nếu không có phòng chat chung, tạo phòng chat mới và trả về ID của nó
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.getUsers().add(user1);
        chatRoom.getUsers().add(user2);
        ChatRoom saveChatRoom = chatRoomRepository.save(chatRoom);
        return saveChatRoom.getId();
    }

    @Override
    public ChatMessageResponse sendMessage(ChatMessageDTO chatMessageDTO) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatMessageDTO.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        User sender = userRepository.findById(chatMessageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setIsDeleted(false);
        chatMessage.setIsEdited(false);
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
        response.setSenderName(chatMessage.getSender().getFirstname() + " " + chatMessage.getSender().getLastname());
        response.setTimestamp(chatMessage.getTimestamp());
        response.setEdited(chatMessage.getIsEdited());
        response.setIsDeleted(chatMessage.getIsDeleted());
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
                     .map(u -> new UserDTO(u.getId(), u.getFirstname() + " " + u.getLastname(), u.getProfileImage()))  // Kết hợp firstname, lastname và avatar
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


    @Override
    public ChatMessageResponse editChatMessage(EditChatRequest editChatRequest) {
        ChatMessage chatMessage = chatMessageRepository.findChatRoomById(editChatRequest.getChatMessageId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        ChatRoom chatRoom = chatMessage.getChatRoom();


        chatMessage.setContent(editChatRequest.getEditContent());
        chatMessage.setTimestamp(new Timestamp(System.currentTimeMillis()));
        chatMessage.setIsEdited(true);
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        ChatMessageResponse response = convertToChatMessageResponse(chatMessage);
        // Gửi tin nhắn qua WebSocket đến tất cả người dùng trong phòng chat
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getId(), response);

        return response;
    }

    @Override
    public ChatMessageResponse deleteChatMessageById(Long  chatMessageId) {
        ChatMessage chatMessage = chatMessageRepository.findChatRoomById(chatMessageId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        User sender = chatMessage.getSender();

        ChatRoom chatRoom = chatMessage.getChatRoom();

        chatMessage.setIsDeleted(true);
        chatMessage.setTimestamp(new Timestamp(System.currentTimeMillis()));
        ChatMessage deleteChat = chatMessageRepository.save(chatMessage);
        ChatMessageResponse response = convertToChatMessageResponse(chatMessage);
        // Gửi tin nhắn qua WebSocket đến tất cả người dùng trong phòng chat
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getId(), response);

        return response;
    }
}
