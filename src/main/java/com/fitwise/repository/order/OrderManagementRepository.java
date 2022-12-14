package com.fitwise.repository.order;

import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.instructor.Tier;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.common.OrderManagement;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderManagementRepository extends JpaRepository<OrderManagement, Long>, JpaSpecificationExecutor<OrderManagement> {
    OrderManagement findTop1ByOrderIdOrderByCreatedDateDesc(String orderId);
    OrderManagement findTop1ByOrderId(String orderId);
    OrderManagement findTop1ByUserAndProgramOrderByCreatedDateDesc(final User user, final Programs program);
    OrderManagement findTop1ByUserAndTierOrderByCreatedDateDesc(final User user, final Tier tier);

    List<OrderManagement> findByProgramOwnerUserId(long instructorId);

    /**
     * @param user
     * @param subscriptionPackage
     * @return
     */
    OrderManagement findTop1ByUserAndSubscriptionPackageOrderByCreatedDateDesc(User user, SubscriptionPackage subscriptionPackage);

    /**
     * @param instructorId
     * @return
     */
    List<OrderManagement> findBySubscriptionPackageOwnerUserId(long instructorId);

}
