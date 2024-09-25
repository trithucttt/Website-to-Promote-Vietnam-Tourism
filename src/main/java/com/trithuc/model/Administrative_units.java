package com.trithuc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Table(name = "administrative_units")
@AllArgsConstructor
@NoArgsConstructor
public class Administrative_units {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String full_name;

    @Column(name = "full_name_en", nullable = false)
    private String full_name_en;

    @Column(name = "short_name", nullable = false)
    private String short_name;

    @Column(name = "short_name_en", nullable = false)
    private String short_name_en;

    @Column(name = "code_name", nullable = false)
    private String code_name;

    @Column(name = "code_name_en", nullable = false)
    private String code_name_en;

    @OneToMany(mappedBy = "administrative_units")
    private List<City> cities;

    @OneToMany(mappedBy = "administrative_units")
    private List<Ward> wards;

    @OneToMany(mappedBy = "administrative_units")
    private List<District> districts;

}
