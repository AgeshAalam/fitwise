package com.fitwise.view.order;

import lombok.Getter;
import lombok.Setter;
import retrofit2.http.GET;

@Getter
@Setter
public class OrderResponseView {
    private String orderId;
}
