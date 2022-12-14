package com.fitwise.repository.qbo;

import com.fitwise.entity.Programs;
import com.fitwise.entity.instructor.Tier;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.qbo.QboProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing product qbo entity in fitwise
 */
@Repository
public interface QboProductCategoryRepository extends JpaRepository<QboProductCategory, Long> {

    List<QboProductCategory> findTop1ByCategoryName(final String categoryName);

    List<QboProductCategory> findTop1ByProgram(final Programs program);

    List<QboProductCategory> findTop1BySubscriptionPackage(final SubscriptionPackage subscriptionPackage);

    List<QboProductCategory> findTop1ByTier(final Tier tier);

}
