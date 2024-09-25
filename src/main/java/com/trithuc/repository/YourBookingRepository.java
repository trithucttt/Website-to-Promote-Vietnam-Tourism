package com.trithuc.repository;

import com.trithuc.model.User;
import com.trithuc.model.YourBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YourBookingRepository extends JpaRepository<YourBooking,Long> {
    List<YourBooking> findByUser(User user);
}
