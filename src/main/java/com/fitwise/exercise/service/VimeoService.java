package com.fitwise.exercise.service;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.VimeoConstants;
import com.fitwise.entity.videoCaching.VideoQuality;
import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.model.VimeoModel;
import com.fitwise.interceptors.VimeoServiceInterceptors;
import com.fitwise.model.instructor.VimeoVersioningModel;
import com.fitwise.repository.videoCaching.VideoQualityRepository;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.utils.APIBuilder;
import com.fitwise.utils.APIService;
import com.fitwise.utils.AppUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.VideoStandards;
import com.google.gson.internal.LinkedTreeMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class VimeoService {

    @Value("${vimeo.placeholder.api}")
    private String vimeoUrl;

    @Value("${vimeo.auth.token}")
    private String vimeoToken;

    @Value("${vimeo.folder.project-id}")
    private String vimeoFolderProjectId;

    @Autowired
    public ValidationService validationService;

    @Autowired
    private VideoQualityRepository videoQualityRepository;

    public VimeoModel createVideoPlaceholder(VimeoModel request) throws ApplicationException, IOException {
        doValidateVimeoModel(request);
        String token = VimeoConstants.TOKEN_PREFIX + vimeoToken;
        APIService webService = APIBuilder.builder(vimeoUrl);
        request = webService.createVideoPlaceholder(request, token, "application/json", "application/vnd.vimeo.*+json;version=3.4").execute().body();
        try{
            webService.moveVideo(vimeoFolderProjectId, request.getUri().split("/")[2], VimeoConstants.TOKEN_PREFIX + vimeoToken).execute().body();
        }catch (Exception exception){
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        }
        return request;
    }

    /**
     * Creating the new version for the given vimeo video
     * @param vimeoModel
     * @return
     * @throws IOException
     */
    public LinkedTreeMap<String, Object> crateVideoVersion(String videlUrl, VimeoVersioningModel vimeoModel) throws IOException {
        String token = VimeoConstants.TOKEN_PREFIX + vimeoToken;
        APIService webService = APIBuilder.builder(vimeoUrl);
        return (LinkedTreeMap) webService.createVideoVersion(vimeoModel, videlUrl.split("/")[2], token, "application/json", "application/vnd.vimeo.*+json;version=3.4").execute().body();
    }

    public void doValidateVimeoModel(final VimeoModel model) throws ApplicationException {
        ValidationUtils.throwException(model == null, "Vimeo model cant be null", Constants.BAD_REQUEST);
        ValidationUtils.throwException(ValidationUtils.isEmptyString(model.getName()), "File name cant be null or empty", Constants.BAD_REQUEST);
    }

    /**
     * Method to parse and get Video url from Vimeo ID
     *
     * @param vimeoId
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException
     * @throws JSONException
     */
    public String getVimeoUrlFromId(long vimeoId) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, JSONException {
        /*
         * Constructing Vimeo get api url
         */
        String vimeoUrl = "https://api.vimeo.com/videos/" + vimeoId;

        /*
         * AppUtils.sslExceptionHandler() is used to avoid SSL handshake exception for Rest Template
         */
        String result = "";
        RestTemplate restTemplate = new RestTemplate(AppUtils.sslExceptionHandler());
        try {
            restTemplate.setInterceptors(Collections.singletonList(new VimeoServiceInterceptors(vimeoToken)));
            result = restTemplate.getForObject(vimeoUrl, String.class);
        } catch (Exception exception) {
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        }
        String vimeoVideourl = "";
        if (!result.isEmpty()) {
            /*
             * Parsing and getting data from Vimeo response
             */
            JSONObject vimeoResultObj = new JSONObject(result);
            JSONArray vimeoVideoFiles = (JSONArray) vimeoResultObj.get(KeyConstants.KEY_VIMEO_FILES);

            for (int i = 0; i < vimeoVideoFiles.length(); i++) {
                JSONObject json = vimeoVideoFiles.getJSONObject(i);
                if (!ValidationUtils.isEmptyString(json.getString(KeyConstants.KEY_VIMEO_LINK))) {
                    vimeoVideourl = json.getString(KeyConstants.KEY_VIMEO_LINK);
                    break;
                }
            }
        }
        return vimeoVideourl;
    }

    /**
     * It will get the vimeo video upload status
     * @param rawVimeoUrl
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    public JSONObject getVimeoStatus(String rawVimeoUrl) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String vimeoId = "";
        if (rawVimeoUrl.contains("/")) {
            String[] videoIds = rawVimeoUrl.split("/");
            vimeoId = videoIds[2];
        }
        String vimeoUrl = "https://api.vimeo.com/videos/" + vimeoId;
        RestTemplate restTemplate = new RestTemplate(AppUtils.sslExceptionHandler());
        restTemplate.setInterceptors(Collections.singletonList(new VimeoServiceInterceptors(vimeoToken)));
        JSONObject vimeoVideoObject = null;
        try {
            String result = restTemplate.getForObject(vimeoUrl, String.class);
            vimeoVideoObject = new JSONObject(result);
        } catch (Exception exception) {
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        }
        return vimeoVideoObject;
    }

    public boolean deleteVimeoVideo(String rawVimeoUrl) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        boolean isDeleted = true;
        String vimeoId = "";
        if (rawVimeoUrl.contains("/")) {
            String[] videoIds = rawVimeoUrl.split("/");
            vimeoId = videoIds[2];
        }
        RestTemplate restTemplate = new RestTemplate(AppUtils.sslExceptionHandler());
        restTemplate.setInterceptors(Collections.singletonList(new VimeoServiceInterceptors(vimeoToken)));
        String vimeoUrl = "https://api.vimeo.com/videos/" + vimeoId;
        try {
            restTemplate.delete(vimeoUrl);
        } catch (Exception exception) {
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
            isDeleted = false;
        }
        return isDeleted;
    }

    /**
     * Method to parse and get Video url, quality, type and size from Vimeo ID
     *
     * @param vimeoId
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException
     * @throws JSONException
     */
    public List<VideoStandards> getVimeoVideos(long vimeoId) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, JSONException {
        /*
         * Constructing Vimeo get api url
         */
        String vimeoUrl = "https://api.vimeo.com/videos/" + vimeoId;

        /*
         * AppUtils.sslExceptionHandler() is used to avoid SSL handshake exception for Rest Template
         */
        String result = "";
        RestTemplate restTemplate = new RestTemplate(AppUtils.sslExceptionHandler());
        try {
            restTemplate.setInterceptors(Collections.singletonList(new VimeoServiceInterceptors(vimeoToken)));
            result = restTemplate.getForObject(vimeoUrl, String.class);
        } catch (Exception exception) {
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        }

        List<VideoStandards> videoStandardsList = new ArrayList<>();
        if (!result.isEmpty()) {
            /*
             * Parsing and getting data from Vimeo response
             */
            JSONObject vimeoResultObj = new JSONObject(result);
            JSONArray vimeoVideoFiles = (JSONArray) vimeoResultObj.get(KeyConstants.KEY_VIMEO_FILES);

            for (int i = 0; i < vimeoVideoFiles.length(); i++) {
                VideoStandards videoStandards = new VideoStandards();
                JSONObject json = vimeoVideoFiles.getJSONObject(i);
                if (!ValidationUtils.isEmptyString(json.getString(KeyConstants.KEY_VIMEO_LINK))) {
                    String vimeoVideourl = json.getString(KeyConstants.KEY_VIMEO_LINK);
                    videoStandards.setUrl(vimeoVideourl);
                }
                if (!ValidationUtils.isEmptyString(json.getString(KeyConstants.KEY_VIMEO_QUALITY))) {
                    String vimeoVideoQuality = json.getString(KeyConstants.KEY_VIMEO_QUALITY);
                    videoStandards.setQuality(vimeoVideoQuality);
                }
                if (!ValidationUtils.isEmptyString(json.getString(KeyConstants.KEY_VIMEO_TYPE))) {
                    String vimeoVideoType = json.getString(KeyConstants.KEY_VIMEO_TYPE);
                    videoStandards.setType(vimeoVideoType);
                }
                if (!ValidationUtils.isNullValue(json.getInt(KeyConstants.KEY_VIMEO_SIZE))) {
                    int vimeoVideoSize = json.getInt(KeyConstants.KEY_VIMEO_SIZE);
                    videoStandards.setSize(vimeoVideoSize);
                }
                if(!ValidationUtils.isEmptyString(json.getString(KeyConstants.KEY_VIMEO_PUBLIC_NAME))){
                    String vimeoPublicName = json.getString(KeyConstants.KEY_VIMEO_PUBLIC_NAME);
                    videoStandards.setPublicName(vimeoPublicName);
                }
                videoStandardsList.add(videoStandards);
            }
        }
        List<VideoStandards> videoStandardsResponse = new ArrayList<>();

        List<VideoQuality> videoQuality = videoQualityRepository.findAll();
        Map<String, VideoStandards> videoStandardsMap = new HashMap<>();
        for(VideoStandards videoStandards : videoStandardsList){
            for (VideoQuality videoQuality1 : videoQuality){
                if(videoQuality1.getVimeoVideoQuality().equalsIgnoreCase(videoStandards.getQuality())){
                    if(videoQuality1.getVimeoPublicName().equalsIgnoreCase(videoStandards.getPublicName())){
                        if(!videoStandardsMap.containsKey(videoStandards.getQuality())){
                            videoStandardsMap.put(videoStandards.getQuality(), videoStandards);
                            videoStandards.setVideoQualityId(videoQuality1.getVideoQualityId());
                            videoStandardsResponse.add(videoStandards);
                        }
                    }
                }
            }
        }
        return videoStandardsResponse;
    }
}
