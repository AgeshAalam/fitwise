package com.fitwise.entity.calendar;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Getter
@Setter
public class UserKloudlessRecurringScheduleMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recurringMasterId;

    private Date start;

    private Date end;

    private String timeZone;

    private String rrule;

    private String exceptionDates;

}
