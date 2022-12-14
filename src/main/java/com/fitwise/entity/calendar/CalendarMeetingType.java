package com.fitwise.entity.calendar;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class CalendarMeetingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meetingTypeId;

    private String meetingType;
}
