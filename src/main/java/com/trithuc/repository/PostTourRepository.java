package com.trithuc.repository;

import com.trithuc.model.PostTour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostTourRepository extends JpaRepository<PostTour,Long> {

    Optional<PostTour> findByPostIdAndTourId(Long postId, Long tourId);
}
