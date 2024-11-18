package com.trithuc.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;



@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "provinces")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class City implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "name_en", nullable = false)
	private String name_en;

	@Column(name = "full_name", nullable = false)
	private String full_name;

	@Column(name = "full_name_en", nullable = false)
	private String full_name_en;

	@Column(name = "code_name", nullable = false)
	@JsonIgnore
	private String code_name;

	// Các quan hệ với các entity khác

	@OneToMany(mappedBy = "city")
	@JsonIgnore
	private List<District> districts;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "administrative_regions_id")
	private Administrative_regions administrative_regions;

	@ManyToOne
	@JoinColumn(name = "administrative_units_id")
	@JsonIgnore
	private Administrative_units administrative_units;


}
