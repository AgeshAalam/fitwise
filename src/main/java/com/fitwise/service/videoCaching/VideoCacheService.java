package com.fitwise.service.videoCaching;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.videoCaching.VideoCacheConfig;
import com.fitwise.entity.videoCaching.VideoCacheConfigAndQualityMapping;
import com.fitwise.entity.videoCaching.VideoQuality;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.videoCaching.VideoCacheConfigModel;
import com.fitwise.repository.videoCaching.VideoCacheAndQualityMappingRepository;
import com.fitwise.repository.videoCaching.VideoCacheConfigRepository;
import com.fitwise.repository.videoCaching.VideoQualityRepository;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.videoCaching.VideoCacheConfigResponseView;
import com.fitwise.view.videoCaching.VideoQualityResponseView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class VideoCacheService {

    @Autowired
    private VideoQualityRepository videoQualityRepository;

    @Autowired
    private VideoCacheConfigRepository videoCacheConfigRepository;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private VideoCacheAndQualityMappingRepository videoCacheAndQualityMappingRepository;

    /**
     * Get all the available video qualities in the system
     * @return
     */
    public ResponseModel getAllVideoQualities() {
        List<VideoQuality> videoQualityList = videoQualityRepository.findAll();
        if(videoQualityList.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,MessageConstants.MSG_DATA_NOT_AVAILABLE,null);
        }
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(KeyConstants.KEY_VIDEO_QUALITY_LIST, videoQualityList);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, videoQualityList);
    }

    /**
     * Creating or updating the video caching config for logged in user
     * @param videoCacheConfigModel
     * @return
     */
    public ResponseModel createOrUpdateVideoConfig(VideoCacheConfigModel videoCacheConfigModel) {
        log.info("createOrUpdateVideoConfig starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();

        long profilingStartTimeMillis = new Date().getTime();
        VideoCacheConfig videoCacheConfig = videoCacheConfigRepository.findTop1ByUser(user);
        if(videoCacheConfig == null){
            videoCacheConfig = new VideoCacheConfig();
        }
        VideoQuality videoQuality = videoQualityRepository.findByVideoQualityId(videoCacheConfigModel.getVideoQualityId());
        if (videoQuality == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VIDEO_QUALITY_NOT_FOUND, MessageConstants.ERROR);
        }
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Query and Validation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        videoCacheConfig.setAutoDownload(videoCacheConfigModel.isAutoDownload());
        videoCacheConfig.setThroughWifi(videoCacheConfigModel.isThroughWifi());
        videoCacheConfig.setUser(user);
        videoCacheConfig.setVideoQuality(videoQuality);
        videoCacheConfigRepository.save(videoCacheConfig);
        profilingEndTimeMillis = new Date().getTime();
        log.info("DB update row : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("createOrUpdateVideoConfig ends.");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_VIDEO_CACHE_CONFIG_UPDATED, constructVideoCacheResponse(videoCacheConfig));
    }

    /**
     * Get the video cache config for the logged in user
     * @return
     */
    public ResponseModel getVideoCacheConfigData() {
        log.info("Flag exercise starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        VideoCacheConfig videoCacheConfig = videoCacheConfigRepository.findTop1ByUser(user);
        log.info("Query to get video cache config and user : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        if (videoCacheConfig == null) {
            videoCacheConfig = new VideoCacheConfig();
            videoCacheConfig.setThroughWifi(true);
            videoCacheConfig.setAutoDownload(true);
            VideoQuality videoQuality = videoQualityRepository.findByVideoQualityId(1L);
            log.info("Query to get video quality : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            videoCacheConfig.setVideoQuality(videoQuality);
            videoCacheConfigRepository.save(videoCacheConfig);
            log.info("Query to save video cache config : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }
        VideoCacheConfigResponseView videoCacheConfigResponseView = constructVideoCacheResponse(videoCacheConfig);
        log.info("Response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get video cache config date ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, videoCacheConfigResponseView);
    }

    public VideoCacheConfigResponseView constructVideoCacheResponse(VideoCacheConfig videoCacheConfig) {
        VideoCacheConfigResponseView videoCacheConfigResponseView = new VideoCacheConfigResponseView();
        videoCacheConfigResponseView.setAutoDownload(videoCacheConfig.isAutoDownload());
        videoCacheConfigResponseView.setThroughWifi(videoCacheConfig.isThroughWifi());
        List<VideoQualityResponseView> videoQualityResponseViews = new ArrayList<>();
        List<VideoQuality> videoQualities = videoQualityRepository.findAll();
        if (!videoQualities.isEmpty()) {
            for (VideoQuality videoQuality : videoQualities) {
                VideoQualityResponseView videoQualityResponseView = new VideoQualityResponseView();
                videoQualityResponseView.setVideoQualityId(videoQuality.getVideoQualityId());
                videoQualityResponseView.setVideoQuality(videoQuality);
                videoQualityResponseView.setTitle(videoQuality.getTitle());
                videoQualityResponseView.setDescription(videoQuality.getDescription());
                if (videoCacheConfig.getVideoQuality().getVideoQualityId().longValue() == videoQuality.getVideoQualityId().longValue()) {
                    videoQualityResponseView.setUserSelected(true);
                }
                videoQualityResponseViews.add(videoQualityResponseView);
            }
        }
        videoCacheConfigResponseView.setVideoQualityList(videoQualityResponseViews);
        return videoCacheConfigResponseView;
    }

}
