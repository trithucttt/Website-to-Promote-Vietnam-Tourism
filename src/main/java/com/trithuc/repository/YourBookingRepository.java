package com.trithuc.repository;

import com.trithuc.model.YourBooking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YourBookingRepository extends JpaRepository<YourBooking,Long> {
}
