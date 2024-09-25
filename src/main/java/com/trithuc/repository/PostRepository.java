package com.trithuc.repository;

import com.trithuc.model.Post;
import com.trithuc.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.users = :user")
    List<Post> findPostsByUser(@Param("user") User user);

    @Query("SELECT t FROM Post t WHERE t.users.id = :user")
    List<Post> findPostsByUserId(@Param("user") Long user);

    // Search by article title and search for articles with similar tour titles
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN p.tours t " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(t.tour.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Post> findByTitleContainingOrTourTitleContaining(String searchTerm);

    Page<Post> findByTitleLikeIgnoreCaseAndIsDeleteFalse(Pageable pageable,String title);
    List<Post> findAllByOrderByTitleAsc();

    List<Post> findPostByStartTimeGreaterThanEqual(LocalDateTime startTime);

    List<Post> findPostByEndTimeLessThanEqual(LocalDateTime endTime);

    @Query("SELECT p, AVG(t.tour.price) AS avgPrice FROM Post p JOIN p.tours t GROUP BY p ORDER BY avgPrice ASC")
    List<Post> findAverageTourPriceOrderByAsc();
}
