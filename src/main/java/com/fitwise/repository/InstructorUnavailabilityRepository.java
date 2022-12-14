package com.fitwise.repository;


import com.fitwise.entity.InstructorUnavailability;
import com.fitwise.entity.User;
import com.fitwise.entity.calendar.UserKloudlessCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InstructorUnavailabilityRepository extends JpaRepository<InstructorUnavailability, Long> {

    InstructorUnavailability findByInstructorUnavailabilityId(Long instructorUnavailabilityId);

    List<InstructorUnavailability> findByUserKloudlessCalendar(UserKloudlessCalendar userKloudlessCalendar);

    InstructorUnavailability findByStartDateAndEndDateAndUserKloudlessCalendar(Date startDate, Date endDate, UserKloudlessCalendar userKloudlessCalendar);
    
    List<InstructorUnavailability> findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndUserKloudlessCalendar(Date startDate, Date endDate, UserKloudlessCalendar userKloudlessCalendar);
    							   
    List<InstructorUnavailability> findByUser(User user);
    
}
