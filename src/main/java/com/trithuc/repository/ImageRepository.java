package com.trithuc.repository;

import com.trithuc.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image,Long> {
    List<Image> findByCommentId(Long id);

    List<Image> findByTourId(Long tourId);

    List<Image> findByTourIdAndImageUrlIn(Long tour_id, Collection<String> imageUrl);

}
