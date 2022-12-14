package com.fitwise.service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Audios;
import com.fitwise.entity.CircuitAndVoiceOverMapping;
import com.fitwise.entity.ExerciseSchedulers;
import com.fitwise.entity.User;
import com.fitwise.entity.VoiceOver;
import com.fitwise.entity.VoiceOverTags;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.SaveVoiceOverRequestModel;
import com.fitwise.model.VoiceOverFilterModel;
import com.fitwise.properties.AwsProperties;
import com.fitwise.repository.AudioRepository;
import com.fitwise.repository.CircuitAndVoiceOverMappingRepository;
import com.fitwise.repository.ExerciseScheduleRepository;
import com.fitwise.repository.VoiceOverRepository;
import com.fitwise.repository.VoiceOverTagsRepository;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.VoiceOverSpecifications;
import com.fitwise.utils.Convertions;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.AudioResponseView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.SaveVoiceOverResponseView;
import com.fitwise.view.VoiceOverCountListResponseView;
import com.fitwise.view.VoiceOverCountResponseView;
import com.fitwise.view.VoiceOverListResponseView;
import com.fitwise.view.VoiceOverResponseView;
import com.fitwise.view.VoiceOverTagsResponseView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VoiceOverService {

    @Autowired
    private S3FileHandlingService s3FileHandlingService;

    private final AwsProperties awsProperties;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private AudioRepository audioRepository;

    @Autowired
    private VoiceOverTagsRepository voiceOverTagsRepository;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private VoiceOverRepository voiceOverRepository;

    @Autowired
    private ExerciseScheduleRepository exerciseScheduleRepository;

    @Autowired
    private CircuitAndVoiceOverMappingRepository circuitAndVoiceOverMappingRepository;

    /**
     * Upload audio
     *
     * @param multipartFile
     * @return
     */
    public ResponseModel uploadAudio(MultipartFile multipartFile) {
        log.info("Upload Audio starts");
        long start = new Date().getTime();
        long profilingStartTimeInMillis;
        long profilingEndTimeInMillis;
        File file;
        String fileName = multipartFile.getOriginalFilename();
        User user = userComponents.getUser();
        try {
            profilingStartTimeInMillis = new Date().getTime();
            file = Convertions.convertToFile(multipartFile);
            profilingEndTimeInMillis = new Date().getTime();
            log.info("Audio file conversion : Time taken in millis : "+(profilingEndTimeInMillis-profilingStartTimeInMillis));
        } catch (ApplicationException exception) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FILE_CONVERSION_FAILURE, null);
        }
        if (!validationService.validateAudio(file.getPath())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WRONG_AUDIO_TYPE, null);
        }
        Audios audio = new Audios();
        try {
            profilingStartTimeInMillis = new Date().getTime();
            AudioFile audioFile = AudioFileIO.read(file);
            audio.setDuration(audioFile.getAudioHeader().getTrackLength());
            profilingEndTimeInMillis = new Date().getTime();
            log.info("Audio file read and getting duration from header : Time taken in millis : "+(profilingEndTimeInMillis-profilingStartTimeInMillis));
        } catch (Exception exception) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FILE_UPLOAD_FAILURE, null);
        }
        String filePath = "audio";
        profilingStartTimeInMillis = new Date().getTime();
        try {
            filePath = filePath + File.separator + file.getName();
            s3FileHandlingService.uploadFile(awsProperties.getAwsS3BucketResources(), filePath, file);
            profilingEndTimeInMillis = new Date().getTime();
            log.info("Audio file upload to s3 : Time taken in millis : "+(profilingEndTimeInMillis-profilingStartTimeInMillis));
            audio.setFilePath(awsProperties.getAwsResourceBaseUrl() + File.separator + filePath);
            log.info(String.valueOf(file.length()));
            audio.setFileName(fileName);
            audio.setUser(user);
            profilingStartTimeInMillis = new Date().getTime();
            audioRepository.save(audio);
            profilingEndTimeInMillis = new Date().getTime();
            log.info("Query: update audio repo : Time taken in millis : "+(profilingEndTimeInMillis-profilingStartTimeInMillis));
        } catch (Exception exception) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FILE_UPLOAD_FAILURE, null);
        }

        AudioResponseView audioResponseView = new AudioResponseView();
        audioResponseView.setFilePath(audio.getFilePath());
        audioResponseView.setAudioId(audio.getAudioId());
        audioResponseView.setDuration(audio.getDuration());
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Audio upload api : Total Time taken in millis : "+(profilingEndTimeInMillis-start));
        log.info("Upload Audio ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_AUDIO_SAVED, audioResponseView);
    }

    /**
     * Get all tags for voice-over
     *
     * @return
     */
    public ResponseModel getVoiceOverTags() {
        List<VoiceOverTags> voiceOverTagsList = voiceOverTagsRepository.findAll();
        List<VoiceOverTagsResponseView> voiceOverTagsResponseViews = new ArrayList<>();
        for (VoiceOverTags voiceOverTag : voiceOverTagsList) {
            VoiceOverTagsResponseView voiceOverTagsResponseView = new VoiceOverTagsResponseView();
            voiceOverTagsResponseView.setTagId(voiceOverTag.getTagId());
            voiceOverTagsResponseView.setTag(voiceOverTag.getTag());
            voiceOverTagsResponseViews.add(voiceOverTagsResponseView);
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_VOICE_OVER_TAGS_RETRIEVED, voiceOverTagsResponseViews);
    }

    /**
     * Save voice over
     *
     * @param model
     * @return
     */
    public ResponseModel saveVoiceOver(SaveVoiceOverRequestModel model) {
        User user = userComponents.getUser();
        boolean isNewVoiceOver = false;
        if(model.getVoiceOverId() == null){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_ID_INVALID, null);
        }
        if (model.getAudioId() == null || model.getAudioId() == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_AUDIO_ID_INVALID, null);
        }

        if (model.getTitle().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_TITLE_CANT_BE_EMPTY, null);
        }
        Audios audio = audioRepository.findByAudioId(model.getAudioId());
        if (audio == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_AUDIO_NOT_FOUND, null);
        }
        VoiceOver voiceOver ;
        if(model.getVoiceOverId() == 0){
            voiceOver = new VoiceOver();
            isNewVoiceOver = true;
        }else{
            voiceOver = voiceOverRepository.findByVoiceOverIdAndUserUserId(model.getVoiceOverId(),user.getUserId());
            if(voiceOver == null){
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_ID_INVALID, null);
            }
        }
        if(isNewVoiceOver || !voiceOver.getTitle().equalsIgnoreCase(model.getTitle())){
            validateVoiceOverTitle(model.getTitle());
        }
        voiceOver.setAudios(audio);
        voiceOver.setTitle(model.getTitle());
        voiceOver.setUser(user);
        List<VoiceOverTagsResponseView> voiceOverTagsResponseViews = new ArrayList<>();
        List<VoiceOverTags> voiceOverTagsList = new ArrayList<>();
        for (Long tagId : model.getTagIds()) {
            VoiceOverTags voiceOverTags = voiceOverTagsRepository.findByTagId(tagId);
            if (voiceOverTags == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_TAG_NOT_FOUND, null);
            }
            voiceOverTagsList.add(voiceOverTags);
            VoiceOverTagsResponseView voiceOverTagsResponseView = new VoiceOverTagsResponseView();
            voiceOverTagsResponseView.setTagId(voiceOverTags.getTagId());
            voiceOverTagsResponseView.setTag(voiceOverTags.getTag());
            voiceOverTagsResponseViews.add(voiceOverTagsResponseView);
        }
        voiceOver.setVoiceOverTags(voiceOverTagsList);
        voiceOver.setCreatedDate(new Date());
        voiceOverRepository.save(voiceOver);
        SaveVoiceOverResponseView responseView = new SaveVoiceOverResponseView();
        responseView.setVoiceOverId(voiceOver.getVoiceOverId());
        responseView.setTitle(voiceOver.getTitle());
        responseView.setAudioId(voiceOver.getAudios().getAudioId());
        responseView.setFilePath(voiceOver.getAudios().getFilePath());
        responseView.setDuration(voiceOver.getAudios().getDuration());
        responseView.setVoiceOverTags(voiceOverTagsResponseViews);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_VOICE_OVER_SAVED, responseView);
    }

    /**
     * Delete Voice Over from library
     *
     * @param voiceOverId
     * @return
     */
    public ResponseModel deleteVoiceOver(Long voiceOverId) {
        log.info("Delete voice over starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        if (voiceOverId == null || voiceOverId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_ID_INVALID, null);
        }
        VoiceOver voiceOver = voiceOverRepository.findByVoiceOverIdAndUserUserId(voiceOverId, user.getUserId());
        if (voiceOver == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_ID_INVALID, null);
        }
        List<ExerciseSchedulers> exerciseSchedulers = exerciseScheduleRepository.findByVoiceOverVoiceOverId(voiceOverId);
        if(!exerciseSchedulers.isEmpty()){
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_VOICE_OVER_DELETION_FAILED, MessageConstants.ERROR);
        }
        log.info("Basic validations with getting user : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        List<CircuitAndVoiceOverMapping> circuitAndVoiceOverMappings = circuitAndVoiceOverMappingRepository.findByVoiceOverVoiceOverId(voiceOverId);
        if(!circuitAndVoiceOverMappings.isEmpty()){
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_VOICE_OVER_DELETION_FAILED, MessageConstants.ERROR);
        }
        log.info("Query to get circuit and voice over mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        Audios audio = audioRepository.findByAudioId(voiceOver.getAudios().getAudioId());
        log.info("Query to get audio : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();


        voiceOverRepository.delete(voiceOver);
        audioRepository.delete(audio);
        log.info("Query to delete both voice over and audio : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Delete voice over ends.");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_VOICE_OVER_DELETED, null);
    }

    /**
     * Get All voice-overs for a user
     *
     * @param pageNo
     * @param pageSize
     * @param tagId
     * @param searchName
     * @param filterModel
     * @return
     */
    public ResponseModel getAllVoiceOvers(int pageNo, int pageSize, Long tagId, Optional<String> searchName, VoiceOverFilterModel filterModel) {
        log.info("Get all voice overs starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        log.info("Get user : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        Page<VoiceOver> voiceOverList;
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, Sort.Direction.DESC, SearchConstants.CREATED_DATE);
        Specification<VoiceOver> userSpecification = VoiceOverSpecifications.getVoiceOverByUser(user.getUserId());
        List<Long> tagIds = new ArrayList<>();
        if (!filterModel.getVoiceOverFilters().isEmpty() && filterModel.getVoiceOverFilters() != null) {
            for (VoiceOverTagsResponseView voiceOverTag : filterModel.getVoiceOverFilters()) {
                VoiceOverTags tag = voiceOverTagsRepository.findByTagId(voiceOverTag.getTagId());
                if (tag == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_TAG_NOT_FOUND, null);
                }
                tagIds.add(voiceOverTag.getTagId());
            }
            userSpecification = userSpecification.and(VoiceOverSpecifications.getVoiceOverByTags(tagIds));
        } else if (tagId != 0) {
            userSpecification = userSpecification.and(VoiceOverSpecifications.getVoiceOverByTagId(tagId));
        }
        if (searchName.isPresent() && !searchName.get().isEmpty()) {
            String search = searchName.get();
            userSpecification = userSpecification.and(VoiceOverSpecifications.getVoiceOverByTitleSearch(search));
        }
        voiceOverList = voiceOverRepository.findAll(userSpecification, pageRequest);
        log.info("Query to get voice over list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        VoiceOverListResponseView voiceOverListResponseView = new VoiceOverListResponseView();
        List<VoiceOverResponseView> voiceOverResponseViews = new ArrayList<>();
        for (VoiceOver voiceOver : voiceOverList) {
            VoiceOverResponseView voiceOverResponseView = new VoiceOverResponseView();
            voiceOverResponseView.setVoiceOverId(voiceOver.getVoiceOverId());
            voiceOverResponseView.setTitle(voiceOver.getTitle());
            if(voiceOver.getAudios() != null){
                voiceOverResponseView.setAudioPath(voiceOver.getAudios().getFilePath());
                voiceOverResponseView.setDuration(voiceOver.getAudios().getDuration());
            }
            voiceOverResponseViews.add(voiceOverResponseView);
        }
        voiceOverListResponseView.setVoiceOverList(voiceOverResponseViews);
        voiceOverListResponseView.setTotalVoiceOvers(voiceOverList.getTotalElements());
        log.info("Construct voice over list response view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get all voice overs ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, voiceOverListResponseView);
    }

    /**
     * Get Voice-overs count by tags
     *
     * @return
     */
    public ResponseModel getVoiceOverCountByTags() {
        log.info("getVoiceOverCountByTags starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        long profilingStartTimeMillis = System.currentTimeMillis();
        User user = userComponents.getUser();
        List<VoiceOverTags> voiceOverTagsList = voiceOverTagsRepository.findAll();
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        List<VoiceOverCountResponseView> voiceOverCountResponseViews = new ArrayList<>();
        for (VoiceOverTags voiceOverTag : voiceOverTagsList) {
            int count = voiceOverRepository.countByVoiceOverTagsTagIdAndUserUserId(voiceOverTag.getTagId(), user.getUserId());
            VoiceOverCountResponseView voiceOverCountResponseView = new VoiceOverCountResponseView();
            voiceOverCountResponseView.setTagId(voiceOverTag.getTagId());
            voiceOverCountResponseView.setTag(voiceOverTag.getTag());
            voiceOverCountResponseView.setCount(count);
            voiceOverCountResponseViews.add(voiceOverCountResponseView);
        }

        int totalCount = voiceOverRepository.countByUserUserId(user.getUserId());
        VoiceOverCountResponseView voiceOverCountResponseView = new VoiceOverCountResponseView();
        voiceOverCountResponseView.setTag("All");
        voiceOverCountResponseView.setCount(totalCount);
        voiceOverCountResponseViews.add(voiceOverCountResponseView);

        VoiceOverCountListResponseView voiceOverCountListResponseView = new VoiceOverCountListResponseView();
        voiceOverCountListResponseView.setVoiceOverCount(voiceOverCountResponseViews);
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Response construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Query and data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getVoiceOverCountByTags ends.");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, voiceOverCountListResponseView);
    }

    /**
     * Get Voice Over Details
     *
     * @param voiceOverId
     * @return
     */
    public ResponseModel getVoiceOverDetails(Long voiceOverId) {
        VoiceOver voiceOver = voiceOverRepository.findByVoiceOverId(voiceOverId);
        if (voiceOver == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_ID_INVALID, null);
        }
        List<VoiceOverTagsResponseView> voiceOverTags = new ArrayList<>();

        for (VoiceOverTags voiceOverTag : voiceOver.getVoiceOverTags()) {
            VoiceOverTagsResponseView voiceOverTagsResponseView = new VoiceOverTagsResponseView();
            voiceOverTagsResponseView.setTagId(voiceOverTag.getTagId());
            voiceOverTagsResponseView.setTag(voiceOverTag.getTag());
            voiceOverTags.add(voiceOverTagsResponseView);
        }

        SaveVoiceOverResponseView voiceOverResponseView = new SaveVoiceOverResponseView();
        voiceOverResponseView.setVoiceOverId(voiceOver.getVoiceOverId());
        voiceOverResponseView.setTitle(voiceOver.getTitle());
        if(voiceOver.getAudios() != null){
            voiceOverResponseView.setAudioId(voiceOver.getAudios().getAudioId());
            voiceOverResponseView.setFilePath(voiceOver.getAudios().getFilePath());
            voiceOverResponseView.setDuration(voiceOver.getAudios().getDuration());
        }
        voiceOverResponseView.setVoiceOverTags(voiceOverTags);

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, voiceOverResponseView);
    }

    /**
     * validate voice-over title
     *
     * @param title
     */
    public void validateVoiceOverTitle(String title) {
        ValidationUtils.validateName(title, ValidationConstants.VOICE_OVER_TITLE_MAX_LENGTH);
        User user = userComponents.getUser();
        List<VoiceOver> voiceOverList = voiceOverRepository.findByUserUserIdAndTitle(user.getUserId(), title);
        if (!voiceOverList.isEmpty()) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_VOICE_OVER_DUPLICATE_TITLE, null);
        }
    }

    public ResponseModel deleteAudioFromVoiceOver(Long voiceOverId){
        User user = userComponents.getUser();
        if (voiceOverId == null || voiceOverId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_ID_INVALID, null);
        }
        VoiceOver voiceOver = voiceOverRepository.findByVoiceOverIdAndUserUserId(voiceOverId, user.getUserId());
        if (voiceOver == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_ID_INVALID, null);
        }
        Audios audio = audioRepository.findByAudioId(voiceOver.getAudios().getAudioId());
        voiceOver.setAudios(null);
        voiceOverRepository.save(voiceOver);
        audioRepository.delete(audio);

        return new ResponseModel(Constants.SUCCESS_STATUS,MessageConstants.MSG_AUDIO_DELETED_FROM_VOICE_OVER,null);
    }

}
