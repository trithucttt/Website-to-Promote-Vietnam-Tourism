package com.trithuc.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "event")
public class Event implements Serializable{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	 private Long id;
	private String name;
	private Boolean active;
	private String description;
	
//
//	@ManyToMany
//	@JoinTable(
//			name = "event_tour",
//			joinColumns = @JoinColumn(name = "event_id"),
//			inverseJoinColumns = @JoinColumn(name = "tour_id")
//			)
//	private Set<Tour> tours = new HashSet<>(); //lien ket bang
	
}
