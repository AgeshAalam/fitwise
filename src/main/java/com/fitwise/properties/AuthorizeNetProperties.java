package com.fitwise.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AuthorizeNetProperties {

    @Value("${auth.api.login.id}")
    private String loginId;

    @Value("${auth.api.transaction.key}")
    private String transactionKey;

    @Value("${auth.api.transaction.env}")
    private String environment;
}
