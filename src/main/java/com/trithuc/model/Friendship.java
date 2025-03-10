package com.trithuc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

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

    @JsonIgnore
    @OneToMany(mappedBy = "relatedFriendship")
    @ToString.Exclude
    private List< Notification> notifications = new ArrayList<>();
}