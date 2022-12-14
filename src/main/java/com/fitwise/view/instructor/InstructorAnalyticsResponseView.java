package com.fitwise.view.instructor;

import lombok.Getter;
import lombok.Setter;

import javax.swing.plaf.basic.BasicIconFactory;
import java.math.BigDecimal;

@Getter
@Setter
public class InstructorAnalyticsResponseView {

    private double upcomingPayment;

    private String upcomingPaymentFormatted;

    private double overallRevenue;

    private String overAllRevenueFormatted;

    private int totalSubscriptions;

    private int totalPrograms;

    private BigDecimal overallRating;

    private String name;




}
