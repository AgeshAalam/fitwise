package com.fitwise.rest;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.service.VimeoService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.VideoStandards;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/vimeo")
public class VimeoController {

    @Autowired
    private VimeoService vimeoService;

    @GetMapping("/getVideoUrl")
    public String getVimeoUrl(@RequestParam int vimeoId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, JSONException {
        return vimeoService.getVimeoUrlFromId(vimeoId);
    }

    /**
     * Get vimeo video standards with url
     * @param vimeoId
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws IOException
     * @throws JSONException
     */
    @GetMapping("/video/standards")
    public ResponseModel getVimeoVideoStandards(@RequestParam int vimeoId) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        ResponseModel responseModel = new ResponseModel();
        try{
            List<VideoStandards> videoStandards = vimeoService.getVimeoVideos(vimeoId);
            if(videoStandards.isEmpty()){
                throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_VIDEO_NOT_FOUND, null);
            }
            responseModel.setStatus(Constants.SUCCESS_STATUS);
            responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
            responseModel.setPayload(videoStandards);
        }catch (ApplicationException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException exception){
            throw exception;
        }
        return responseModel;
    }

}
