package com.fitwise.entity.calendar;

import com.fitwise.entity.User;
import com.fitwise.entity.packaging.PackageKloudlessMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.util.Date;

@Entity
@Getter
@Setter
public class UserKloudlessSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userKloudlessScheduleId;

    private String scheduleId;

    @ManyToOne
    private UserKloudlessMeeting userKloudlessMeeting;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private SubscriptionPackage subscriptionPackage;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private PackageKloudlessMapping packageKloudlessMapping;

    private Date bookingDate;

    private String scheduleType;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String schedulePayload;

    private String masterScheduleId;

    @ManyToOne
    @JoinColumn(name = "meeting_type_id")
    private CalendarMeetingType meetingTypeId;

    @ManyToOne
    @JoinColumn(name = "recurring_schedule_master_id")
    private UserKloudlessRecurringScheduleMaster userKloudlessRecurringScheduleMaster;

    private String onlineMeetingEntryUrl;

    @OneToOne
    private ZoomMeeting zoomMeeting;
    
    private Boolean isRescheduled;
  
    private String scheduleStartTime;
    
    private String scheduleEndTime;
   
    private String realtimeschedulingId;
    
    private String srealtimeschedulingToken;

}
