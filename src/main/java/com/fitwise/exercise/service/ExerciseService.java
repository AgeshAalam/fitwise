package com.fitwise.exercise.service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.ValidationConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.VideoUploadStatus;
import com.fitwise.constants.VimeoConstants;
import com.fitwise.entity.CircuitSchedule;
import com.fitwise.entity.Equipments;
import com.fitwise.entity.ExerciseSchedulers;
import com.fitwise.entity.Exercises;
import com.fitwise.entity.Images;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.VideoManagement;
import com.fitwise.entity.admin.ExerciseCategoryMapping;
import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.daoImpl.ExerciseRepoImpl;
import com.fitwise.exercise.model.ExerciseModel;
import com.fitwise.exercise.model.SupportingVideoModel;
import com.fitwise.exercise.model.UploadModel;
import com.fitwise.exercise.model.VimeoModel;
import com.fitwise.model.ExerciseCategoryModel;
import com.fitwise.model.ExerciseResponse;
import com.fitwise.model.instructor.VideoVersioningModel;
import com.fitwise.repository.EquipmentsRepository;
import com.fitwise.repository.ExerciseRepository;
import com.fitwise.repository.ExerciseScheduleRepository;
import com.fitwise.repository.FlaggedExercisesSummaryRepository;
import com.fitwise.repository.ImageRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.VideoManagementRepo;
import com.fitwise.repository.admin.ExerciseCategoryMappingRepository;
import com.fitwise.repository.circuit.CircuitRepository;
import com.fitwise.repository.circuit.CircuitScheduleRepository;
import com.fitwise.response.VimeoVideoView;
import com.fitwise.service.instructor.InstructorProgramService;
import com.fitwise.utils.AppUtils;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The Class ExerciseService.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExerciseService {

    /**
     * The vimeo service.
     */
    @Autowired
    private VimeoService vimeoService;

    /**
     * The exercise impl.
     */
    @Autowired
    private ExerciseRepoImpl exerciseImpl;

    /**
     * The eq repo.
     */
    @Autowired
    private EquipmentsRepository eqRepo;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private EquipmentsRepository equipmentsRepository;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private ExerciseScheduleRepository exerciseScheduleRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    private VideoManagementRepo videoManagementRepo;

    @Autowired
    private InstructorProgramService instructorProgramService;
    @Autowired
    FlaggedExercisesSummaryRepository flaggedExercisesSummaryRepository;
    @Autowired
    private EmailContentUtil emailContentUtil;

    @Autowired
    private CircuitScheduleRepository circuitScheduleRepository;

    @Autowired
    private CircuitRepository circuitRepository;

    @Autowired
    private ExerciseCategoryMappingRepository exerciseCategoryMappingRepository;

    private final AsyncMailer asyncMailer;

    /**
     * Creates the exercise.
     *
     * @param request the request
     * @return the response model
     * @throws ApplicationException the application exception
     * @throws IOException          Signals that an I/O exception has occurred.
     */
    @Transactional
    public ResponseModel createExercise(final ExerciseModel request) throws IOException {
        log.info("Create Exercise starts");
        User user = userComponents.getUser();
        Date createExerciseStart = new Date();
        ResponseModel response = new ResponseModel();
        VimeoModel vimeoModel = new VimeoModel();
        UserProfile userProfile = userProfileRepository.findByUser(user);
        if (!request.getFileName().isEmpty() && userProfile.getFirstName() != null && userProfile.getLastName() != null) {
            request.setFileName(userProfile.getFirstName() + " " + userProfile.getLastName() + " - " + request.getFileName());
        }
        doConstructVimeoModel(request, vimeoModel);
        long profilingStart = new Date().getTime();
        vimeoModel = vimeoService.createVideoPlaceholder(vimeoModel);
        long profilingEnd = new Date().getTime();
        log.info("Creating placeholder for exercise video : time taken in millis : "+(profilingEnd-profilingStart));
        doValidateExercise(request);
        //Supporting Video vimeo model
        boolean isSupportingVideoPresent = false;
        VimeoModel supportingVimeoModel = null;
        SupportingVideoModel supportingVideoModel = request.getSupportingVideoModel();
        if (supportingVideoModel != null) {
            isSupportingVideoPresent = true;
            supportingVimeoModel = new VimeoModel();
            if (!request.getFileName().isEmpty() && userProfile.getFirstName() != null && userProfile.getLastName() != null) {
                supportingVideoModel.setFileName(userProfile.getFirstName() + " " + userProfile.getLastName() + " - " + supportingVideoModel.getFileName());
            }
            doConstructSupportingVideoVimeoModel(supportingVideoModel, supportingVimeoModel);
            Date supportStart = new Date();
            log.info("createVideoPlaceholder for supporting video started at : " + supportStart + " by " + user.getEmail());
            supportingVimeoModel = vimeoService.createVideoPlaceholder(supportingVimeoModel);
            Date supportEnd = new Date();
            log.info("createVideoPlaceholder for supporting video ended at : " + supportEnd + " by " + user.getEmail());
            log.info("createVideoPlaceholder for supporting video completion took : " + (supportEnd.getTime() - supportStart.getTime()) + " ms for " + user.getEmail());
        }
        // Need to store exercise in db
        Exercises exercise = new Exercises();
        doConstructExerciseEntity(exercise, request, vimeoModel, supportingVimeoModel);
        // Saving exercise
        exercise = exerciseImpl.saveExercise(exercise);
        request.setExerciseId(exercise.getExerciseId());
        request.setVimeoData(vimeoModel);
        if (isSupportingVideoPresent) {
            request.getSupportingVideoModel().setVimeoData(supportingVimeoModel);
        }
        Date createExerciseEnd = new Date();
        log.info("createExercise for exercise ended at : " + createExerciseEnd + " by " + user.getEmail());
        log.info("createExercise completion took : " + (createExerciseEnd.getTime() - createExerciseStart.getTime()) + " ms for " + user.getEmail());
        response.setMessage("Successfully stored exercise and created placeholder for video");
        response.setPayload(request);
        response.setStatus(Constants.SUCCESS_STATUS);
        return response;
    }

    /**
     * Do construct exercise entity.
     *
     * @param exercise   the exercise
     * @param request    the request
     * @param vimeoModel the vimeo model
     * @throws ApplicationException the application exception
     */
    public void doConstructExerciseEntity(Exercises exercise, ExerciseModel request, VimeoModel vimeoModel, VimeoModel supportingVideoVimeoModel) {
        User user = userComponents.getUser();
        String exerciseTitle = request.getTitle();
        exercise.setTitle(exerciseTitle);
        exercise.setDescription(request.getDescription());
        //Duplicate Exercise title validation
        List<Exercises> exercisesWithSameTitle = exerciseImpl.findByOwnerUserIdAndTitle(user.getUserId(), exerciseTitle);
        if (!exercisesWithSameTitle.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_EXERCISE_DUPLICATE_TITLE, MessageConstants.ERROR);
        }
        doValidateRoleAsInstructor(user);
        exercise.setOwner(user);
        VideoManagement videoManagement = new VideoManagement();
        videoManagement.setOwner(user);
        Images image = imageRepository.findByImageId(request.getImageId());
        if (image != null)
            videoManagement.setThumbnail(image);
        videoManagement.setUploadStatus(VideoUploadStatus.UPLOAD);
        request.setVideoUploadStatus(VideoUploadStatus.UPLOAD);
        doConstructVideoManagement(exercise, vimeoModel, videoManagement);
        exercise.setVideoManagement(videoManagement);
        if (supportingVideoVimeoModel != null) {
            VideoManagement supportingVideoManagement = new VideoManagement();
            supportingVideoManagement.setOwner(user);
            Images supportVideoImage = imageRepository.findByImageId(request.getSupportingVideoModel().getImageId());
            if (supportVideoImage != null)
                supportingVideoManagement.setThumbnail(supportVideoImage);
            supportingVideoManagement.setUploadStatus(VideoUploadStatus.UPLOAD);
            request.getSupportingVideoModel().setVideoUploadStatus(VideoUploadStatus.UPLOAD);
            doConstructSupportVideoManagement(request.getSupportingVideoModel(), supportingVideoVimeoModel, supportingVideoManagement);
            exercise.setSupportVideoManagement(supportingVideoManagement);
        }
        /*
         * Iterating and setting exercise to list from request object
         */
        List<Equipments> equipmentsList = new ArrayList<>();
        for (Equipments equipment : request.getEquipments()) {
            Equipments equipmentFromDb = equipmentsRepository.findByEquipmentId(equipment.getEquipmentId());
            equipmentsList.add(equipmentFromDb);
        }
        if (!equipmentsList.isEmpty())
            exercise.setEquipments(equipmentsList);
    }

    /**
     * Do construct video management.
     *
     * @param exercise        the exercise
     * @param vimeoModel      the vimeo model
     * @param videoManagement the video management
     */
    public void doConstructVideoManagement(Exercises exercise, VimeoModel vimeoModel, VideoManagement videoManagement) {
        videoManagement.setTitle(exercise.getTitle());
        videoManagement.setDescription(exercise.getDescription());
        videoManagement.setUrl(vimeoModel.getUri());
    }

    /**
     * Do construct support video management.
     *
     * @param supportingVideo
     * @param supportVimeoModel the support vimeo model
     * @param videoManagement   the support video management
     */
    public void doConstructSupportVideoManagement(SupportingVideoModel supportingVideo, VimeoModel supportVimeoModel, VideoManagement videoManagement) {
        videoManagement.setTitle(supportingVideo.getTitle());
        videoManagement.setDescription(supportingVideo.getDescription());
        videoManagement.setUrl(supportVimeoModel.getUri());
    }

    /**
     * Do validate role as instructor.
     *
     * @param user the user
     * @throws ApplicationException the application exception
     */
    public void doValidateRoleAsInstructor(User user) {
        Set<UserRole> roles = AppUtils.getUserRoles(user);  //AKHIL
        boolean isInstructor = false;
        for (UserRole role : roles) {
            if (role.getName().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)) {
                isInstructor = true;
                break;
            }
        }
        if (!isInstructor) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_ROLE_INCORRECT, MessageConstants.ERROR);
        }
    }

    /**
     * Do construct vimeo model.
     *
     * @param exercise   the exercise
     * @param vimeoModel the vimeo model
     */
    public void doConstructVimeoModel(ExerciseModel exercise, VimeoModel vimeoModel) {
        vimeoModel.setName(exercise.getFileName());
        UploadModel upload = new UploadModel();
        upload.setSize(exercise.getFileSize());
        upload.setApproach(VimeoConstants.APPROACH);
        vimeoModel.setUpload(upload);
    }

    /**
     * Do construct supporting video vimeo model.
     *
     * @param supportingVideoModel the exercise
     * @param supportingVimeoModel the vimeo model
     */
    public void doConstructSupportingVideoVimeoModel(SupportingVideoModel supportingVideoModel, VimeoModel supportingVimeoModel) {
        if (!ValidationUtils.isEmptyString(supportingVideoModel.getFileName()) && !(supportingVideoModel.getFileSize() == null || supportingVideoModel.getFileSize() == 0)) {
            supportingVimeoModel.setName(supportingVideoModel.getFileName());
            UploadModel upload = new UploadModel();
            upload.setSize(supportingVideoModel.getFileSize());
            upload.setApproach(VimeoConstants.APPROACH);
            supportingVimeoModel.setUpload(upload);
        }
    }

    /**
     * Do validate exercise.
     *
     * @param exercise the exercise
     * @throws ApplicationException the application exception
     */
    public void doValidateExercise(ExerciseModel exercise){
        User user = userComponents.getUser();
        ValidationUtils.throwException(exercise == null, "Exercise cant be null", Constants.BAD_REQUEST);
        ValidationUtils.throwException(ValidationUtils.isEmptyString(exercise.getTitle()), ValidationMessageConstants.MSG_TITLE_NULL, Constants.BAD_REQUEST);
        if(exercise.getVideoId() == null || exercise.getVideoId() == 0) {
            ValidationUtils.throwException(ValidationUtils.isEmptyString(exercise.getFileName()), "File name cant be null or empty", Constants.BAD_REQUEST);
            ValidationUtils.throwException(exercise.getFileSize() == null || exercise.getFileSize() == 0, "File size cant be null or 0", Constants.BAD_REQUEST);
        }
        ValidationUtils.throwException(user.getUserId() == null || user.getUserId() == 0, "Instructor Id cant be null or 0", Constants.BAD_REQUEST);
    }

    /**
     * Construct exercise response view.
     *
     * @param exercises the exercises
     * @return the exercise model
     */
    public ExerciseResponse constructExerciseResponseView(Exercises exercises) {
        ExerciseResponse exerciseResponse = new ExerciseResponse();
        exerciseResponse.setExerciseId(exercises.getExerciseId());
        exerciseResponse.setTitle(exercises.getTitle());
        exerciseResponse.setDescription(exercises.getDescription());
        VideoManagement videoManagement = exercises.getVideoManagement();
        if (videoManagement != null) {
            VimeoModel video = new VimeoModel();
            video.setUri(videoManagement.getUrl());
            video.setName(videoManagement.getTitle());
            video.setVideoId(videoManagement.getVideoManagementId());  //Adding video management Id
            exerciseResponse.setFileName(videoManagement.getUrl());
            exerciseResponse.setVimeoData(video);
            exerciseResponse.setDuration(videoManagement.getDuration());
            exerciseResponse.setVideoUploadStatus(videoManagement.getUploadStatus());
            exerciseResponse.setVideoId(videoManagement.getVideoManagementId());
            /**
             * If the video processing was failed first time, marking it as upload status
             * If the video processing was failed more than one time, marking it as re-upload status
             */
            if (videoManagement.getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_PROCESSING_FAILED)) {
                exerciseResponse.setVideoUploadStatus(VideoUploadStatus.UPLOAD);
            } else if (videoManagement.getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED)) {
                exerciseResponse.setVideoUploadStatus(VideoUploadStatus.REUPLOAD);
            }

            if (videoManagement.getThumbnail() != null) {
                exerciseResponse.setImageId(videoManagement.getThumbnail().getImageId());
                exerciseResponse.setThumbnailUrl(videoManagement.getThumbnail().getImagePath());
            }
        }
        //Support Video management construction
        VideoManagement supportVideoManagement = exercises.getSupportVideoManagement();
        if (supportVideoManagement != null) {
            SupportingVideoModel supportingVideoModel = new SupportingVideoModel();
            supportingVideoModel.setTitle(supportVideoManagement.getTitle());
            supportingVideoModel.setDescription(supportVideoManagement.getDescription());
            supportingVideoModel.setFileName(supportVideoManagement.getUrl());
            supportingVideoModel.setVideoId(supportVideoManagement.getVideoManagementId());
            VimeoModel supportVideo = new VimeoModel();
            supportVideo.setUri(supportVideoManagement.getUrl());
            supportVideo.setName(supportVideoManagement.getTitle());
            supportingVideoModel.setVimeoData(supportVideo);
            supportingVideoModel.setDuration(supportVideoManagement.getDuration());
            supportingVideoModel.setVideoUploadStatus(supportVideoManagement.getUploadStatus());
            /**
             * If the video processing was failed first time, marking it as upload status
             * If the video processing was failed more than one time, marking it as re-upload status
             */
            if (supportVideoManagement.getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_PROCESSING_FAILED)) {
                supportingVideoModel.setVideoUploadStatus(VideoUploadStatus.UPLOAD);
            } else if (supportVideoManagement.getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED)) {
                supportingVideoModel.setVideoUploadStatus(VideoUploadStatus.REUPLOAD);
            }
            if (supportVideoManagement.getThumbnail() != null) {
                supportingVideoModel.setImageId(supportVideoManagement.getThumbnail().getImageId());
                supportingVideoModel.setThumbnailUrl(supportVideoManagement.getThumbnail().getImagePath());
            }
            exerciseResponse.setSupportingVideoModel(supportingVideoModel);
        }
        List<Equipments> equipments = new ArrayList<>();
        for (Equipments equipment : exercises.getEquipments()) {
            equipments.add(equipment);
        }
        exerciseResponse.setEquipments(equipments);

        boolean isExerciseBlocked = flaggedExercisesSummaryRepository.existsByExerciseExerciseIdAndFlagStatus(exercises.getExerciseId(), KeyConstants.KEY_BLOCK);
        exerciseResponse.setIsExerciseBlocked(isExerciseBlocked);

        if(exercises.isByAdmin()){
            List<ExerciseCategoryMapping> exerciseCategoryMappings = exerciseCategoryMappingRepository.findByExercise(exercises);
            List<ExerciseCategoryModel> exerciseCategories = new ArrayList<>();
            for (ExerciseCategoryMapping exerciseCategoryMapping : exerciseCategoryMappings) {
                ExerciseCategoryModel exerciseCategoryModel = new ExerciseCategoryModel();
                exerciseCategoryModel.setCategoryId(exerciseCategoryMapping.getExerciseCategory().getCategoryId());
                exerciseCategoryModel.setCategoryName(exerciseCategoryMapping.getExerciseCategory().getCategoryName());
                exerciseCategories.add(exerciseCategoryModel);
            }
            exerciseResponse.setExerciseCategories(exerciseCategories);
        }
        return exerciseResponse;
    }

    /**
     * Gets the exercise.
     *
     * @param exerciseId the exercise id
     * @return the exercise
     * @throws ApplicationException the application exception
     */
    public ExerciseResponse getExercise(final Long exerciseId){
        User user = userComponents.getUser();
        Exercises exercise = exerciseImpl.findByExerciseIdAndOwnerUserId(exerciseId, user.getUserId());
        if (exercise == null) {
            throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_EXERCISE_NOT_FOUND, null);
        }
        return constructExerciseResponseView(exercise);
    }


    /**
     * Gets the exercises.
     *
     * @param searchName
     * @return
     * @throws ApplicationException
     */
    public List<ExerciseResponse> getExercises(Optional<String> searchName) {
        User user = userComponents.getUser();
        List<Exercises> exercises;
        if (searchName.isPresent() && !searchName.get().isEmpty()) {
            exercises = exerciseRepository.findByOwnerUserIdAndTitleIgnoreCaseContainingOrOwnerUserIdAndDescriptionIgnoreCaseContaining(user.getUserId(), searchName.get(), user.getUserId(), searchName.get());
        } else {
            exercises = exerciseImpl.findByOwnerUserId(user.getUserId());
        }
        if (exercises.isEmpty()) {
            throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        List<ExerciseResponse> exerciseModels = new ArrayList<>();
        for (Exercises exercise : exercises) {
            exerciseModels.add(constructExerciseResponseView(exercise));
        }
        return exerciseModels;
    }

    /**
     * Gets the equipments.
     *
     * @return the equipments
     */
    public List<Equipments> getEquipments() {
        List<Equipments> equipments = equipmentsRepository.findAll();
        if (equipments.isEmpty()) {
            throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        return equipments;
    }

    /**
     * Validate exercise name duplicate for instructor library
     *
     * @param exerciseName
     */
    public void validateName(String exerciseName) {
        ValidationUtils.validateName(exerciseName, ValidationConstants.NAME_LENGTH_CHAR_50);
        User user = userComponents.getUser();
        List<Exercises> exercises = exerciseImpl.findByOwnerUserIdAndTitle(user.getUserId(), exerciseName);
        if (!exercises.isEmpty()) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_EXERCISE_DUPLICATE_TITLE, null);
        }
    }

    /**
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param searchName
     * @return
     * @throws ApplicationException
     */
    public ResponseModel getAllInstructorExercises(int pageNo, int pageSize, String sortOrder, String sortBy, Optional<String> searchName) {
        log.info("Get All Instructor exercises starts");
        long start = new Date().getTime();

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        if (!(sortBy.equalsIgnoreCase(SearchConstants.TITLE) || sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
        }

        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }

        User user = userComponents.getUser();

        Sort sort = getExerciseLibrarySortCriteria(sortBy);
        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            sort = sort.descending();
        }
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
        log.info("Basic validations and get user : Time taken in millis : "+(new Date().getTime() - start));

        long temp = new Date().getTime();
        Page<Exercises> exercisesPage;
        if (searchName.isPresent() && !searchName.get().isEmpty()) {
            exercisesPage = exerciseRepository.findByOwnerUserIdAndTitleIgnoreCaseContaining(user.getUserId(), searchName.get(), pageRequest);
        } else {
            exercisesPage = exerciseRepository.findByOwnerUserId(user.getUserId(), pageRequest);
        }
        log.info("Query : Time taken in millis : "+(new Date().getTime() - temp));

        Map<String, Object> map = new HashMap<>();
        if (exercisesPage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        temp = new Date().getTime();
        List<ExerciseResponse> exerciseResponseModels = new ArrayList<>();
        for (Exercises exercise : exercisesPage) {
            exerciseResponseModels.add(constructExerciseResponseView(exercise));
        }
        log.info("Response construction : Time taken in millis : "+(new Date().getTime() - temp));


        map.put(KeyConstants.KEY_TOTAL_COUNT, exercisesPage.getTotalElements());
        map.put(KeyConstants.KEY_EXERCISES, exerciseResponseModels);
        log.info("Get all instructor exercises : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get all instructor exercises ends");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, map);
    }

    public Sort getExerciseLibrarySortCriteria(String sortBy) {
        Sort sort;
        if (sortBy.equalsIgnoreCase(SearchConstants.TITLE)) {
            sort = Sort.by(SearchConstants.TITLE);
        } else {
            sort = Sort.by("exerciseId");
        }
        return sort;
    }

    /**
     * Delete a Exercise and make it anonymous.
     *
     * @param exerciseId
     */
    public void deleteExercise(Long exerciseId) {
        long startTime = new Date().getTime();
        User user = userComponents.getUser();
        log.info("Get User : " + (new Date().getTime() - startTime));
        deleteExercise(exerciseId, user, true, false);
    }

    public void deleteExercise(Long exerciseId, User user, boolean sendMailNotification, boolean isByAdmin) {
        long startTime = new Date().getTime();
        log.info("Delete exercise started.");
        long temp = new Date().getTime();
        if (exerciseId == null || exerciseId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_ID_NULL, MessageConstants.ERROR);
        }
        log.info("Field validation : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        Exercises exercise ;
        User owner;
        if(isByAdmin){
            exercise = exerciseRepository.findByExerciseIdAndIsByAdmin(exerciseId,isByAdmin);
            owner = exercise.getOwner();
        }else{
            exercise = exerciseImpl.findByExerciseIdAndOwnerUserId(exerciseId, user.getUserId());
            owner = user;
        }
        if (exercise == null) {
            throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_EXERCISE_NOT_FOUND, null);
        }
        log.info("Get exercise : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        List<ExerciseSchedulers> exerciseSchedulerList = exerciseScheduleRepository.findByExerciseExerciseId(exerciseId);
        List<Long> circuitIds = new ArrayList<>();
        for(ExerciseSchedulers schedulers : exerciseSchedulerList){
            circuitIds.add(schedulers.getCircuit().getCircuitId());
        }
        log.info("Collect circuit ids : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        List<CircuitSchedule> circuitSchedules = circuitScheduleRepository.findByCircuitCircuitIdIn(circuitIds);
        log.info("Get circuit schedules : " + (new Date().getTime() - temp));
        if (!circuitSchedules.isEmpty()) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_EXERCISE_DELETION_FAILED, MessageConstants.ERROR);
        }else{
            temp = new Date().getTime();
            for(CircuitSchedule circuitSchedule : circuitSchedules){
                fitwiseUtils.makeCircuitAnonymous(circuitSchedule.getCircuit());
                circuitRepository.deleteById(circuitSchedule.getCircuit().getCircuitId());
                circuitScheduleRepository.deleteByCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
            }
            log.info("Updapte and delete cicuit data : " + (new Date().getTime() - temp));
        }
        //deleting exercise and supporting videos on vimeo
        // Stop deleteing videos form vimeo service. One video can add in multiple exercise. If it deleting will affect in rest of the exe videos.
        /*VideoManagement exerciseVideo = exercise.getVideoManagement();
        VideoManagement supportingVideo = exercise.getSupportVideoManagement();
        try {
            if (supportingVideo != null) {
                temp = new Date().getTime();
                vimeoService.deleteVimeoVideo(supportingVideo.getUrl());
                log.info("Delete exercise support video from vimeo : " + (new Date().getTime() - temp));
            }
            if (exerciseVideo != null) {
                temp = new Date().getTime();
                vimeoService.deleteVimeoVideo(exerciseVideo.getUrl());
                log.info("Delete exercise video : " + (new Date().getTime() - temp));
            }
        } catch (Exception e) {
            log.info(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
        }*/
        temp = new Date().getTime();
        String title = exercise.getTitle();

        //making Exercise as anonymous
        if(exercise.isByAdmin()){
            List<ExerciseCategoryMapping> exerciseCategoryMappings = exerciseCategoryMappingRepository.findByExercise(exercise);
            exerciseCategoryMappingRepository.deleteInBatch(exerciseCategoryMappings);
        }
        fitwiseUtils.makeExerciseAnonymous(exercise);
        if(isByAdmin){
            exercise.setLastModifiedBy(user);
        }
        exerciseRepository.save(exercise);
        log.info("Make exercise anonymous : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        //deleting video management table entry for exercise and supporting video.
        // Stop deleting videos management table entry.
        /*if (exerciseVideo != null) {
            videoManagementRepo.delete(exerciseVideo);
        }
        if (supportingVideo != null) {
            videoManagementRepo.delete(supportingVideo);
        }
        log.info("Delete support and exercise video obj : " + (new Date().getTime() - temp));*/
        //Sending mail to instructor
        if (sendMailNotification) {
            temp = new Date().getTime();
            String subject = EmailConstants.EXERCISE_DELETE_SUBJECT.replace("#EXERCISE_NAME#", "'" + title + "'");
            String mailBody = EmailConstants.EXERCISE_DELETE_CONTENT.replace("#EXERCISE_NAME#", "<b>" + title + "</b>");
            String userName = fitwiseUtils.getUserFullName(owner);
            mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody);
            mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
            asyncMailer.sendHtmlMail(owner.getEmail(), subject, mailBody);
            log.info("Email send : " + (new Date().getTime() - temp));
        }
        log.info("Delete exercise Completed " + (new Date().getTime() - startTime));
    }

    /**
     * Editing exercise video and support video
     *
     * @param exerciseModel
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    public ResponseModel editExercise(ExerciseModel exerciseModel) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.info("Edit exercise started " + new Date());
        Exercises exercises;
        String tempStatus = "";
        User user = userComponents.getUser();
        UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId());
        if (exerciseModel.getExerciseId() != null) {
            exercises = exerciseRepository.findByExerciseId(exerciseModel.getExerciseId());
            if (exercises == null){
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_NOT_FOUND, null);
            }else{
                if(exercises.isByAdmin()){
                    throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_STOCK_EXERCISE_CANNOT_EDITED_BY_INSTRUCTOR, null);
                }
            }
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_NOT_FOUND, MessageConstants.ERROR);
        }
        if (exerciseModel.getTitle() != null && !exercises.getTitle().equals(exerciseModel.getTitle())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_TITLE_CHANGE, MessageConstants.ERROR);
        }
        int count = 0;
        boolean isEqual = false;
        if (exerciseModel.getEquipments().size() == exercises.getEquipments().size()) {
            for (Equipments equipment : exerciseModel.getEquipments()) {
                for (Equipments equipments : exercises.getEquipments()) {
                    if (equipment.getEquipmentId().equals(equipments.getEquipmentId())) {
                        count++;
                    }
                }
            }
            if (count == exerciseModel.getEquipments().size()) {
                isEqual = true;
            }
        }
        if (exerciseModel.getEquipments() != null && !isEqual) {
            exercises.setEquipments(null);
            exerciseRepository.save(exercises);
            List<Equipments> equipmentsList = new ArrayList<>();
            for (Equipments equipment : exerciseModel.getEquipments()) {
                Equipments equipmentFromDb = equipmentsRepository.findByEquipmentId(equipment.getEquipmentId());
                equipmentsList.add(equipmentFromDb);
            }
            if (!equipmentsList.isEmpty())
                exercises.setEquipments(equipmentsList);
            exerciseRepository.save(exercises);
        }
        if (exercises.getSupportVideoManagement() != null && exerciseModel.getSupportingVideoModel() != null) {
            VideoVersioningModel videoVersioningModel = new VideoVersioningModel();
            videoVersioningModel.setFileName(exerciseModel.getSupportingVideoModel().getFileName());
            videoVersioningModel.setFileSize(Long.toString(exerciseModel.getSupportingVideoModel().getFileSize()));
            videoVersioningModel.setVersioningEntityId(exerciseModel.getExerciseId());
            videoVersioningModel = instructorProgramService.createVideoVersion(videoVersioningModel, InstructorConstant.VIDEO_TYPE_EXERCISE_SUPPORT_VIDEO, false);
            if (exercises.getSupportVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.UPLOAD) || exercises.getSupportVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.INPROGRESS)) {
                tempStatus = VideoUploadStatus.UPLOAD;
            } else {
                tempStatus = VideoUploadStatus.REUPLOAD;
            }
            /**
             * If the video processing was failed first time, marking it as upload status
             * If the video processing was failed more than one time, marking it as re-upload status
             */
            if (exercises.getSupportVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_PROCESSING_FAILED)) {
                tempStatus = VideoUploadStatus.UPLOAD;
            } else if (exercises.getSupportVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED)) {
                tempStatus = VideoUploadStatus.REUPLOAD;
            }

            exercises.getSupportVideoManagement().setUploadStatus(tempStatus);
            exerciseModel.getSupportingVideoModel().setVideoUploadStatus(tempStatus);
            VimeoModel supportingVimeoModel = new VimeoModel();
            doConstructSupportingVideoVimeoModel(exerciseModel.getSupportingVideoModel(), supportingVimeoModel);
            supportingVimeoModel.getUpload().setUpload_link(videoVersioningModel.getUploadLink());
            exerciseModel.getSupportingVideoModel().setVimeoData(supportingVimeoModel);
            videoManagementRepo.save(exercises.getSupportVideoManagement());
        } else if (exercises.getSupportVideoManagement() == null && exerciseModel.getSupportingVideoModel() != null) {
            SupportingVideoModel supportingVideoModel = exerciseModel.getSupportingVideoModel();
            VimeoModel supportingVimeoModel = new VimeoModel();
            if (!exerciseModel.getSupportingVideoModel().getFileName().isEmpty() &&
                    userProfile.getFirstName() != null && userProfile.getLastName() != null) {
                supportingVideoModel.setFileName(userProfile.getFirstName() + " " + userProfile.getLastName() + " - " + supportingVideoModel.getFileName());
            }
            doConstructSupportingVideoVimeoModel(supportingVideoModel, supportingVimeoModel);
            supportingVimeoModel = vimeoService.createVideoPlaceholder(supportingVimeoModel);
            exerciseModel.getSupportingVideoModel().setVimeoData(supportingVimeoModel);
            VideoManagement supportingVideoManagement = new VideoManagement();
            supportingVideoManagement.setOwner(user);
            supportingVideoManagement.setUploadStatus(VideoUploadStatus.UPLOAD);
            doConstructSupportVideoManagement(exerciseModel.getSupportingVideoModel(), supportingVimeoModel, supportingVideoManagement);
            exerciseModel.getSupportingVideoModel().setVideoUploadStatus(VideoUploadStatus.UPLOAD);
            exercises.setSupportVideoManagement(supportingVideoManagement);
            videoManagementRepo.save(supportingVideoManagement);
            exerciseRepository.save(exercises);
        } else if (exercises.getSupportVideoManagement() != null && exerciseModel.getIsDeleteSupportVideo()) {
            String vimeoUrl = exercises.getSupportVideoManagement().getUrl();
            VideoManagement videoManagement = exercises.getSupportVideoManagement();
            exercises.setSupportVideoManagement(null);
            exerciseRepository.save(exercises);
            videoManagementRepo.delete(videoManagement);
            vimeoService.deleteVimeoVideo(vimeoUrl);
        }
        if (exerciseModel.getFileName() != null) {
            VideoVersioningModel videoVersioningModel = new VideoVersioningModel();
            videoVersioningModel.setFileName(exerciseModel.getFileName());
            videoVersioningModel.setFileSize(Long.toString(exerciseModel.getFileSize()));
            videoVersioningModel.setVersioningEntityId(exerciseModel.getExerciseId());
            videoVersioningModel = instructorProgramService.createVideoVersion(videoVersioningModel, InstructorConstant.VIDEO_TYPE_EXERCISE_VIDEO, false);
            if (exercises.getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.UPLOAD) || exercises.getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.INPROGRESS)) {
                tempStatus = VideoUploadStatus.UPLOAD;
            } else {
                tempStatus = VideoUploadStatus.REUPLOAD;
            }

            /**
             * If the video processing was failed first time, marking it as upload status
             * If the video processing was failed more than one time, marking it as re-upload status
             */
            if (exercises.getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_PROCESSING_FAILED)) {
                tempStatus = VideoUploadStatus.UPLOAD;
            } else if (exercises.getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED)) {
                tempStatus = VideoUploadStatus.REUPLOAD;
            }


            exercises.getVideoManagement().setUploadStatus(tempStatus);
            exerciseModel.setVideoUploadStatus(tempStatus);
            videoManagementRepo.save(exercises.getVideoManagement());
            VimeoModel vimeoModel = new VimeoModel();
            doConstructVimeoModel(exerciseModel, vimeoModel);
            vimeoModel.getUpload().setUpload_link(videoVersioningModel.getUploadLink());
            exerciseModel.setVimeoData(vimeoModel);
        }
        if (exerciseModel.getImageId() != null) {
            VideoManagement videoManagement = exercises.getVideoManagement();
            Images image = imageRepository.findByImageId(exerciseModel.getImageId());
            if (image != null) {
                videoManagement.setThumbnail(image);
                videoManagementRepo.save(videoManagement);
            }
        }
        if (exerciseModel.getSupportingVideoModel() != null && exerciseModel.getSupportingVideoModel().getImageId() != null) {
            VideoManagement videoManagement = exercises.getSupportVideoManagement();
            Images image = imageRepository.findByImageId(exerciseModel.getSupportingVideoModel().getImageId());
            if (image != null) {
                videoManagement.setThumbnail(image);
                videoManagementRepo.save(videoManagement);
            }
        }
        log.info("Exercise edit completed on " + new Date());
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_EXERCISE_UPDATED, exerciseModel);
    }
    
    
	/**
	 * Gets the instructor videos.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @param searchName the search name
	 * @return the instructor videos
	 */
	public ResponseModel getInstructorVideos(int pageNo, int pageSize, String sortOrder, String sortBy,
			Optional<String> searchName) {
		log.info("Get Instructor videos starts");
		long start = new Date().getTime();
		if (pageNo <= 0 || pageSize <= 0) {
			throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
		}
		if (!(sortBy.equalsIgnoreCase(SearchConstants.TITLE)
				|| sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE))) {
			throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
		}
		if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)
				|| sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
			throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
		}
		User user = userComponents.getUser();
		Sort sort = getInstuctorVideoSortCriteria(sortBy);
		if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
			sort = sort.descending();
		}
		PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
		log.info("Basic validations and get user : Time taken in millis : " + (new Date().getTime() - start));
		long temp = new Date().getTime();
		Page<VideoManagement> userVideoList;
		if (searchName.isPresent() && !searchName.get().isEmpty()) {
			userVideoList = videoManagementRepo.findByOwnerAndTitle(user, searchName.get(), pageRequest);
		} else {
			userVideoList = videoManagementRepo.findByOwner(user, pageRequest);
		}
		log.info("Query : Time taken in millis : " + (new Date().getTime() - temp));
		Map<String, Object> map = new HashMap<>();
		if (userVideoList.isEmpty()) {
			throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE,
					null);
		}
		temp = new Date().getTime();
		List<VimeoVideoView> vimeoVideoList = new ArrayList<>();
		for (VideoManagement video : userVideoList.getContent()) {
			if (video != null) {
				VimeoVideoView videoView = new VimeoVideoView();
				videoView.setUri(video.getUrl());
				videoView.setName(video.getTitle());
				videoView.setVideoId(video.getVideoManagementId());
				videoView.setDuration(video.getDuration());
				videoView.setVideoUploadStatus(video.getUploadStatus());
				/**
				 * If the video processing was failed first time, marking it as upload status If
				 * the video processing was failed more than one time, marking it as re-upload
				 * status
				 */
				if (video.getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_PROCESSING_FAILED)) {
					videoView.setVideoUploadStatus(VideoUploadStatus.UPLOAD);
				} else if (video.getUploadStatus()
						.equalsIgnoreCase(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED)) {
					videoView.setVideoUploadStatus(VideoUploadStatus.REUPLOAD);
				}
				if (video.getThumbnail() != null) {
					videoView.setImageId(video.getThumbnail().getImageId());
					videoView.setThumbnailUrl(video.getThumbnail().getImagePath());
				}
				vimeoVideoList.add(videoView);
			}
		}
		log.info("Response construction : Time taken in millis : " + (new Date().getTime() - temp));
		map.put(KeyConstants.KEY_TOTAL_COUNT, userVideoList.getTotalElements());
		map.put(KeyConstants.KEY_VIDEOS, vimeoVideoList);
		log.info("Get instructor videos : Total Time taken in millis : " + (new Date().getTime() - start));
		log.info("Get instructor videos ends");
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, map);
	}
	
	/**
	 * Gets the instuctor video sort criteria.
	 *
	 * @param sortBy the sort by
	 * @return the instuctor video sort criteria
	 */
	public Sort getInstuctorVideoSortCriteria(String sortBy) {
		Sort sort;
		if (sortBy.equalsIgnoreCase(SearchConstants.TITLE)) {
			sort = Sort.by(SearchConstants.TITLE);
		} else {
			sort = Sort.by("videoManagementId");
		}
		return sort;
	}
}