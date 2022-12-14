package com.fitwise.specifications.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InstructorSubscriptionCount {

    private String programType;

    private Long programCount;

    private Long subscriptionCount;

    private Double revenue;
}
