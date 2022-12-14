package com.fitwise.specifications.jpa.dao;

import com.fitwise.entity.PlatformType;
import com.fitwise.entity.Programs;
import com.fitwise.entity.payments.common.OrderManagement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class ManageSubscriptionDAO {

    private PlatformType subscribedViaPlatform;

    private Date subscribedDate;

    private Programs program;

    private OrderManagement orderManagement;
}
