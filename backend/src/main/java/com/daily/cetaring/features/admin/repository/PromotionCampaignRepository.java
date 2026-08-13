package com.daily.cetaring.features.admin.repository;

import com.daily.cetaring.features.admin.entity.PromotionCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionCampaignRepository extends JpaRepository<PromotionCampaign, Long> {

    List<PromotionCampaign> findByBusinessIdOrderByCreatedAtDesc(Long businessId);

    Optional<PromotionCampaign> findByIdAndBusinessId(Long id, Long businessId);
}
