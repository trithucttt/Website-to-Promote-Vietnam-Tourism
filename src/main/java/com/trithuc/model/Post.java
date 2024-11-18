package com.trithuc.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.sym.Name;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "post")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Post implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	 private Long id;
	// tên của post title
	@Column(length = 100000)
	private String title;
	
	// người đăng post
	@ManyToOne
	@JoinColumn(name = "user_id")
	@JsonIgnore
	@ToString.Exclude
	private User users;
	
	// content cho post
	// thời gian đăng post
	@Column
	private LocalDateTime startTime;
	@Column
	private LocalDateTime endTime;

	private Boolean isDelete;

	@ToString.Exclude


	@OneToMany(mappedBy = "post")
	private Set<Tour> tours = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "relatedPost")
	@ToString.Exclude
	private List< Notification> notifications = new ArrayList<>();

//	 1 post có thể có nhiều ảnh
	@OneToMany(mappedBy = "post")
	@ToString.Exclude
	private List<Image> images;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	@ToString.Exclude
	private List<Comment> comments ;

	private Boolean isBusiness;
	// đánh giá của post
//	private Integer rate;
//    public Post(Long id, String title1, User user) {
//		this.id=id;
//		this.title = title1;
//		this.users = user;
//    }
	@ToString.Exclude
	@Enumerated(EnumType.STRING)
	private State state;
}
