package com.fitwise.view.calendar;

import com.fitwise.entity.calendar.UserKloudlessCalendar;
import lombok.Data;

import java.util.List;

@Data
public class KloudlessCalendarResponseView {

    private String accountId;

    private boolean isActive;

    private String accountEmail;

    private String accountToken;
    
    private String profileId;

    private List<KloudlessCalendarModel> kloudlessCalendars;
}
