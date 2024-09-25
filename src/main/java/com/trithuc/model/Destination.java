package com.trithuc.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@Table(name = "destination")
public class Destination implements Serializable {
// điểm đến của các tour

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String name;

    @JsonIgnore
    private String address;


    @ManyToOne
    @JoinColumn(name = "ward_id")
    @JsonIgnore
    private Ward ward;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    @JsonIgnore
    @ToString.Exclude
    private User manager;

    @Lob
    @Column(length = 100000)
    @JsonIgnore
    private String description;

    @ManyToMany(mappedBy = "destination")
    @JsonIgnore
    private Set<Tour> tours = new HashSet<>();

    //	@ManyToMany
    @JsonIgnore
    private String image_destination;
}
