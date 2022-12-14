package com.fitwise.properties;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class CronofyProperties {

	@Value("${cronofy.app.clientId}")
	private String clientId;

	@Value("${cronofy.api.clientSecret}")
	private String clientSecret;
	
	@Value("${cronofy.client.redirect.uri}")
	private String redirectUri;
}