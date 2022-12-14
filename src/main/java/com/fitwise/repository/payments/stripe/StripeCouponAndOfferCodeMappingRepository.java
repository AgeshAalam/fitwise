package com.fitwise.repository.payments.stripe;

import com.fitwise.entity.discounts.OfferCodeDetail;
import com.fitwise.entity.payments.stripe.StripeCouponAndOfferCodeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 20/11/20
 */
@Repository
public interface StripeCouponAndOfferCodeMappingRepository extends JpaRepository<StripeCouponAndOfferCodeMapping, Long> {

    /**
     * @param offerCodeDetail
     * @return
     */
    StripeCouponAndOfferCodeMapping findByOfferCodeDetail(OfferCodeDetail offerCodeDetail);

    StripeCouponAndOfferCodeMapping findByStripeCouponId(String stripeCouponId);

}
