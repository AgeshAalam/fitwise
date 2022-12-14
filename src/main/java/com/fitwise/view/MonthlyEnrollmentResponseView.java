package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MonthlyEnrollmentResponseView {
    private int totalCount;
    private List<MonthlyEnrollmentView> enrollmentPerWeek;
}
