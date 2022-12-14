package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlyEnrollmentView {
    private int weekId;
    private String weekName;
    private int enrollmentCount;
    private String weekDuration;
}
