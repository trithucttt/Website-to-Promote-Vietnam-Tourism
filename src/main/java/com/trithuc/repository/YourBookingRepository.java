package com.trithuc.repository;

import com.trithuc.model.User;
import com.trithuc.model.YourBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface YourBookingRepository extends JpaRepository<YourBooking,Long> {
    List<YourBooking> findByUser(User user);

    @Query("SELECT COUNT(b.id) FROM YourBooking b JOIN b.tourbooking_items ti JOIN ti.tour t JOIN t.manager m WHERE m.id = :businessId")
    Long countToursBookedByBusiness(@Param("businessId") Long businessId);

    @Query("SELECT COUNT(DISTINCT b.user.id) FROM YourBooking b JOIN b.tourbooking_items ti JOIN ti.tour t JOIN t.manager m WHERE m.id = :businessId")
    Long countUniqueCustomersByBusiness(@Param("businessId") Long businessId);

    @Query("SELECT COUNT(b.id) FROM YourBooking b JOIN b.tourbooking_items ti JOIN ti.tour t JOIN t.manager m WHERE m.id = :businessId AND ti.status = 'CONFIRM'")
    Long countCompletedToursByBusiness(@Param("businessId") Long businessId);

    @Query("SELECT COUNT(b.id) FROM YourBooking b JOIN b.tourbooking_items ti JOIN ti.tour t JOIN t.manager m WHERE m.id = :businessId AND ti.status = 'REJECT'")
    Long countCanceledToursByBusiness(@Param("businessId") Long businessId);

    List<YourBooking> findByUserId(Long businessId);
}
