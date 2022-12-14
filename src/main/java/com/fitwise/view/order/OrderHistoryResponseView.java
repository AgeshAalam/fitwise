package com.fitwise.view.order;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderHistoryResponseView {
    List<OrderHistoryTileResponseView> orders;

    private long totalOrderCount;
}
