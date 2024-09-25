package com.trithuc.repository;

import com.trithuc.model.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District,Long> {
    List<District> findByCityId(Long id);
}
