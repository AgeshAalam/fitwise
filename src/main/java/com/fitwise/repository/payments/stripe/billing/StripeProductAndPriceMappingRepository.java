package com.fitwise.repository.payments.stripe.billing;

import com.fitwise.entity.payments.stripe.billing.StripeProductAndPriceMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StripeProductAndPriceMappingRepository extends JpaRepository<StripeProductAndPriceMapping, Long> {
    List<StripeProductAndPriceMapping> findByProductId(String productId);

    List<StripeProductAndPriceMapping> findByProductIdAndPrice(String productId, Double price);
}
