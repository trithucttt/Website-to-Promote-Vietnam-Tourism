package com.trithuc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String message;
    private boolean isRead;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "receiver_id")
    private User receiver;  // Người nhận thông báo

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "related_chat_id", nullable = true)
    private ChatMessage relatedChatMessage;  // Thông báo liên quan tới tin nhắn

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "related_comment_id", nullable = true)
    private Comment relatedComment;  // Thông báo liên quan tới comment

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "related_post_id", nullable = true)
    private Post relatedPost;  // Thông báo liên quan tới bài đăng

    private LocalDateTime createdAt;

    @ManyToOne
    @ToString.Exclude
    @JoinColumns({
            @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
            @JoinColumn(name = "friend_id", referencedColumnName = "friend_id")
    })
    private Friendship relatedFriendship;


}
