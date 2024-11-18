package com.trithuc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "friendships")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friendship {

    @EmbeddedId
    private FriendshipId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("friendId")
    @JoinColumn(name = "friend_id")
    private User friend;


    @Enumerated(EnumType.STRING)
    @Column(name = "friend_state")
    private FriendState friendState;
}