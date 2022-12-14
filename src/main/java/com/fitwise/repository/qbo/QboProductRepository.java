package com.fitwise.repository.qbo;

import com.fitwise.entity.Programs;
import com.fitwise.entity.instructor.Tier;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.qbo.QboProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing product qbo entity in fitwise
 */
@Repository
public interface QboProductRepository extends JpaRepository<QboProduct, Long> {

    List<QboProduct> findByNeedUpdate(final Boolean isNeedUpadte);

    List<QboProduct> findByProgram(final Programs program);

    List<QboProduct> findBySubscriptionPackage(final SubscriptionPackage subscriptionPackage);

    List<QboProduct> findByTier(final Tier tier);
}
