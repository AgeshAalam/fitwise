package com.fitwise.repository.calendar;

import com.fitwise.entity.calendar.CalendarMeetingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarMeetingTypeRepository extends JpaRepository<CalendarMeetingType, Long> {

    CalendarMeetingType findByMeetingTypeId(final Long meetingId);
}
