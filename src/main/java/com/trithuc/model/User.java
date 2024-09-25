package com.trithuc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PUBLIC, force=true)
public class User implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	 private Long id;
	@JsonIgnore
	private String username;
	private String firstname;
	private String lastname;
	@JsonIgnore
	private String email;
	@JsonIgnore
	private String password;
	private String profileImage;
	@JsonIgnore
	private String address;
	@JsonIgnore
	@Enumerated(EnumType.STRING)
	private Role role;
	@JsonIgnore
	@OneToMany(mappedBy = "users")
	@JsonManagedReference
	private List<Post> posts;
	@JsonIgnore
	@OneToMany(mappedBy = "user")
	private List<YourBooking> yourBookings;
	@JsonIgnore
	@OneToMany(mappedBy = "manager")
	private Set<Tour> managedTour;
	@JsonIgnore
	@OneToMany(mappedBy = "manager")
	private Set<Destination> managedDestination;
	@JsonIgnore
	@OneToMany(mappedBy = "user")
	private List<Comment> comments = new ArrayList<>(); // Các comment của người dùng

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
	@JsonIgnore
	private List<CartItems> cartItems = new ArrayList<>();
}
