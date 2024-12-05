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
    @Query(value = """
                SELECT cr.*
                FROM chat_room cr
                JOIN chat_room_users cru1 ON cr.id = cru1.chat_room_id
                JOIN chat_room_users cru2 ON cr.id = cru2.chat_room_id
                WHERE cru1.user_id = :user1Id
                  AND cru2.user_id = :user2Id
            """, nativeQuery = true)
    Optional<ChatRoom> findByUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);


    @Query("""
                SELECT cr
                FROM ChatRoom cr
                WHERE :user MEMBER OF cr.users
            """)
    List<ChatRoom> findChatRoomsByUser(@Param("user") User user);

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
