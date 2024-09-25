package com.trithuc.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "yourbooking")
public class YourBooking implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

    private String status;

	
	@OneToMany(mappedBy = "yourbooking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<tourbooking_item> tourbooking_items;

	@OneToOne(mappedBy = "yourBooking", cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn
	@JsonIgnore
	private Payment payment;

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "voucher_id")
//	@JsonIgnore
//	private Voucher voucher;
}


