package com.trithuc.repository;

import com.trithuc.model.Comment;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByPostTourId(Long id);

    @Query("SELECT COUNT(c.id) FROM Comment c WHERE c.postTour.post.id = :postId")
    Double findAverageNumberOfCommentsByPostId(@Param("postId") Long postId);

}
