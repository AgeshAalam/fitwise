package com.fitwise.repository.calendar;

import com.fitwise.entity.User;
import com.fitwise.entity.calendar.UserKloudlessAccount;
import com.fitwise.entity.calendar.UserKloudlessCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserKloudlessCalendarRepository extends JpaRepository<UserKloudlessCalendar, Long> {

    List<UserKloudlessCalendar> findByUserAndCalendarId(final User user, final String calendarId);

    List<UserKloudlessCalendar> findByUserAndUserKloudlessAccount(final User user, final UserKloudlessAccount userKloudlessAccount);

    List<UserKloudlessCalendar> findByUserAndUserKloudlessAccountAndCalendarId(final User user, final UserKloudlessAccount userKloudlessAccount, final String clendarId);

    List<UserKloudlessCalendar> findByUserAndPrimaryCalendar(User user, boolean isPrimary);

    List<UserKloudlessCalendar> findByUserKloudlessAccountAndPrimaryCalendar(UserKloudlessAccount userKloudlessAccount, boolean isPrimary);

    List<UserKloudlessCalendar> findByUser(User user);
    
    List<UserKloudlessCalendar> findByUserAndUserKloudlessAccountAndCalendarName(final User user, final UserKloudlessAccount userKloudlessAccount, final String clendarName);
}
