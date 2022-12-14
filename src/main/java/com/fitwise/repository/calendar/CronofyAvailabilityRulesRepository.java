package com.fitwise.repository.calendar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import com.fitwise.entity.calendar.CronofyAvailabilityRules;
import com.fitwise.entity.calendar.UserKloudlessMeeting;

@Repository
public interface CronofyAvailabilityRulesRepository extends JpaRepository<CronofyAvailabilityRules, Long> {
   
	
	Optional<CronofyAvailabilityRules> findByUserKloudlessMeetingAndUserCronofyAvailabilityRulesId(final UserKloudlessMeeting UserKloudlessMeeting, final Long userCronofyAvailabilityRulesId);
	
	List<CronofyAvailabilityRules> findByUserKloudlessMeeting(final UserKloudlessMeeting UserKloudlessMeeting);
	
	CronofyAvailabilityRules findByUserCronofyAvailabilityRulesId(final Long userCronofyAvailabilityRulesId);

}
