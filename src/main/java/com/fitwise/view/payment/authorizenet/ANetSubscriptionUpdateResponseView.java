package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ANetSubscriptionUpdateResponseView extends AuthNetWebHookParentResponseView {
    ANetSubscriptionUpdatePayload payload;
}
