package com.trithuc.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "tour")
@AllArgsConstructor
//@NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
@NoArgsConstructor
public class Tour implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String title;

    @Lob
    @Column(length = 100000)
    @JsonIgnore
    private String description;

    @JsonIgnore
    private Double price;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "manager_id")
    private User manager;

    @JsonIgnore
    private Boolean isDelete;

    @ManyToMany
    @JoinTable(
            name = "destination_in_tour",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "destination_id")
    )
    @JsonIgnore
    private Set<Destination> destination = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @JsonIgnore
    private String image_tour;

    private Integer quantity;

    private Double discount;

    private  LocalDateTime startTimeTour;

    private LocalDateTime endTimeTour;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tour", orphanRemoval = true)
    @JsonIgnore
    private List<CartItems> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL)
    private List<Comment> comments ;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL)
    private List<tourbooking_item> tourbooking_items;


}
