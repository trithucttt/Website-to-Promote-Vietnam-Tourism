package com.trithuc.model;

import java.io.Serializable;

import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Geometry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "geometry")
public class GeometryTravel {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(name = "the_geom",columnDefinition = "GEOMETRY")
    private Geometry the_geom;
    @Column(name = "the_geom_text")  
    private String the_geom_text;
}
