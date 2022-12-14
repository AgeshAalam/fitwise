package com.fitwise.service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Images;
import com.fitwise.entity.User;
import com.fitwise.entity.thumbnail.BulkUpload;
import com.fitwise.entity.thumbnail.BulkUploadFailure;
import com.fitwise.entity.thumbnail.BulkUploadFailureCsv;
import com.fitwise.entity.thumbnail.BulkUploadThumbnailMapping;
import com.fitwise.entity.thumbnail.ThumbnailImages;
import com.fitwise.entity.thumbnail.ThumbnailMainTags;
import com.fitwise.entity.thumbnail.ThumbnailSubTags;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.thumbnail.BulkUploadCsvModel;
import com.fitwise.model.thumbnail.SystemThumbnailUploadModel;
import com.fitwise.model.thumbnail.ThumbnailMainTagsModel;
import com.fitwise.model.thumbnail.ThumbnailSubTagsModel;
import com.fitwise.repository.thumbnail.BulkUploadFailureCsvRepository;
import com.fitwise.repository.thumbnail.BulkUploadFailureRepository;
import com.fitwise.repository.thumbnail.BulkUploadRepository;
import com.fitwise.repository.thumbnail.BulkUploadThumbnailMappingRepository;
import com.fitwise.repository.thumbnail.ThumbnailMainTagsRepository;
import com.fitwise.repository.thumbnail.ThumbnailRepository;
import com.fitwise.repository.thumbnail.ThumbnailSubTagsRepository;
import com.fitwise.rest.google.GoogleGDriveAccessController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * Created by Vignesh G on 18/08/20
 */
@Service
@Slf4j
public class BulkUploadService {

    @Autowired
    private ThumbnailService thumbnailService;
    @Autowired
    BulkUploadRepository bulkUploadRepository;
    @Autowired
    private ThumbnailRepository thumbnailRepository;
    @Autowired
    BulkUploadFailureRepository bulkUploadFailureRepository;
    @Autowired
    BulkUploadFailureCsvRepository bulkUploadFailureCsvRepository;
    @Autowired
    BulkUploadThumbnailMappingRepository bulkUploadThumbnailMappingRepository;
    @Autowired
    private GoogleGDriveAccessController authenticationController;
    @Autowired
    private UploadImageService uploadImageService;
    @Autowired
    private ThumbnailMainTagsRepository thumbnailMainTagsRepository;
    @Autowired
    private ThumbnailSubTagsRepository thumbnailSubTagsRepository;

    @Autowired
    private UserComponents userComponents;

    /**
     * Async bulk upload processing
     * @param user
     * @param bulkUpload
     * @param inputStream
     * @param driveLink
     */
    @Async("threadPoolTaskExecutor")
    public void initiateBulkUpload(User user, BulkUpload bulkUpload, InputStream inputStream, String driveLink) {
        try {
            List<BulkUploadCsvModel> bulkUploadCsvModelList = processCSVFile(inputStream);

            String fileId = validateDriveFolderLink(driveLink);

            String folderPath = copyImagesToAppServer(fileId);

            processBulkThumbnailUpload(user,bulkUploadCsvModelList, folderPath, bulkUpload);
        } catch (Exception e) {
            log.error("Exception on thumbnail bulk upload : " + e.getMessage());
            e.printStackTrace();

            bulkUpload.setUploadedTime(new Date());
            bulkUpload.setUploadStatus(KeyConstants.KEY_CONST_FAILURE);
            bulkUploadRepository.save(bulkUpload);
        }
    }

