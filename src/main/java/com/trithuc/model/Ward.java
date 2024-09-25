package com.trithuc.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "ward")
public class Ward implements Serializable{
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "district_id")
	@JsonIgnore
	private District district;

	@ManyToOne
	@JoinColumn(name = "administrative_units_id")
	@JsonIgnore
	private Administrative_units administrative_units;

	@OneToMany(mappedBy = "ward")
	@JsonIgnore
	private List<Destination> destinations;


	
	
	

}
