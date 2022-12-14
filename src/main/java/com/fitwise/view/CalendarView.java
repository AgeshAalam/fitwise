package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarView {

    private int date;
    private int month;
    private int year;

    public CalendarView(int date, int month, int year) {
        this.date = date;
        this.month = month;
        this.year = year;
    }
}
