package com.fitwise.repository.payments.stripe.billing;

import com.fitwise.entity.payments.stripe.billing.StripeProductAndProgramMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeProductAndProgramMappingRepository extends JpaRepository<StripeProductAndProgramMapping, Long> {
    StripeProductAndProgramMapping findByProgramProgramIdAndIsActive(Long programId, boolean isActive);

    /**
     * @param programId
     * @return
     */
    StripeProductAndProgramMapping findByProgramProgramId(Long programId);
}
