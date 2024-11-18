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
    @Query("SELECT ti FROM tourbooking_item ti JOIN ti.tour t JOIN t.manager m WHERE m.id = :businessId AND ti.status = 'PROCESS'")
    List<tourbooking_item> findPendingBookingsByBusiness(@Param("businessId") Long businessId);

    List<tourbooking_item> findByTourId(Long tourId);
    @Query("SELECT tbi.id AS tourBookingItemId " +
            "FROM tourbooking_item tbi " +
            "JOIN tbi.yourbooking yb " +
            "JOIN tbi.tour t " +
            "JOIN t.manager u " +
            "WHERE u.username = :username AND tbi.status = :status")
    List<Long> findTourBookingItemIdWithStatusByUsernameBusiness(@Param("username") String username, @Param("status") String status);
}
