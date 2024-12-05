package com.trithuc.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "chat_message")
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private Timestamp timestamp;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    @ToString.Exclude
    @JsonBackReference
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    @ToString.Exclude

    private User sender;

    @JsonIgnore
    @OneToMany(mappedBy = "relatedChatMessage")
    @ToString.Exclude
    private List< Notification> notifications = new ArrayList<>();

    private Boolean isEdited;
    private Boolean isDeleted;
}

