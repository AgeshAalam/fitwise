package com.fitwise.repository.calendar;

import com.fitwise.entity.User;
import com.fitwise.entity.calendar.CalendarMeetingType;
import com.fitwise.entity.calendar.UserKloudlessAccount;
import com.fitwise.entity.calendar.UserKloudlessCalendar;
import com.fitwise.entity.calendar.UserKloudlessMeeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserKloudlessMeetingRepository extends JpaRepository<UserKloudlessMeeting, Long> {

    List<UserKloudlessMeeting> findByUserKloudlessCalendarAndMeetingId(final UserKloudlessCalendar userKloudlessCalendar, final String meetingId);

    Optional<UserKloudlessMeeting> findByUserKloudlessCalendarAndUserKloudlessMeetingId(final UserKloudlessCalendar userKloudlessCalendar, final Long userKloudlessMeetingId);

    UserKloudlessMeeting findByUserKloudlessMeetingId(final Long userKloudlessMeetingId);
    
    
   // Page<UserKloudlessMeeting> findByNameNotNullAndUserKloudlessCalendar(final UserKloudlessCalendar userKloudlessCalendar, Pageable pageable);
    
   // Page<UserKloudlessMeeting> findByNameNotNullAndUserKloudlessCalendarAndCalendarMeetingType(final UserKloudlessCalendar userKloudlessCalendar, final CalendarMeetingType meetingType, Pageable pageable);
    Page<UserKloudlessMeeting> findByNameNotNullAndUserKloudlessCalendar(final UserKloudlessCalendar userKloudlessCalendar, Pageable pageable);
    
	Page<UserKloudlessMeeting> findByNameNotNullAndUserKloudlessCalendarAndCalendarMeetingType(final UserKloudlessCalendar userKloudlessCalendar, final CalendarMeetingType meetingType, Pageable pageable);
   
    Page<UserKloudlessMeeting> findByMeetingIdNotNullAndUserKloudlessCalendar(final UserKloudlessCalendar userKloudlessCalendar, Pageable pageable);
   
    Page<UserKloudlessMeeting> findByMeetingIdNotNullAndUserKloudlessCalendarAndCalendarMeetingType(final UserKloudlessCalendar userKloudlessCalendar, final CalendarMeetingType meetingType, Pageable pageable);
   
    
    

   
    List<UserKloudlessMeeting> findByUserKloudlessCalendar(final UserKloudlessCalendar userKloudlessCalendar);
    
    List<UserKloudlessMeeting> findByMeetingIdNotNullAndUserKloudlessCalendarAndCalendarMeetingType(final UserKloudlessCalendar userKloudlessCalendar, final CalendarMeetingType meetingType);

    List<UserKloudlessMeeting> findByUserAndUserKloudlessCalendar(User user, UserKloudlessCalendar userKloudlessCalendar);
    
    List<UserKloudlessMeeting> findByStartDateInUtcNotNullAndUserAndUserKloudlessCalendar(User user, UserKloudlessCalendar userKloudlessCalendar);
    


}

