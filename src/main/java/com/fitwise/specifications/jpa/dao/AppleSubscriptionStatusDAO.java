package com.fitwise.specifications.jpa.dao;

import com.fitwise.entity.payments.appleiap.AppleSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class AppleSubscriptionStatusDAO {

    private Long programId;

    private AppleSubscriptionStatus appleSubscriptionStatus;

    private Date modifiedDate;
}
