package com.trithuc.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.sym.Name;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name =  "tourbooking_item")
@AllArgsConstructor
// @NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
@NoArgsConstructor
public class tourbooking_item implements Serializable {



	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Integer quantity;

	@ManyToOne
	@JoinColumn(name =  "tour_id")
	private Tour tour;

	private Double price;

	@ManyToOne
	@JoinColumn(name = "yourbooking_id")
	private YourBooking yourbooking;

	@JsonIgnore
	private String status;
//	@Enumerated(EnumType.STRING)
//    private itemStatus status;
//	public enum itemStatus {
//	    INCART,
//	    CHECKED_OUT,
//	}
	
}
