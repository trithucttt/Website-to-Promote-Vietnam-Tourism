package com.trithuc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comment")
public class Comment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    // user comment

    private Long id;
    @Lob
    @Column(length = 100000)
    private String content;
    private LocalDateTime start_time;

//    private Short rating;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    @ToString.Exclude
    private User user; // Người dùng tạo comment

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "parent_id")
    private Comment parent; // Comment gốc mà reply này thuộc về

    @OneToMany(mappedBy = "parent" ,fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Comment> replies = new HashSet<>(); // Các reply cho comment này

    @ManyToOne
    @JoinColumn(name = "post_id")
    @JsonIgnore
    @ToString.Exclude
    private Post post;

    @OneToMany(mappedBy = "comment")
    private Set<Image> images = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "relatedComment")
    @ToString.Exclude
    private List< Notification> notifications = new ArrayList<>();

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private State state;

}




