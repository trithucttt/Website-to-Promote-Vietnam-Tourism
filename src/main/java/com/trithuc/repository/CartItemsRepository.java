package com.trithuc.repository;

import com.trithuc.model.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemsRepository extends JpaRepository<CartItems, Long> {
    CartItems findByPostTourIdAndUserId(Long postTourId, Long userId);

    List<CartItems> findByUserId(Long id);
}
