package com.fitwise.view.instructor;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProgramAnalyticsResponseView {

    private String revenueFormatted;

    private double revenue;

    private int totalSubscriptions;

    private BigDecimal rating;

    private String programTitle;

}