    /**
     * Processing the csv file uploaded
     * @param inputStream
     * @return
     */
    private List<BulkUploadCsvModel> processCSVFile(InputStream inputStream) {

        List<BulkUploadCsvModel> bulkUploadCsvModelList = convertCsvToPOJO(inputStream);

        if (bulkUploadCsvModelList.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BULK_UPLOAD_CSV_EMPTY, MessageConstants.ERROR);
        }
        return bulkUploadCsvModelList;
    }

    /**
     * Convert csv stream into object list
     * @param is
     * @return
     */
    private List<BulkUploadCsvModel> convertCsvToPOJO(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {

            List<BulkUploadCsvModel> bulkUploadCsvModelList = new ArrayList<BulkUploadCsvModel>();

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                BulkUploadCsvModel model = new BulkUploadCsvModel();
                model.setFileName(csvRecord.get(0));
                model.setGender(csvRecord.get(1));
                model.setLocation(csvRecord.get(2));
                model.setFitnessActivity(csvRecord.get(3));
                model.setPeople(csvRecord.get(4));
                model.setEquipment(csvRecord.get(5));
                model.setExerciseMovement(csvRecord.get(6));
                model.setMuscleGroups(csvRecord.get(7));

                bulkUploadCsvModelList.add(model);
            }

            return bulkUploadCsvModelList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BULK_UPLOAD_CSV_WRONG_STRUCTURE, MessageConstants.ERROR);
        }
    }

    /**
     * @param bulkUploadCsvModelList
     * @param folderPath
     * @param bulkUpload
     */
    private void processBulkThumbnailUpload(User user, List<BulkUploadCsvModel> bulkUploadCsvModelList, String folderPath, BulkUpload bulkUpload) {
        int totalImages = bulkUploadCsvModelList.size();
        int failure = 0;
        for (BulkUploadCsvModel bulkUploadCsvModel : bulkUploadCsvModelList) {
            List<ThumbnailImages> thumbnailImagesList = thumbnailRepository.findByTypeAndImagesFileName(KeyConstants.KEY_SYSTEM, bulkUploadCsvModel.getFileName());
            if (!thumbnailImagesList.isEmpty()) {
                BulkUploadFailure bulkUploadFailure = new BulkUploadFailure();
                bulkUploadFailure.setBulkUpload(bulkUpload);
                bulkUploadFailure.setImageTitle(bulkUploadCsvModel.getFileName());
                bulkUploadFailure.setStatus(KeyConstants.KEY_CONST_FAILURE);
                bulkUploadFailure.setMessage(DBConstants.DUPLICATE_IMAGE_TITLE);

                bulkUploadFailureRepository.save(bulkUploadFailure);

                BulkUploadFailureCsv bulkUploadFailureCsv = constructFailureCsvRow(bulkUpload, bulkUploadCsvModel);
                bulkUploadFailureCsvRepository.save(bulkUploadFailureCsv);

                failure++;
                continue;
            }

            SystemThumbnailUploadModel systemThumbnailUploadModel = null;
            try {
                systemThumbnailUploadModel = constructSystemThumbnailModel(bulkUploadCsvModel);
            } catch (ApplicationException e) {
                BulkUploadFailure bulkUploadFailure = new BulkUploadFailure();
                bulkUploadFailure.setBulkUpload(bulkUpload);
                bulkUploadFailure.setImageTitle(bulkUploadCsvModel.getFileName());
                bulkUploadFailure.setStatus(KeyConstants.KEY_CONST_FAILURE);
                bulkUploadFailure.setMessage(e.getMessage());

                bulkUploadFailureRepository.save(bulkUploadFailure);

                BulkUploadFailureCsv bulkUploadFailureCsv = constructFailureCsvRow(bulkUpload, bulkUploadCsvModel);
                bulkUploadFailureCsvRepository.save(bulkUploadFailureCsv);

                failure++;
                continue;
            }

            Long imageId = null;
            try {
                imageId = uploadImageToS3(bulkUploadCsvModel.getFileName(), folderPath);
            } catch (ApplicationException ex) {
                BulkUploadFailure bulkUploadFailure = new BulkUploadFailure();
                bulkUploadFailure.setBulkUpload(bulkUpload);
                bulkUploadFailure.setImageTitle(bulkUploadCsvModel.getFileName());
                bulkUploadFailure.setStatus(KeyConstants.KEY_CONST_FAILURE);
                bulkUploadFailure.setMessage(ex.getMessage());

                bulkUploadFailureRepository.save(bulkUploadFailure);

                BulkUploadFailureCsv bulkUploadFailureCsv = constructFailureCsvRow(bulkUpload, bulkUploadCsvModel);
                bulkUploadFailureCsvRepository.save(bulkUploadFailureCsv);

                failure++;
                continue;
            }
            systemThumbnailUploadModel.setImageId(imageId);
            thumbnailService.uploadSystemThumbnail(user, systemThumbnailUploadModel);

            ThumbnailImages thumbnailImage = thumbnailRepository.findByImagesImageId(imageId);
            BulkUploadThumbnailMapping bulkUploadThumbnailMapping = new BulkUploadThumbnailMapping();
            bulkUploadThumbnailMapping.setThumbnailImages(thumbnailImage);
            bulkUploadThumbnailMapping.setBulkUpload(bulkUpload);
            bulkUploadThumbnailMappingRepository.save(bulkUploadThumbnailMapping);

        }

        bulkUpload.setTotalImages(totalImages);
        bulkUpload.setSuccess(totalImages - failure);
        bulkUpload.setFailure(failure);
        bulkUpload.setUploadStatus(DBConstants.COMPLETED);
        bulkUpload.setUploadedTime(new Date());
        bulkUploadRepository.save(bulkUpload);

        deleteTempFolder(folderPath);
    }

    private BulkUploadFailureCsv constructFailureCsvRow(BulkUpload bulkUpload, BulkUploadCsvModel bulkUploadCsvModel) {
        BulkUploadFailureCsv bulkUploadFailureCsv = new BulkUploadFailureCsv();
        bulkUploadFailureCsv.setBulkUpload(bulkUpload);
        bulkUploadFailureCsv.setFileName(bulkUploadCsvModel.getFileName());
        bulkUploadFailureCsv.setGender(bulkUploadCsvModel.getGender());
        bulkUploadFailureCsv.setLocation(bulkUploadCsvModel.getLocation());
        bulkUploadFailureCsv.setFitnessActivity(bulkUploadCsvModel.getFitnessActivity());
        bulkUploadFailureCsv.setPeople(bulkUploadCsvModel.getPeople());
        bulkUploadFailureCsv.setEquipment(bulkUploadCsvModel.getEquipment());
        bulkUploadFailureCsv.setExerciseMovement(bulkUploadCsvModel.getExerciseMovement());
        bulkUploadFailureCsv.setMuscleGroups(bulkUploadCsvModel.getMuscleGroups());

        return bulkUploadFailureCsv;
    }

    /**
     * Validating google drive folder url
     * @param driveLink
     * @return file id
     */
    private String validateDriveFolderLink(String driveLink) {
        String fileId = null;

        String folderurlDelimiter = "/folders/";

        if (driveLink.contains(folderurlDelimiter)) {
            int startIndex = driveLink.indexOf(folderurlDelimiter) + folderurlDelimiter.length();
            if (driveLink.contains("?")) {
                int endIndex = driveLink.indexOf("?");
                fileId = driveLink.substring(startIndex, endIndex);
            } else {
                fileId = driveLink.substring(startIndex);
            }
        }

        if (fileId == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_DRIVE_FOLDER_LINK_INVALID, MessageConstants.ERROR);
        }

        log.info("Thumbnail bulk upload : Google Drive folder : File ID : " + fileId);

        return fileId;
    }

    /**
     * Copying files to app server.
     * @param driveLink
     * @return
     */
    private String copyImagesToAppServer(String driveLink) {
        String folderPath = "/tmp/bulkupload/upload_" + new Date().getTime() + "/";

        try {
            authenticationController.downloadFiles(driveLink, folderPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_DRIVE_LINK_DOWNLOAD_FAILED, MessageConstants.ERROR);
        }

        return folderPath;
    }

    /**
     * Download each image from google drive into S3 and return image id
     * @param fileName
     * @param folderPath
     * @return
     */
    private Long uploadImageToS3(String fileName, String folderPath) {
        String filePath = folderPath + fileName;
        File imageFile = new File(filePath);
        if (!imageFile.exists()) {
            throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_GDRIVE_DOWNLOAD_FILE_MISSING, MessageConstants.ERROR);
        }
        Images image = uploadImageService.uploadImage(null,imageFile, "thumbnail", fileName);

        return image.getImageId();
    }

    /**
     * Deleting the temp folder where images from G Drive were copied
     * @param folderPath
     */
    private void deleteTempFolder(String folderPath) {
        try {
            if (folderPath != null) {
                File folder = new File(folderPath);
                FileUtils.deleteDirectory(folder);
                log.info("Bulk upload temp folder deleted. Folder Path : " + folderPath);
            }
        } catch (Exception e) {
            log.warn("Exception while deleting bulk thumbnail temp folder : " + e.getMessage());
        }
    }

    /**
     * @param bulkUploadCsvModel
     * @return
     */
    private SystemThumbnailUploadModel constructSystemThumbnailModel(BulkUploadCsvModel bulkUploadCsvModel) {

        List<String> csvFields = bulkUploadCsvModel.getAllFieldsAsList();

        List<ThumbnailMainTagsModel> thumbnailMainTagsModelList = new ArrayList<>();
        for (int i = 1; i < ThumbnailService.CSV_HEADERS.size(); i++) {
            ThumbnailMainTags thumbnailMainTag = thumbnailMainTagsRepository.findByThumbnailMainTagIgnoreCase(ThumbnailService.CSV_HEADERS.get(i).trim());
            if (thumbnailMainTag == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_THUMBNAIL_MAIN_TAG_NAME_INCORRECT, MessageConstants.ERROR);
            } else {
                String csvField = csvFields.get(i);
                if (csvField.isEmpty()) {
                    continue;
                }
                boolean isMultiField = csvField.contains(";");
                if ((isMultiField && !thumbnailMainTag.isMultipleTagsAllowed())) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_MULTIPLE_SUB_TAGS_NOT_ALLOWED, MessageConstants.ERROR);
                }

                //Constructing sub tag model
                List<ThumbnailSubTagsModel> thumbnailSubTagsModelList = new ArrayList<>();
                if (isMultiField) {
                    for (String eachField : csvField.split(";")) {
                        ThumbnailSubTags thumbnailSubTag = thumbnailSubTagsRepository.findByThumbnailMainTagsThumbnailMainTagIdAndThumbnailSubTagIgnoreCase(thumbnailMainTag.getThumbnailMainTagId(), eachField.trim());
                        if (thumbnailSubTag == null) {
                            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_THUMBNAIL_SUB_TAG_NAME_INCORRECT, MessageConstants.ERROR);
                        } else {
                            ThumbnailSubTagsModel thumbnailSubTagsModel = new ThumbnailSubTagsModel();
                            thumbnailSubTagsModel.setThumbnailSubTagId(thumbnailSubTag.getThumbnailSubTagId());
                            thumbnailSubTagsModel.setThumbnailSubTag(thumbnailSubTag.getThumbnailSubTag());

                            thumbnailSubTagsModelList.add(thumbnailSubTagsModel);
                        }
                    }
                } else {
                    ThumbnailSubTags thumbnailSubTag = thumbnailSubTagsRepository.findByThumbnailMainTagsThumbnailMainTagIdAndThumbnailSubTagIgnoreCase(thumbnailMainTag.getThumbnailMainTagId(), csvField.trim());
                    if (thumbnailSubTag == null) {
                        throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_THUMBNAIL_SUB_TAG_NAME_INCORRECT, MessageConstants.ERROR);
                    } else {
                        ThumbnailSubTagsModel thumbnailSubTagsModel = new ThumbnailSubTagsModel();
                        thumbnailSubTagsModel.setThumbnailSubTagId(thumbnailSubTag.getThumbnailSubTagId());
                        thumbnailSubTagsModel.setThumbnailSubTag(thumbnailSubTag.getThumbnailSubTag());

                        thumbnailSubTagsModelList.add(thumbnailSubTagsModel);
                    }
                }

                //Constructing ThumbnailMainTagsModel
                ThumbnailMainTagsModel thumbnailMainTagsModel = new ThumbnailMainTagsModel();
                thumbnailMainTagsModel.setThumbnailMainTagId(thumbnailMainTag.getThumbnailMainTagId());
                thumbnailMainTagsModel.setThumbnailMainTag(thumbnailMainTag.getThumbnailMainTag());
                thumbnailMainTagsModel.setMultipleSubTagsAllowed(thumbnailMainTag.isMultipleTagsAllowed());
                thumbnailMainTagsModel.setThumbnailSubTags(thumbnailSubTagsModelList);

                thumbnailMainTagsModelList.add(thumbnailMainTagsModel);
            }
        }

        //Constructing SystemThumbnailUploadModel
        SystemThumbnailUploadModel systemThumbnailUploadModel = new SystemThumbnailUploadModel();
        systemThumbnailUploadModel.setThumbnailTags(thumbnailMainTagsModelList);

        return systemThumbnailUploadModel;
    }

}
