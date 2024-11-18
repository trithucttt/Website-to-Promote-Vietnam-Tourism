package com.trithuc.repository;

import com.trithuc.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    @Query("SELECT SUM(p.total) FROM Payment p JOIN p.yourBooking b JOIN b.tourbooking_items ti JOIN ti.tour t JOIN t.manager m WHERE m.id = :businessId")
    Double calculateTotalRevenueByBusiness(@Param("businessId") Long businessId);


}
