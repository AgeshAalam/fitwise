package com.fitwise.model;

import lombok.Data;

import java.util.List;

@Data
public class MemberCalendarFilterModel {

    private List<Long> subscriptionPackageIds;
}
