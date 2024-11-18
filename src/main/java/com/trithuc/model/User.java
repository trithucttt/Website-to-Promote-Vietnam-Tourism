package com.trithuc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.*;

@Entity
@Data
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String firstname;

    private String lastname;

    @JsonIgnore
    private String email;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private String profileImage;

    @JsonIgnore
    private String address;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private Role role;

    @JsonIgnore
    @OneToMany(mappedBy = "users")
    @ToString.Exclude
    private List<Post> posts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private List<YourBooking> yourBookings = new ArrayList<>();;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "manager")
    private Set<Tour> managedTour = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "manager")
    @ToString.Exclude
    private Set<Destination> managedDestination = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private List<Comment> comments = new ArrayList<>(); // Các comment của người dùng

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<CartItems> cartItems = new ArrayList<>();


    @OneToMany(mappedBy = "user")
    @JsonIgnore
    @ToString.Exclude
    private Set<Friendship> friends = new HashSet<>();

    @OneToMany(mappedBy = "friend")
    @JsonIgnore
    @ToString.Exclude
    private Set<Friendship> friendOf = new HashSet<>();

    @OneToMany(mappedBy = "sender")
    @ToString.Exclude
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @ManyToMany(mappedBy = "users")
    @ToString.Exclude
    private Set<ChatRoom> chatRoom = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "receiver")
    @ToString.Exclude
    private List<Notification> notifications = new ArrayList<>();
}
