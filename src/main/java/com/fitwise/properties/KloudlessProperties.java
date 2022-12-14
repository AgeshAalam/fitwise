package com.fitwise.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KloudlessProperties {

    @Value("${kloudless.app.id}")
    private String applicationId;

    @Value("${kloudless.api.key}")
    private String kloudlessAPIKey;

}
