package com.fitwise.view.order;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderHistoryResponseViewOfAMember {

    private  long totalOrderCount;

    List<OrderHistoryTileResponseViewOfAMember> orders;
}
