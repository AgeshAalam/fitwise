package com.fitwise.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ZoomProperties {

    @Value("${zoom.client.id}")
    private String clientId;

    @Value("${zoom.client.secret}")
    private String clientSecret;

    @Value("${zoom.client.redirect.url}")
    private String redirectUrl;

    @Value("${zoom.client.verification.token}")
    private String verificationToken;

}
