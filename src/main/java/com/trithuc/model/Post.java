package com.trithuc.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.sym.Name;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


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
	@JsonBackReference
	private User users;
	
	// content cho post
	// thời gian đăng post
	@Column
	private LocalDateTime startTime;
	@Column
	private LocalDateTime endTime;

	private Boolean isDelete;

	@OneToMany(mappedBy = "post",cascade = CascadeType.ALL,orphanRemoval = true)
	private Set<PostTour> tours = new HashSet<>();


//	@ManyToMany
//	@JoinTable(
//			name = "post_in_tour",
//			joinColumns = @JoinColumn(name ="post_id"),
//			inverseJoinColumns = @JoinColumn(name = "tour_id")
//	)
//	private Set<Tour> tours = new HashSet<>();
//
	// 1 post có thể có nhiều ảnh
//	@OneToMany(mappedBy = "post")
//	private List<Image> images;
	
	// đánh giá của post
//	private Integer rate;

//	public City getCityTour() {
//	    if (tour != null && tour.getDestination() != null && !tour.getDestination().isEmpty()) {
//	        Destination destination = tour.getDestination().get(0);
//	        if (destination != null && destination.getWard() != null && destination.getWard().getDistrict() != null) {
//	            return destination.getWard().getDistrict().getCity();
//	        }
//	    }
//	    return null;
//	}
//	 public String getCityName() {
//	        if (tour != null && tour.getDestination() != null && !tour.getDestination().isEmpty()) {
//	            Destination destination = tour.getDestination().get(0);
//	            if (destination != null && destination.getWard() != null && destination.getWard().getDistrict() != null) {
//	                return destination.getWard().getDistrict().getCity().getName();
//	            }
//	        }
//	        return null;
//	    }
//	 public void setCityName(String cityName) {
//	        // Tạo mới một đối tượng City từ tên thành phố
//	        City city = new City();
//	        city.setName(cityName);
//	        this.cityName = city;
//	    }


	
}
