package com.trithuc.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "tour")
@AllArgsConstructor
//@NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
@NoArgsConstructor
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
    @ToString.Exclude
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
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Destination> destination = new HashSet<>();

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "post_id")
    private Post post;

//    @JsonIgnore
//    private String image_tour;

    @OneToMany(mappedBy = "tour")
    @ToString.Exclude
    private List<Image> images;

    private Integer quantity;

    private Double discount;

    private  LocalDateTime startTimeTour;

    private LocalDateTime endTimeTour;

    @OneToMany( mappedBy = "tour")
    @JsonIgnore
    @ToString.Exclude
    private List<CartItems> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<tourbooking_item> tourbooking_items;


}
