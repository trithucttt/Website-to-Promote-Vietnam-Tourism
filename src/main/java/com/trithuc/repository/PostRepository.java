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

    @Query("SELECT t FROM Post t WHERE t.users.id = :user AND t.isDelete = false")
    List<Post> findPostsByUserId(@Param("user") Long user);

    // Search by article title and search for articles with similar tour titles
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN p.tours t " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " )
    List<Post> findByTitleContainingOrTourTitleContaining(String searchTerm);

    Page<Post> findByTitleLikeIgnoreCaseAndIsDeleteFalse(Pageable pageable,String title);
    List<Post> findAllByOrderByTitleAsc();

    List<Post> findPostByStartTimeGreaterThanEqual(LocalDateTime startTime);

    List<Post> findPostByEndTimeLessThanEqual(LocalDateTime endTime);

    @Query("SELECT p, AVG(t.price) AS avgPrice FROM Post p JOIN p.tours t GROUP BY p ORDER BY avgPrice ASC")
    List<Post> findAverageTourPriceOrderByAsc();

    List<Post> findAllByTitleLikeIgnoreCaseAndIsDeleteFalse(String title);

    @Query(value = "SELECT DISTINCT p.* " +
            "FROM post p " +
            "LEFT JOIN tour t ON p.id = t.post_id " +
            "LEFT JOIN destination_in_tour td ON t.id = td.tour_id " +
            "LEFT JOIN destination d ON td.destination_id = d.id " +
            "LEFT JOIN ward w ON d.ward_id = w.id " +
            "LEFT JOIN districts dis ON w.district_id = dis.id " +
            "LEFT JOIN provinces c ON dis.city_id = c.id " +
            "LEFT JOIN administrative_regions ar ON c.administrative_regions_id = ar.id " +
            "WHERE p.is_delete = FALSE " +
            "AND (p.title LIKE %:title% OR :title IS NULL) " +
            "AND (p.start_time >= :startTime OR :startTime IS NULL) " +
            "AND (t.quantity BETWEEN :quantityStart AND :quantityEnd OR :quantityStart  IS NULL OR :quantityEnd IS NULL) " +
            "AND (t.price BETWEEN :priceStart AND :priceEnd OR :priceStart  IS NULL OR :priceEnd IS NULL) " +
            "AND (t.discount BETWEEN :discountStart AND :discountEnd OR :discountStart IS NULL OR :discountEnd IS NULL) " +
            "AND (c.id = :cityId OR :cityId IS NULL) " +
            "AND (ar.name LIKE %:regionName% OR :regionName IS NULL)", nativeQuery = true)
    List<Post> searchPosts(
            @Param("title") String title,
            @Param("startTime") LocalDateTime startTime,
            @Param("quantityStart") Integer quantityStart,
            @Param("quantityEnd") Integer quantityEnd,
            @Param("priceStart") Double priceStart,
            @Param("priceEnd") Double priceEnd,
            @Param("discountStart") Double discountStart,
            @Param("discountEnd") Double discountEnd,
            @Param("cityId") Long cityId,
            @Param("regionName") String regionName);



//             "ORDER BY p.start_time DESC
}
