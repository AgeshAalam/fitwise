package com.fitwise.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fitwise.constants.RestUrlConstants;

@Configuration
public class RestTemplateConfiguration {

    @Autowired
    private RestTemplateExceptionHandlerConfiguration restTemplateErrorHandler;

    @Bean(name = "fcmRestTemplate")
    public RestTemplate mlServiceRestTemplate() {
        return new RestTemplateBuilder().rootUri(RestUrlConstants.URL_FCM).errorHandler(restTemplateErrorHandler).build();
    }
}
