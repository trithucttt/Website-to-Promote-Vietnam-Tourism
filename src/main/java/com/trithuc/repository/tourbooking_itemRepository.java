package com.trithuc.repository;

import com.trithuc.model.YourBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.trithuc.model.tourbooking_item;

import java.util.List;

@Repository
public interface tourbooking_itemRepository extends JpaRepository<tourbooking_item,Long> {
    List<tourbooking_item> findByYourbooking(YourBooking yourBooking);

    @Query("SELECT tbi.id AS tourBookingItemId " +
            "FROM tourbooking_item tbi " +
            "JOIN tbi.yourbooking yb " +
            "JOIN tbi.tour t " +
            "JOIN t.manager u " +
            "WHERE u.username = :username AND tbi.status = :status")
    List<Long> findTourBookingItemIdWithStatusByUsernameBusiness(@Param("username") String username, @Param("status") String status);
}
