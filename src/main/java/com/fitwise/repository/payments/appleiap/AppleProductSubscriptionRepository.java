package com.fitwise.repository.payments.appleiap;



import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.payments.appleiap.AppleProductSubscription;
import com.fitwise.entity.payments.appleiap.AppleSubscriptionStatus;

public interface AppleProductSubscriptionRepository extends JpaRepository<AppleProductSubscription, Long> {
	AppleProductSubscription findTop1ByProgramAndUserOrderByModifiedDateDesc(Programs program,User user);
	AppleProductSubscription findTop1ByProgramAndUserAndAppleSubscriptionStatusOrderByModifiedDateDesc(Programs program,User user,AppleSubscriptionStatus appleSubsStatus);
	//List<AppleProductSubscription> findByProgramAndUserAndEventIn(Programs program,User user,List<String> statusList);
	List<AppleProductSubscription> findByProgramAndUserAndEvent(Programs program,User user,String status);
	AppleProductSubscription findTop1ByTransactionIdAndOriginalTransactionIdOrderByModifiedDateDesc(String transactionId,String originalTransactioId);
	//List<AppleProductSubscription> findByProgramAndUserAndEventInAndCreatedDateBetween(Programs program,User user,List<String> statusList,Date date1,Date date2);
	List<AppleProductSubscription> findByProgramAndUserAndEventInAndCreatedDateLessThanEqual(Programs program,User user,List<String> statusList,Date date);
	AppleProductSubscription findTop1ByTransactionIdOrderByModifiedDateDesc(String transactionId);
	
}
