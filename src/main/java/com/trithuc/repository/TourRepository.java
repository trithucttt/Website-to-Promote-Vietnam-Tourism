package com.trithuc.repository;

import com.trithuc.model.Tour;
import com.trithuc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour,Long> {
    @Query("SELECT t FROM Tour t WHERE t.manager = :manager")
    List<Tour> findToursByManager(@Param("manager") User manager);

    @Query("SELECT t FROM Tour t WHERE t.manager.id = :manager")
    List<Tour> findToursByManagerId(@Param("manager") Long manager);


    @Query(value = "SELECT t.* FROM post_tour pt JOIN tour t ON pt.tour_id = t.id WHERE pt.post_id = :postId", nativeQuery = true)
    List<Tour> findToursByPostId(@Param("postId") Long postId);


    @Query("SELECT t.image_tour FROM Tour t WHERE t.id = :tourId")
    String findImageNameByTourId(Long tourId);
    @Query("SELECT pt.discount FROM PostTour pt WHERE pt.post.id = :postId")
    List<Double> findDiscountsByPostId(@Param("postId") Long postId);

    List<Tour> findByTitleContainingIgnoreCase(String title);
    @Query(value = "SELECT MAX(id) FROM Tour ")
    Long getMaxId();
}
