package com.fitwise.entity;


import com.fitwise.entity.calendar.UserKloudlessCalendar;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
public class InstructorUnavailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long instructorUnavailabilityId;

    private Date startDate;

    private Date endDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String schedulePayload;

    private String eventId;

    @ManyToOne
    @JoinColumn(name = "user_kloudless_calendar_id")
    private UserKloudlessCalendar userKloudlessCalendar;

}
