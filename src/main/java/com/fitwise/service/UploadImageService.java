package com.fitwise.service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Images;
import com.fitwise.entity.User;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.AwsProperties;
import com.fitwise.repository.ImageRepository;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.utils.Convertions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;

/**
 * The Class UploadImageService.
 */
@Slf4j
@Service
public class UploadImageService {

	/** The s 3 file handling service. */
	@Autowired
	private S3FileHandlingService s3FileHandlingService;

	/** The aws properties. */
	@Autowired
	private AwsProperties awsProperties;

	/** The image repository. */
	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private ValidationService validationService;

	@Autowired
	private UserComponents userComponents;

	/**
	 * Upload image.
	 *
	 * @param file the file
	 * @param filePath the file path
	 * @return the images
	 * @throws ApplicationException the application exception
	 */
	public Images uploadImage(File file, String filePath, String fileName) {
		User user = userComponents.getUser();
		return uploadImage(user,file, filePath, fileName);
	}

	public Images uploadImage(User user, File file, String filePath, String fileName) {
		Date uploadImageStart = new Date();
		String userEmail = "Admin Upload";
		if(user != null){
			userEmail = user.getEmail();
		}
		log.info("uploadImage() started at : " + uploadImageStart + " by " + userEmail);
		Images savedImage;
		try {
			filePath = filePath + File.separator + file.getName();
			s3FileHandlingService.uploadFile(awsProperties.getAwsS3BucketResources(), filePath, file);
			Images image = new Images();
			image.setImagePath(awsProperties.getAwsResourceBaseUrl() + File.separator + filePath);
			image.setFileName(fileName);
			savedImage = imageRepository.save(image);
		}catch (Exception exception) {
			log.error("File upload exception : " + exception.getMessage());

			throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_FILE_UPLOAD_FAILURE, null);
		}
		Date uploadImageEnd = new Date();
		log.info("uploadImage() end at : " + uploadImageEnd + " by " + userEmail);
		log.info("uploadImage() s3 completion took : " + (uploadImageEnd.getTime() - uploadImageStart.getTime()) + " ms for " + userEmail);
		return savedImage;
	}

	/**
	 * Upload image.
	 *
	 * @param multipartFile the multipart file
	 * @param type the type
	 * @return the images
	 * @throws ApplicationException the application exception
	 */
	public Images uploadImage(MultipartFile multipartFile, String type) {
		long startTimeInMillis = new Date().getTime();
		File file;
		String fileName = multipartFile.getOriginalFilename();
		long profilingStartTimeInMillis;
		long profilingEndTimeInMillis;
		try {
			profilingStartTimeInMillis = new Date().getTime();
			file = Convertions.convertToFile(multipartFile);
			profilingEndTimeInMillis = new Date().getTime();
			log.info("File conversion : time taken in millis :" +(profilingEndTimeInMillis-profilingStartTimeInMillis));
		} catch (ApplicationException e) {
			throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FILE_CONVERSION_FAILURE, null);
		}
		profilingStartTimeInMillis = new Date().getTime();
		if(!validationService.validateImage(file.getPath())){
			throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WRONG_IMAGE_TYPE, null);

		}
		profilingEndTimeInMillis = new Date().getTime();
		log.info("Validate image type : time taken in millis :" +(profilingEndTimeInMillis-profilingStartTimeInMillis));
		String filePath;
		if(type.equalsIgnoreCase("THUMBNAIL")){
			filePath = "thumbnail";
		} else if(type.equalsIgnoreCase("PROFILE")){
			filePath = "profile";
		} else if(type.equalsIgnoreCase("CERTIFICATE")){
			filePath = "certificate";
		} else if(type.equalsIgnoreCase("AWARD")){
			filePath = "award";
		} else if(type.equalsIgnoreCase("COVERIMAGE")){
			filePath = "coverImage";
		} else{
			throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WRONG_IMAGE_TYPE, null);
		}
		Images image =  uploadImage(file, filePath, fileName);
		profilingEndTimeInMillis = new Date().getTime();
		log.info("Image upload Total time taken in millis :" +(profilingEndTimeInMillis-startTimeInMillis));
        return image;
	}

}