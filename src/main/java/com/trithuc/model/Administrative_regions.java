package com.trithuc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "administrative_regions")
public class Administrative_regions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255) COMMENT 'Name of the region'")
    private String name;

    @Column(name = "name_en", nullable = false, columnDefinition = "VARCHAR(255) COMMENT 'English name of the region'")
    private String name_en;

    @Column(name = "code_name", nullable = false, columnDefinition = "VARCHAR(255) COMMENT 'Code name of the region'")
    private String code_name;

    @Column(name = "code_name_en", nullable = false, columnDefinition = "VARCHAR(255) COMMENT 'English code name of the region'")
    private String code_name_en;

    @OneToMany(mappedBy = "administrative_regions")
    private List<City> cities;
}
