package com.trithuc.repository;

import com.trithuc.model.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WardRepository extends JpaRepository<Ward,Long> {
    List<Ward> findByDistrictId(Long id);
}
