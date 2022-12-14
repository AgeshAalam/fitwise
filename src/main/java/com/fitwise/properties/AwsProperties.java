package com.fitwise.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AwsProperties {
	
	@Value("${aws.access-key}")
	private String awsAccessKey;
	
	@Value("${aws.secret-key}")
	private String awsSecretKey;
	
	@Value("${aws.region}")
	private String awsRegion;
	
	@Value("${aws.s3.bucket-resources}")
	private String awsS3BucketResources;

	@Value("${aws.resource.baseurl}")
	private String awsResourceBaseUrl;

}
