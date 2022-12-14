package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ANetVoidCreatedResponseView extends AuthNetWebHookParentResponseView {
    ANetVoidCreatedPayload payload;
}
