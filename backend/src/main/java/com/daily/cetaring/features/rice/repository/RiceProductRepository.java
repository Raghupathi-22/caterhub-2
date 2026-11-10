package com.daily.cetaring.features.rice.repository;

import com.daily.cetaring.features.rice.entity.RiceProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiceProductRepository extends JpaRepository<RiceProduct, Long> {
    Optional<RiceProduct> findByCode(String code);
}
