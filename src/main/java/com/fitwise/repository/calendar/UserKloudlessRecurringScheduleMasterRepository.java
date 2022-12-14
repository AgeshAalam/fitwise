package com.fitwise.repository.calendar;

import com.fitwise.entity.calendar.UserKloudlessRecurringScheduleMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserKloudlessRecurringScheduleMasterRepository extends JpaRepository<UserKloudlessRecurringScheduleMaster, Long> {
}
