package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class InstructorProgramsPerformanceViewResponse {

    private String programTitle;
    private double percentage;
    private BigDecimal rating;

}
