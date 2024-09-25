package com.trithuc.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.sym.Name;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "post")
public class Post implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	 private Long id;
	// tên của post title
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
	@OneToMany(mappedBy = "post",cascade = CascadeType.ALL,orphanRemoval = true)
	private Set<Tour> tours = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "relatedPost")
	@ToString.Exclude
	private List< Notification> notifications = new ArrayList<>();

	private String content;

//	 1 post có thể có nhiều ảnh
	@OneToMany(mappedBy = "post")
	@ToString.Exclude
	private List<Image> images;
	
	// đánh giá của post
//	private Integer rate;


//    public Post(Long id, String title1, User user) {
//		this.id=id;
//		this.title = title1;
//		this.users = user;
//    }
	
}
