package com.fitwise.service;

import java.io.File;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;

@Service
public class S3FileHandlingService {
	
	@Autowired
	private AmazonS3 amazonS3;
	
	public void uploadFile(String bucketName, String path, File file) {
		amazonS3.putObject(new PutObjectRequest(bucketName, path, file).withCannedAcl(CannedAccessControlList.PublicRead));
	}
}
