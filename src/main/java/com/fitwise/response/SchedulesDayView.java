package com.fitwise.response;

import lombok.Data;
import java.util.List;

@Data
public class SchedulesDayView {

    private String bookedDate;

    private List<BookedScheduleView> bookedSchedules;

}

