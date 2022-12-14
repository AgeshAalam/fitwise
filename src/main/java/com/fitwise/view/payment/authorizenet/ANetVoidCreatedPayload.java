package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ANetVoidCreatedPayload {
    private int responseCode;
    private String avsResponse;
    private Double authAmount;
    private String merchantReferenceId;
    private String entityName;
    private String id;
}