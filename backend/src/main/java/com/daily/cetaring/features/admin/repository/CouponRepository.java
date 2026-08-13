package com.daily.cetaring.features.admin.repository;

import com.daily.cetaring.features.admin.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    List<Coupon> findByBusinessIdOrderByCreatedAtDesc(Long businessId);

    Optional<Coupon> findByIdAndBusinessId(Long id, Long businessId);

    boolean existsByCouponCode(String couponCode);
}
