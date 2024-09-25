package com.trithuc.repository;

import com.trithuc.model.ChatRoom;
import com.trithuc.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("SELECT cr.id, cr.users, cr.messages FROM ChatRoom cr JOIN cr.users u WHERE :user1 MEMBER OF cr.users AND :user2 MEMBER OF cr.users")
    Optional<ChatRoom> findByUsers(@Param("user1") User user1, @Param("user2") User user2);


    List<ChatRoom> findByUsersContaining(User user);

    @Query("SELECT c FROM ChatRoom c JOIN c.users u WHERE u.id = :userId")
    List<ChatRoom> findChatRoomsByUserId(@Param("userId") Long userId);
//    @Query(value = "SELECT c.* FROM chat_room c JOIN chat_room_users cru ON c.id = cru.chat_room_id WHERE cru.user_id = :userId", nativeQuery = true)
//    List<ChatRoom> findChatRoomsByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM ChatRoom c JOIN FETCH c.users WHERE c.id IN :chatRoomIds")
    List<ChatRoom> findChatRoomsWithUsersByIds(@Param("chatRoomIds") List<Long> chatRoomIds);

    @Query("SELECT u FROM User u JOIN u.chatRoom cr WHERE cr.id = :chatRoomId")
    List<User> findAllUsersInChatRoom(@Param("chatRoomId") Long chatRoomId);

}
