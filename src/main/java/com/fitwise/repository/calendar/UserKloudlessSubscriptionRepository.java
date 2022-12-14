package com.fitwise.repository.calendar;

import com.fitwise.entity.calendar.UserKloudlessAccount;
import com.fitwise.entity.calendar.UserKloudlessSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * Created by Vignesh G on 02/02/21
 */
@Repository
public interface UserKloudlessSubscriptionRepository extends JpaRepository<UserKloudlessSubscription, Long> {

    Optional<UserKloudlessSubscription> findByUserKloudlessAccountAndSubscriptionId(UserKloudlessAccount userKloudlessAccount, Long subscriptionId);

    UserKloudlessSubscription findByUserKloudlessAccountUserKloudlessTokenId(Long userKloudlessTokenId);

}
