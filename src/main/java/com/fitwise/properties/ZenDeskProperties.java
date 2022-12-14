package com.fitwise.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ZenDeskProperties {

    @Value("${zendesk.api.login.key}")
    private String apiKey;

    @Value("${zendesk.api.domain.url}")
    private String domainUrl;

    @Value("${zendesk.support.phone.number}")
    private String supportPhoneNumber;
}
