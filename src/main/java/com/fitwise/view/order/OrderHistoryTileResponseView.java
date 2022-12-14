package com.fitwise.view.order;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class OrderHistoryTileResponseView {
    private String orderId;
    private String programName;
    private String orderStatus;
    private String orderDate;
    private Date orderDateTimeStamp;
    private boolean canReOrder = false;
    private String subscriptionType;
}
