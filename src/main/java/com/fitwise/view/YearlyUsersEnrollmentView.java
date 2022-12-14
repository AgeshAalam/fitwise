package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class YearlyUsersEnrollmentView {
    private int totalEnrollmentCount;
    private List<YearsEnrollmentResponseView> enrollmentPerMonth;
}
