package com.trithuc.repository;

import com.trithuc.model.Tour;
import com.trithuc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour,Long> {
    @Query("SELECT t FROM Tour t WHERE t.manager = :manager")
    List<Tour> findToursByManager(@Param("manager") User manager);
    List<Tour> findByManagerId(Long managerId);
    @Query("SELECT t FROM Tour t WHERE t.manager.id = :manager AND t.isDelete = false")
    List<Tour> findToursByManagerId(@Param("manager") Long manager);

    @Query(value = "SELECT t FROM Tour t JOIN t.post p WHERE p.id = :postId")
    List<Tour> findToursByPostId(@Param("postId") Long postId);

    @Query("SELECT t.images FROM Tour t WHERE t.id = :tourId")
    String findImageNameByTourId(Long tourId);

    @Query("SELECT pt.discount FROM Tour pt WHERE pt.post.id = :postId")
    List<Double> findDiscountsByPostId(@Param("postId") Long postId);

    List<Tour> findByTitleContainingIgnoreCase(String title);
    @Query(value = "SELECT MAX(id) FROM Tour ")
    Long getMaxId();

    List<Tour> findByPostId(Long postId);

    @Query("SELECT t FROM Tour t WHERE t.post IS NULL " +
            "AND t.endTimeTour >= :oneWeekLater " +
            "AND t.quantity > 0 " +
            "AND t.manager.id = :managerId")
    List<Tour> findAvailableToursByManager(LocalDateTime oneWeekLater, Long managerId);
}
