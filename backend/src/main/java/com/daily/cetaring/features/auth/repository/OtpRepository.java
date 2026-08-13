package com.daily.cetaring.features.auth.repository;

import com.daily.cetaring.features.auth.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, Long> {
    Optional<OtpEntity> findTopByMobileNumberAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(String mobileNumber, String purpose);
    Optional<OtpEntity> findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(String mobileNumber, String purpose);
    long countByMobileNumberAndPurposeAndCreatedAtAfter(String mobileNumber, String purpose, LocalDateTime createdAtAfter);
    long countByRequesterIpAndCreatedAtAfter(String requesterIp, LocalDateTime createdAtAfter);
}
