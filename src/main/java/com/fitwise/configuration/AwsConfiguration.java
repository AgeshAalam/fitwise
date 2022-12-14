package com.fitwise.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fitwise.properties.AwsProperties;

@Configuration
public class AwsConfiguration {

	@Autowired
	private AwsProperties awsProperties;

	@Bean
	public AmazonS3 amazonS3() {
		return AmazonS3ClientBuilder.standard().withRegion(awsProperties.getAwsRegion())
				.withCredentials(new AWSStaticCredentialsProvider(
						new BasicAWSCredentials(awsProperties.getAwsAccessKey(), awsProperties.getAwsSecretKey())))
				.build();
	}

}
