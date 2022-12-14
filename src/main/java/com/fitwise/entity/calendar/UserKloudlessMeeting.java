package com.fitwise.entity.calendar;

import com.fitwise.entity.TimeSpan;
import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class UserKloudlessMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userKloudlessMeetingId;

    private String meetingId;

    @OneToOne
    private CalendarMeetingType calendarMeetingType;

    private String name;

    private Date startDate;

    private Date startDateInUtc;

    private Date endDate;

    private Date endDateInUtc;

    private String timeZone;
    
    private String eventDescription;

    private int meetingDurationInDays;

    @ManyToOne
    private UserKloudlessCalendar userKloudlessCalendar;

    @OneToOne(cascade = CascadeType.DETACH)
    private TimeSpan duration;

    @ManyToOne
    private User user;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String meetingWindow;
    
    @Column(columnDefinition = "MEDIUMTEXT")
    private String availability;
    
}
