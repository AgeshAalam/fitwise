package com.fitwise.repository.calendar;

import com.fitwise.entity.User;
import com.fitwise.entity.calendar.UserKloudlessAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserKloudlessTokenRepository extends JpaRepository<UserKloudlessAccount, Long> {

    List<UserKloudlessAccount> findByUser(final User user);

    List<UserKloudlessAccount> findByUserAndAccountId(final User user, final String accountId);
    
    List<UserKloudlessAccount> findByUserAndProfileId(final User user, final String profileId);

    UserKloudlessAccount findByAccountId(final String accountId);

    UserKloudlessAccount findTop1ByAccountId(final String accountId);

    List<UserKloudlessAccount> findByUserAndActive(final User user, final boolean status);
}
