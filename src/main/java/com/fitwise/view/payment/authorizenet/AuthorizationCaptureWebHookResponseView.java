package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

/**
 * Class that holds the response view of the webhook notification that will be triggered
 * during Authorization and capture event.
 * <p>
 * Event type  net.authorize.payment.auth capture.created
 */

@Getter
@Setter
public class AuthorizationCaptureWebHookResponseView extends AuthNetWebHookParentResponseView {
    private AuthCaptureWebHookPayload payload;
}
