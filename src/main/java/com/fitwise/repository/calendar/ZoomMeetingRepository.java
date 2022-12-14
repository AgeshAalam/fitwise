package com.fitwise.repository.calendar;

import com.fitwise.entity.calendar.ZoomAccount;
import com.fitwise.entity.calendar.ZoomMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoomMeetingRepository extends JpaRepository<ZoomMeeting, Long> {

    Optional<ZoomMeeting> findByMeetingId(String meetingId);

    Optional<ZoomMeeting> findByMeetingIdAndOccurrenceId(String meetingId, String occurrenceId);

    List<ZoomMeeting> findByZoomAccount(ZoomAccount zoomAccount);

}
