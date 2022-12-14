package com.fitwise.repository.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.User;
import com.fitwise.entity.subscription.TierSubscription;

@Repository
public interface TierSubscriptionRepository extends JpaRepository<TierSubscription, Long> {

	TierSubscription findTop1ByUserUserIdAndTierTierIdOrderBySubscribedDateDesc(Long userId, Long tierId);

	TierSubscription findTop1ByUserUserIdOrderBySubscribedDateDesc(final Long userId);
	
	TierSubscription findByUser(User user);

}
