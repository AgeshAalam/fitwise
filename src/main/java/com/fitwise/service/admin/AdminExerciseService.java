package com.fitwise.service.admin;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.ValidationConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.VideoUploadStatus;
import com.fitwise.constants.VimeoConstants;
import com.fitwise.entity.Equipments;
import com.fitwise.entity.Exercises;
import com.fitwise.entity.Images;
import com.fitwise.entity.User;
import com.fitwise.entity.VideoManagement;
import com.fitwise.entity.admin.ExerciseCategory;
import com.fitwise.entity.admin.ExerciseCategoryMapping;
import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.daoImpl.ExerciseRepoImpl;
import com.fitwise.exercise.model.SupportingVideoModel;
import com.fitwise.exercise.model.UploadModel;
import com.fitwise.exercise.model.VimeoModel;
import com.fitwise.exercise.service.ExerciseService;
import com.fitwise.exercise.service.VimeoService;
import com.fitwise.model.AdminExerciseModel;
import com.fitwise.model.ExerciseCategoryModel;
import com.fitwise.model.ExerciseListResponse;
import com.fitwise.model.ExerciseResponse;
import com.fitwise.model.instructor.VideoVersioningModel;
import com.fitwise.repository.EquipmentsRepository;
import com.fitwise.repository.ExerciseRepository;
import com.fitwise.repository.ImageRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.VideoManagementRepo;
import com.fitwise.repository.admin.ExerciseCategoryMappingRepository;
import com.fitwise.repository.admin.ExerciseCategoryRepository;
import com.fitwise.response.VimeoVideoView;
import com.fitwise.service.instructor.InstructorProgramService;
import com.fitwise.specifications.ExerciseSpecification;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.admin.ExerciseCategoryCountListView;
import com.fitwise.view.admin.ExerciseCategoryCountView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminExerciseService {

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private ExerciseCategoryRepository exerciseCategoryRepository;

    @Autowired
    private ExerciseCategoryMappingRepository exerciseCategoryMappingRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private FitwiseUtils fitwiseUtils;

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private ExerciseRepoImpl exerciseImpl;

    @Autowired
    private VimeoService vimeoService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private EquipmentsRepository equipmentsRepository;

    @Autowired
    private VideoManagementRepo videoManagementRepo;

    @Autowired
    private InstructorProgramService instructorProgramService;


    /**
     * Get Exercise categories along with count of exercises
     *
     * @return
     */
    public ResponseModel getExerciseCategoriesWithCount() {
        log.info("Get Exercise categories starts");
        long start = System.currentTimeMillis();
        List<ExerciseCategory> exerciseCategoryList = exerciseCategoryRepository.findAll();
        if (exerciseCategoryList.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        log.info("Query : Time taken in millis : " + (System.currentTimeMillis() - start));
        ExerciseCategoryCountListView exerciseCategoryCountListView = new ExerciseCategoryCountListView();
        List<ExerciseCategoryCountView> exerciseCategoryCountViews = new ArrayList<>();

        long temp = System.currentTimeMillis();
        for (ExerciseCategory exerciseCategory : exerciseCategoryList) {
            long count = exerciseCategoryMappingRepository.countByExerciseCategoryCategoryId(exerciseCategory.getCategoryId());
            ExerciseCategoryCountView exerciseCategoryCountView = new ExerciseCategoryCountView();
            exerciseCategoryCountView.setCategoryId(exerciseCategory.getCategoryId());
            exerciseCategoryCountView.setCategory(exerciseCategory.getCategoryName());
            exerciseCategoryCountView.setCount(count);
            exerciseCategoryCountViews.add(exerciseCategoryCountView);
        }
        exerciseCategoryCountListView.setExerciseCategories(exerciseCategoryCountViews);
        exerciseCategoryCountListView.setTotalCount(exerciseRepository.countByIsByAdminAndOwnerNotNull(true));
        log.info("Response construction : Time taken in millis : " + (System.currentTimeMillis() - temp));
        log.info("Get exercise categories : Total Time taken in millis : " + (System.currentTimeMillis() - start));
        log.info("Get Exercise categories ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, exerciseCategoryCountListView);
    }


    /**
     * Get All stock exercises
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param categoryId
     * @param searchName
     * @return
     */
    public ResponseModel getAllStockExercises(int pageNo, int pageSize, String sortOrder, String sortBy, Optional<Long> categoryId,  Optional<String> searchName) {
       List<Long> categoryIds = new ArrayList<>();
       if(categoryId.isPresent() && categoryId.get() != 0){
           categoryIds.add(categoryId.get());
       }
       return getAllStockExercises(pageNo, pageSize, sortOrder, sortBy, categoryIds, null, searchName, true);
    }


        /**
         * Get all stock exercises
         *
         * @param pageNo
         * @param pageSize
         * @param sortOrder
         * @param sortBy
         * @param categoryIds
         * @param searchName
         * @return
         */
    public ResponseModel getAllStockExercises(int pageNo, int pageSize, String sortOrder, String sortBy, List<Long> categoryIds, List<Long> equipmentIds,  Optional<String> searchName, boolean isViaAdmin) {
        log.info("Get Stock exrecises starts");
        long start = System.currentTimeMillis();
        if (isViaAdmin && !userComponents.getRole().equalsIgnoreCase(KeyConstants.KEY_ADMIN)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }

        if (!isViaAdmin && !userComponents.getRole().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, MessageConstants.ERROR);
        }

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        if (!(sortBy.equalsIgnoreCase(SearchConstants.TITLE) || sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE))) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_SORT_BY, null);
        }

        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }
        log.info("Basic valiations : Time taken in millis : " + (System.currentTimeMillis() - start));

        long temp = System.currentTimeMillis();
        Sort sort = exerciseService.getExerciseLibrarySortCriteria(sortBy);
        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            sort = sort.descending();
        }
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
        log.info("Page request : Time taken in millis : " + (System.currentTimeMillis() - temp));

        List<Long> categoryIdList = new ArrayList<>();

        temp = System.currentTimeMillis();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            categoryIdList.addAll(categoryIds);
        } else {
            List<ExerciseCategory> exerciseCategories = exerciseCategoryRepository.findAll();
            categoryIdList = exerciseCategories.stream()
                    .map(ExerciseCategory::getCategoryId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        log.info("Category id list : Time taken in millis : " + (System.currentTimeMillis() - temp));

        temp = System.currentTimeMillis();
        Specification finalSpec = ExerciseSpecification.geStockExercises();
        if (!categoryIdList.isEmpty()) {
            Specification<Exercises> categorySpecification = ExerciseSpecification.getExercisesByCategories(categoryIdList);
            finalSpec = finalSpec.and(categorySpecification);
        }

        if (searchName.isPresent() && !searchName.get().isEmpty()) {
            Specification<Exercises> searchSpecification = ExerciseSpecification.getExercisesByTitleSearch(searchName.get());
            finalSpec = finalSpec.and(searchSpecification);
        }

        if(equipmentIds != null && !equipmentIds.isEmpty()){
            Specification<Exercises> equipmentSpecification = ExerciseSpecification.getExercisesByEquipments(equipmentIds);
            finalSpec = finalSpec.and(equipmentSpecification);
        }

        Page<Exercises> exercisesPage = exerciseRepository.findAll(finalSpec, pageRequest);
        log.info("Query : Time taken in millis : " + (System.currentTimeMillis() - temp));
        if(exercisesPage.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,MessageConstants.MSG_DATA_NOT_AVAILABLE,null);
        }

        temp = System.currentTimeMillis();
        List<ExerciseResponse> exerciseResponses = new ArrayList<>();
        for (Exercises exercise : exercisesPage.getContent()) {
            ExerciseResponse exerciseResponse = exerciseService.constructExerciseResponseView(exercise);
            exerciseResponses.add(exerciseResponse);
        }
        log.info("Response construction : Time taken in millis : " + (System.currentTimeMillis() - temp));

        ExerciseListResponse exerciseListResponse = new ExerciseListResponse();
        exerciseListResponse.setExerciseResponses(exerciseResponses);
        exerciseListResponse.setTotalcount(exercisesPage.getTotalElements());
        log.info("Get stock exercises : Total Time taken in millis : " + (System.currentTimeMillis() - start));
        log.info("Get Stock exrecises ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, exerciseListResponse);
    }

    @Transactional
    public ResponseModel createExercise(final AdminExerciseModel request) throws IOException {
        log.info("Create Exercise starts");
        User user = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(user);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
        Date createExerciseStart = new Date();
        ResponseModel response = new ResponseModel();
        VimeoModel vimeoModel = new VimeoModel();
        request.setFileName(KeyConstants.KEY_ADMIN + " - " + request.getFileName());

        doConstructVimeoModel(request, vimeoModel);
        long profilingStart = new Date().getTime();
        vimeoModel = vimeoService.createVideoPlaceholder(vimeoModel);
        long profilingEnd = new Date().getTime();
        log.info("Creating placeholder for exercise video : time taken in millis : " + (profilingEnd - profilingStart));
        validateExercise(request);
        if(request.getExerciseCategoryModels() == null || request.getExerciseCategoryModels().isEmpty()){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_CATEGORY_NULL, null);
        }
        //Supporting Video vimeo model
        boolean isSupportingVideoPresent = false;
        VimeoModel supportingVimeoModel = null;
        SupportingVideoModel supportingVideoModel = request.getSupportingVideoModel();
        if (supportingVideoModel != null) {
            isSupportingVideoPresent = true;
            supportingVimeoModel = new VimeoModel();
            if (!request.getFileName().isEmpty()) {
                supportingVideoModel.setFileName(KeyConstants.KEY_ADMIN + " - " + request.getFileName());
            }
            exerciseService.doConstructSupportingVideoVimeoModel(supportingVideoModel, supportingVimeoModel);
            Date supportStart = new Date();
            log.info("createVideoPlaceholder for supporting video started at : " + supportStart);
            supportingVimeoModel = vimeoService.createVideoPlaceholder(supportingVimeoModel);
            Date supportEnd = new Date();
            log.info("createVideoPlaceholder for supporting video ended at : " + supportEnd );
            log.info("createVideoPlaceholder for supporting video completion took : " + (supportEnd.getTime() - supportStart.getTime()) + " ms");
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
        log.info("createExercise for exercise ended at : " + createExerciseEnd);
        log.info("createExercise completion took : " + (createExerciseEnd.getTime() - createExerciseStart.getTime()) + " ms");

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
    private void doConstructExerciseEntity(Exercises exercise, AdminExerciseModel request, VimeoModel vimeoModel, VimeoModel supportingVideoVimeoModel) {
        User user = userComponents.getUser();
        String exerciseTitle = request.getTitle();
        exercise.setTitle(exerciseTitle);
        exercise.setDescription(request.getDescription());
        //Duplicate Exercise title validation
        List<Exercises> exercisesWithSameTitle = exerciseRepository.findByIsByAdminAndTitleIgnoreCase(true, exerciseTitle);
        if (!exercisesWithSameTitle.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_EXERCISE_DUPLICATE_TITLE, MessageConstants.ERROR);
        }
        exercise.setOwner(user);
        VideoManagement videoManagement = new VideoManagement();
        videoManagement.setOwner(user);
        Images image = imageRepository.findByImageId(request.getImageId());
        if (image != null)
            videoManagement.setThumbnail(image);
        videoManagement.setUploadStatus(VideoUploadStatus.UPLOAD);
        request.setVideoUploadStatus(VideoUploadStatus.UPLOAD);
        exerciseService.doConstructVideoManagement(exercise, vimeoModel, videoManagement);
        exercise.setVideoManagement(videoManagement);

        if (supportingVideoVimeoModel != null) {
            VideoManagement supportingVideoManagement = new VideoManagement();
            supportingVideoManagement.setOwner(user);
            Images supportVideoImage = imageRepository.findByImageId(request.getSupportingVideoModel().getImageId());
            if (supportVideoImage != null)
                supportingVideoManagement.setThumbnail(supportVideoImage);
            supportingVideoManagement.setUploadStatus(VideoUploadStatus.UPLOAD);
            request.getSupportingVideoModel().setVideoUploadStatus(VideoUploadStatus.UPLOAD);
            exerciseService.doConstructSupportVideoManagement(request.getSupportingVideoModel(), supportingVideoVimeoModel, supportingVideoManagement);
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

        List<ExerciseCategoryMapping> exerciseCategoryMappings = new ArrayList<>();
        for (ExerciseCategoryModel exerciseCategoryModel : request.getExerciseCategoryModels()) {
            ExerciseCategory category = exerciseCategoryRepository.findByCategoryId(exerciseCategoryModel.getCategoryId());
            ValidationUtils.throwException(category == null, ValidationMessageConstants.MSG_EXERCISE_CATEGORY_NOT_FOUND, Constants.BAD_REQUEST);
            ExerciseCategoryMapping exerciseCategoryMapping = new ExerciseCategoryMapping();
            exerciseCategoryMapping.setExercise(exercise);
            exerciseCategoryMapping.setExerciseCategory(category);
            exerciseCategoryMappings.add(exerciseCategoryMapping);
        }
        exercise.setExerciseCategoryMappings(exerciseCategoryMappings);
        exercise.setByAdmin(true);
    }

    /**
     * Delete Exercise
     *
     * @param exerciseId
     * @return
     */
    public ResponseModel deleteExercise(Long exerciseId) {
        User user = userComponents.getUser();
        boolean isAdmin = fitwiseUtils.isAdmin(user);
        if (!isAdmin) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
        exerciseService.deleteExercise(exerciseId, user, true, true);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_EXERCISE_DELETED, null);
    }

    /**
     * Edit exercise
     *
     * @param exerciseModel
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    public ResponseModel editExercise(AdminExerciseModel exerciseModel) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.info("Edit exercise started " + new Date());
        long start = System.currentTimeMillis();
        User user = userComponents.getUser();
        if (!userComponents.getRole().equalsIgnoreCase(KeyConstants.KEY_ADMIN)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN, MessageConstants.ERROR);
        }
        Exercises exercises;
        String tempStatus = "";
        if (exerciseModel.getExerciseId() != null) {
            exercises = exerciseRepository.findByExerciseIdAndIsByAdmin(exerciseModel.getExerciseId(), true);
            if (exercises == null){
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_NOT_FOUND, null);
            }
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_NOT_FOUND, MessageConstants.ERROR);
        }
        if (exerciseModel.getTitle() != null && !exercises.getTitle().equals(exerciseModel.getTitle())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_TITLE_CHANGE, MessageConstants.ERROR);
        }

        if(exerciseModel.getExerciseCategoryModels() == null || exerciseModel.getExerciseCategoryModels().isEmpty()){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_CATEGORY_NULL, null);
        }
        log.info("Basic validations for id and title : Time taken in millis : "+(System.currentTimeMillis() - start));

        long temp = System.currentTimeMillis();
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
            exercises.setLastModifiedBy(user);
            exerciseRepository.save(exercises);
        }
        log.info("Exercise equipment construction : Time taken in millis : "+(System.currentTimeMillis() - temp));

        temp = System.currentTimeMillis();
        if (exercises.getSupportVideoManagement() != null && exerciseModel.getSupportingVideoModel() != null) {
            VideoVersioningModel videoVersioningModel = new VideoVersioningModel();
            videoVersioningModel.setFileName(exerciseModel.getSupportingVideoModel().getFileName());
            videoVersioningModel.setFileSize(Long.toString(exerciseModel.getSupportingVideoModel().getFileSize()));
            videoVersioningModel.setVersioningEntityId(exerciseModel.getExerciseId());
            videoVersioningModel = instructorProgramService.createVideoVersion(videoVersioningModel, InstructorConstant.VIDEO_TYPE_EXERCISE_SUPPORT_VIDEO, true);
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
            exerciseService.doConstructSupportingVideoVimeoModel(exerciseModel.getSupportingVideoModel(), supportingVimeoModel);
            supportingVimeoModel.getUpload().setUpload_link(videoVersioningModel.getUploadLink());
            exerciseModel.getSupportingVideoModel().setVimeoData(supportingVimeoModel);
            videoManagementRepo.save(exercises.getSupportVideoManagement());
        } else if (exercises.getSupportVideoManagement() == null && exerciseModel.getSupportingVideoModel() != null) {
            SupportingVideoModel supportingVideoModel = exerciseModel.getSupportingVideoModel();
            VimeoModel supportingVimeoModel = new VimeoModel();
            if (!exerciseModel.getSupportingVideoModel().getFileName().isEmpty()) {
                supportingVideoModel.setFileName(KeyConstants.KEY_ADMIN + " - " + supportingVideoModel.getFileName());
            }
            exerciseService.doConstructSupportingVideoVimeoModel(supportingVideoModel, supportingVimeoModel);
            supportingVimeoModel = vimeoService.createVideoPlaceholder(supportingVimeoModel);
            exerciseModel.getSupportingVideoModel().setVimeoData(supportingVimeoModel);
            VideoManagement supportingVideoManagement = new VideoManagement();
            supportingVideoManagement.setOwner(user);
            supportingVideoManagement.setUploadStatus(VideoUploadStatus.UPLOAD);
            exerciseService.doConstructSupportVideoManagement(exerciseModel.getSupportingVideoModel(), supportingVimeoModel, supportingVideoManagement);
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
        log.info("Exercise support video update : Time taken in millis : "+(System.currentTimeMillis() - temp));

        temp = System.currentTimeMillis();
        if (exerciseModel.getFileName() != null) {
            VideoVersioningModel videoVersioningModel = new VideoVersioningModel();
            videoVersioningModel.setFileName(exerciseModel.getFileName());
            videoVersioningModel.setFileSize(Long.toString(exerciseModel.getFileSize()));
            videoVersioningModel.setVersioningEntityId(exerciseModel.getExerciseId());
            videoVersioningModel = instructorProgramService.createVideoVersion(videoVersioningModel, InstructorConstant.VIDEO_TYPE_EXERCISE_VIDEO, true);
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
        log.info("Exercise video update : Time taken in millis : "+(System.currentTimeMillis() - temp));

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
        temp = System.currentTimeMillis();
        //Save exercise categories
        List<ExerciseCategoryMapping> exerciseCategoryMappingsList = exerciseCategoryMappingRepository.findByExercise(exercises);
        exerciseCategoryMappingRepository.deleteInBatch(exerciseCategoryMappingsList);
        List<ExerciseCategoryMapping> exerciseCategoryMappings = new ArrayList<>();
        for (ExerciseCategoryModel exerciseCategoryModel : exerciseModel.getExerciseCategoryModels()) {
            ExerciseCategory category = exerciseCategoryRepository.findByCategoryId(exerciseCategoryModel.getCategoryId());
            ValidationUtils.throwException(category == null, ValidationMessageConstants.MSG_EXERCISE_CATEGORY_NOT_FOUND, Constants.BAD_REQUEST);
            ExerciseCategoryMapping exerciseCategoryMapping;
            exerciseCategoryMapping = new ExerciseCategoryMapping();
            exerciseCategoryMapping.setExercise(exercises);
            exerciseCategoryMapping.setExerciseCategory(category);
            exerciseCategoryMappings.add(exerciseCategoryMapping);

        }
        exercises.setExerciseCategoryMappings(exerciseCategoryMappings);
        exerciseRepository.save(exercises);
        log.info("Exercise category construction : Time taken in millis : "+(System.currentTimeMillis() - temp));
        log.info("Exercise edit : Time taken in millis : "+(System.currentTimeMillis() - start));
        log.info("Exercise edit completed ");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_EXERCISE_UPDATED, exerciseModel);
    }

    /**
     * Construct vimeo model
     *
     * @param exercise
     * @param vimeoModel
     */
    private void doConstructVimeoModel(AdminExerciseModel exercise, VimeoModel vimeoModel) {
        vimeoModel.setName(exercise.getFileName());
        UploadModel upload = new UploadModel();
        upload.setSize(exercise.getFileSize());
        upload.setApproach(VimeoConstants.APPROACH);
        vimeoModel.setUpload(upload);
    }

    /**
     * Validating exercise request
     *
     * @param exerciseModel
     */
    private void validateExercise(AdminExerciseModel exerciseModel) {
        ValidationUtils.throwException(exerciseModel == null, "Exercise cant be null", Constants.BAD_REQUEST);
        ValidationUtils.throwException(ValidationUtils.isEmptyString(exerciseModel.getTitle()), ValidationMessageConstants.MSG_TITLE_NULL, Constants.BAD_REQUEST);
        ValidationUtils.throwException(ValidationUtils.isEmptyString(exerciseModel.getFileName()), "File name cant be null or empty", Constants.BAD_REQUEST);
        ValidationUtils.throwException(exerciseModel.getFileSize() == null || exerciseModel.getFileSize() == 0, "File size cant be null or 0", Constants.BAD_REQUEST);
    }

    /**
     * Validate title
     * @param exerciseName
     */
    public void validateName(String exerciseName) {
        ValidationUtils.validateName(exerciseName, ValidationConstants.NAME_LENGTH_CHAR_50);
        List<Exercises> exercises = exerciseRepository.findByIsByAdminAndTitleIgnoreCase(true, exerciseName);
        if (!exercises.isEmpty()) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_EXERCISE_DUPLICATE_TITLE, null);
        }
    }

    public ExerciseResponse getExercise(final Long exerciseId) {
        Exercises exercise = exerciseRepository.findByExerciseIdAndIsByAdmin(exerciseId, true);
        if (exercise == null) {
            throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_EXERCISE_NOT_FOUND, null);
        }
        return exerciseService.constructExerciseResponseView(exercise);
    }
    
	/**
	 * Gets the stock exercises videos.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @param categoryIds the category ids
	 * @param equipmentIds the equipment ids
	 * @param searchName the search name
	 * @param isViaAdmin the is via admin
	 * @return the stock exercises videos
	 */
	public ResponseModel getStockExercisesVideos(int pageNo, int pageSize, String sortOrder, String sortBy,
			List<Long> categoryIds, Optional<String> searchName, boolean isViaAdmin) {
		log.info("Get Stock exrecises videos starts");
		long start = System.currentTimeMillis();
		if (isViaAdmin && !userComponents.getRole().equalsIgnoreCase(KeyConstants.KEY_ADMIN)) {
			throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_ADMIN,
					MessageConstants.ERROR);
		}
		if (!isViaAdmin && !userComponents.getRole().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)) {
			throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR,
					MessageConstants.ERROR);
		}
		if (pageNo <= 0 || pageSize <= 0) {
			throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
		}
		if (!(sortBy.equalsIgnoreCase(SearchConstants.TITLE)
				|| sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE))) {
			throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_SORT_BY, null);
		}
		if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)
				|| sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
			throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
		}
		log.info("Basic valiations : Time taken in millis : " + (System.currentTimeMillis() - start));

		long temp = System.currentTimeMillis();
		Sort sort = exerciseService.getExerciseLibrarySortCriteria(sortBy);
		if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
			sort = sort.descending();
		}
		PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
		log.info("Page request : Time taken in millis : " + (System.currentTimeMillis() - temp));
		List<Long> categoryIdList = new ArrayList<>();
		temp = System.currentTimeMillis();
		if (categoryIds != null && !categoryIds.isEmpty()) {
			categoryIdList.addAll(categoryIds);
		} else {
			List<ExerciseCategory> exerciseCategories = exerciseCategoryRepository.findAll();
			categoryIdList = exerciseCategories.stream().map(ExerciseCategory::getCategoryId).distinct()
					.collect(Collectors.toList());
		}
		log.info("Category id list : Time taken in millis : " + (System.currentTimeMillis() - temp));
		temp = System.currentTimeMillis();
		Specification finalSpec = ExerciseSpecification.geStockExercises();
		if (!categoryIdList.isEmpty()) {
			Specification<Exercises> categorySpecification = ExerciseSpecification
					.getExercisesByCategories(categoryIdList);
			finalSpec = finalSpec.and(categorySpecification);
		}
		if (searchName.isPresent() && !searchName.get().isEmpty()) {
			Specification<Exercises> searchSpecification = ExerciseSpecification
					.getExercisesByTitleSearch(searchName.get());
			finalSpec = finalSpec.and(searchSpecification);
		}
		Page<Exercises> exercisesPage = exerciseRepository.findAll(finalSpec, pageRequest);
		log.info("Query : Time taken in millis : " + (System.currentTimeMillis() - temp));
		if (exercisesPage.isEmpty()) {
			throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE,
					null);
		}
		temp = System.currentTimeMillis();
		List<VimeoVideoView> vimeoVideoList = new ArrayList<>();
		for (Exercises exercises : exercisesPage.getContent()) {
			VideoManagement video = exercises.getVideoManagement();
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

				if (exercises.isByAdmin()) {
					List<ExerciseCategoryMapping> exerciseCategoryMappings = exerciseCategoryMappingRepository
							.findByExercise(exercises);
					List<ExerciseCategoryModel> exerciseCategories = new ArrayList<>();
					for (ExerciseCategoryMapping exerciseCategoryMapping : exerciseCategoryMappings) {
						ExerciseCategoryModel exerciseCategoryModel = new ExerciseCategoryModel();
						exerciseCategoryModel
								.setCategoryId(exerciseCategoryMapping.getExerciseCategory().getCategoryId());
						exerciseCategoryModel
								.setCategoryName(exerciseCategoryMapping.getExerciseCategory().getCategoryName());
						exerciseCategories.add(exerciseCategoryModel);
					}
					videoView.setVideoExerciseCategories(exerciseCategories);
				}
				vimeoVideoList.add(videoView);
			}
		}
		log.info("Response construction : Time taken in millis : " + (System.currentTimeMillis() - temp));
		Map<String, Object> map = new HashMap<>();
		map.put(KeyConstants.KEY_TOTAL_COUNT, exercisesPage.getTotalElements());
		map.put(KeyConstants.KEY_VIDEOS, vimeoVideoList);
		log.info("Get stock exercises videos : Total Time taken in millis : " + (System.currentTimeMillis() - start));
		log.info("Get Stock exrecises videos ends");
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, map);
	}
}
