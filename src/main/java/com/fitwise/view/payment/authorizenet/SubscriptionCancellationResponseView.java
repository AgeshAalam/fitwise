package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionCancellationResponseView extends AuthNetWebHookParentResponseView{
    SubscriptionCancellationPayload payload;
}

