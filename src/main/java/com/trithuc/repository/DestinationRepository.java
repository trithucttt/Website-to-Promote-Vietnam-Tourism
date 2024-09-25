package com.trithuc.repository;

import com.trithuc.model.Destination;
import com.trithuc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DestinationRepository extends JpaRepository<Destination,Long> {
    @Query("SELECT d FROM Destination d WHERE d.manager = :manager")
    List<Destination> findDestinationsByManager(@Param("manager") User manager);

    @Query("SELECT t FROM Destination t WHERE t.manager.id = :manager")
    List<Destination> findToursByManagerId(@Param("manager") Long manager);

    @Query("SELECT d FROM Destination d JOIN d.tours t WHERE t.id = :tourId")
    List<Destination> findDestinationsByTourId(@Param("tourId") Long tourId);

    @Query("SELECT t.image_destination FROM Destination t WHERE t.id = :desId")
    String findImageNameByDestinationId(Long desId);
    @Query(value = "SELECT MAX(id) FROM Destination")
    Long getMaxId();
}
