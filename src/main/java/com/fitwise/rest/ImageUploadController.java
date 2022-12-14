package com.fitwise.rest;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.UploadImageService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * The Class ImageUploadController.
 */
@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/image")
public class ImageUploadController {

    /** The upload image service. */
    @Autowired
    private UploadImageService uploadImageService;

    /**
     * Upload image.
     *
     * @param file the file
     * @param type the type
     * @return the response model
     * @throws ApplicationException the application exception
     */
    @PostMapping("/uploadImage")
    public ResponseModel uploadImage(@RequestParam MultipartFile file, @RequestParam String type) {
        return new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_IMAGE_SAVED, uploadImageService.uploadImage(file, type));
    }
}
