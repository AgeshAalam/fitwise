package com.fitwise.repository.calendar;

import com.fitwise.entity.User;
import com.fitwise.entity.calendar.ZoomAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoomAccountRepository extends JpaRepository<ZoomAccount, Long> {

    List<ZoomAccount> findByUser(User user);

    List<ZoomAccount> findByUserAndActive(User user, boolean isActive);

    List<ZoomAccount> findByAccountId(String accountId);

    List<ZoomAccount> findByUserAndAccountId(User user, String accountId);

}
