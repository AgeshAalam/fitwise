package com.fitwise.repository;

import com.fitwise.entity.Promotions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotions, Long> {

    Promotions findByPromotionId(final Long promotionId);
}
