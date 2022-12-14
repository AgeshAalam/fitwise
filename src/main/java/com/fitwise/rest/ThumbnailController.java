package com.fitwise.rest;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.User;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.ThumbnailService;
import com.fitwise.view.ResponseModel;
import com.fitwise.model.thumbnail.SystemThumbnailUploadModel;
import com.fitwise.view.ThumbnailFilterView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/thumbnail")
public class ThumbnailController {

    @Autowired
    private ThumbnailService thumbnailService;

    @Autowired
    private UserComponents userComponents;


    @PostMapping(value = "/uploadCustomThumbnail")
    public ResponseModel getPrograms(@RequestParam MultipartFile file) throws ApplicationException, IOException {
        return thumbnailService.uploadCustomThumbnail(file);
    }

    @PostMapping(value = "/uploadSystemThumbnail")
    public ResponseModel uploadSystemThumbnail( @RequestBody SystemThumbnailUploadModel systemThumbnailUploadView) throws ApplicationException {
        User user = userComponents.getUser();
        return thumbnailService.uploadSystemThumbnail(user,systemThumbnailUploadView);
    }

    /**
     * To get all thumbnails uploaded by instructor
     *
     * @param pageNo
     * @param pageSize
     * @return
     * @throws ApplicationException
     * @throws IOException
     */
    @GetMapping(value = "/getAllCustomThumbnails")
    public ResponseModel getAllCustomThumbnails(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam Optional<String> searchName) throws ApplicationException {
        return thumbnailService.getCustomThumbnails(pageNo, pageSize, searchName);
    }

    /**
     * Get all system thumbnails
     * @param pageNo
     * @param pageSize
     * @param thumbnailMainTagId
     * @param thumbnailSubTagId
     * @param searchName
     * @param thumbnailFilters
     * @return
     * @throws ApplicationException
     */
    @PutMapping(value = "/getAllSystemThumbnails")
    public ResponseModel getAllSystemThumbnails(@RequestParam int pageNo, @RequestParam int pageSize,
                                                @RequestParam long thumbnailMainTagId, @RequestParam long thumbnailSubTagId, @RequestParam Optional<String> searchName, @RequestBody List<ThumbnailFilterView> thumbnailFilters) throws ApplicationException {
        return thumbnailService.getSystemThumbnails(pageNo, pageSize, thumbnailMainTagId,thumbnailSubTagId,searchName,thumbnailFilters);
    }


    @GetMapping(value = "/getAllSystemThumbnailTags")
    public ResponseModel getSystemThumbnailTags() throws ApplicationException {
        return thumbnailService.getSystemTags();
    }

    @DeleteMapping(value = "/deleteThumbnailFromLibrary")
    public ResponseModel deleteThumbnailFromLibrary(@RequestParam long thumbnailId) throws ApplicationException {
        return thumbnailService.deleteThumbnailFromLibrary(thumbnailId);
    }


    @GetMapping(value = "/getThumbnail")
    public ResponseModel getThumbnail(@RequestParam long thumbnailId) throws ApplicationException {
        return thumbnailService.getThumbnail(thumbnailId);
    }

    /**
     * API for bulk uploading
     * @param csvFile
     * @param driveLink
     * @return
     * @throws ApplicationException
     */
    @PostMapping(value = "/bulkUpload/upload")
    public ResponseModel bulkUpload(@RequestParam MultipartFile csvFile, @RequestParam String driveLink) throws ApplicationException {
        thumbnailService.bulkUpload(csvFile, driveLink);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_THUMBNAIL_BULK_UPLOAD_INITIATED);
        response.setPayload(null);

        return response;
    }

    /**
     * API for L1 page
     * @param pageNo
     * @param pageSize
     * @return
     * @throws ApplicationException
     */
    @GetMapping(value = "/bulkUpload/list")
    public ResponseModel bulkUploadList(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy) throws ApplicationException {
        Map<String, Object> responseMap = thumbnailService.bulkUploadList(pageNo, pageSize, sortOrder, sortBy);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(responseMap);

        return response;
    }

    /**
     * API for L2 page
     * @param bulkUploadId
     * @param pageNo
     * @param pageSize
     * @return
     * @throws ApplicationException
     */
    @GetMapping(value = "/bulkUpload/details")
    public ResponseModel bulkUploadDetails(@RequestParam Long bulkUploadId, @RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy) throws ApplicationException {
        Map<String, Object> responseMap = thumbnailService.bulkUploadDetails(bulkUploadId, pageNo, pageSize, sortOrder, sortBy);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(responseMap);

        return response;
    }

    /**
     * API for L2 page - download csv
     * @param bulkUploadId
     * @return
     * @throws ApplicationException
     */
    @GetMapping(value = "/bulkUpload/details/downloadCsv")
    public ResponseEntity<InputStreamResource> bulkUploadDetailsCsv(@RequestParam Long bulkUploadId) throws ApplicationException {
        ByteArrayInputStream byteArrayInputStream = thumbnailService.bulkUploadDetailsCsv(bulkUploadId);
        String filename = "upload_failures.csv";
        InputStreamResource file = new InputStreamResource(byteArrayInputStream);

        HttpHeaders httpHeaders = getHttpHeaders(filename);

        return new ResponseEntity<>(file, httpHeaders, OK);
    }

    /**
     * Sample csv download API
     * @return
     * @throws ApplicationException
     */
    @GetMapping(value = "/bulkUpload/csv/sample")
    public ResponseEntity<InputStreamResource> bulkUploadSampleCsv() throws ApplicationException {
        ByteArrayInputStream byteArrayInputStream = thumbnailService.bulkUploadSampleCsv();
        String filename = "sample.csv";
        InputStreamResource file = new InputStreamResource(byteArrayInputStream);

        HttpHeaders httpHeaders = getHttpHeaders(filename);

        return new ResponseEntity<>(file, httpHeaders, OK);
    }

    private HttpHeaders getHttpHeaders(String filename) {
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.parseMediaType("application/csv"));
        respHeaders.setContentDispositionFormData("attachment", filename);
        return respHeaders;
    }


}

