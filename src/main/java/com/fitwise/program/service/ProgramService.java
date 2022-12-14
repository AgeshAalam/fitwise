package com.fitwise.program.service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.AppConfigConstants;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.StripeConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.VideoUploadStatus;
import com.fitwise.constants.VimeoConstants;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.entity.AppConfigKeyValue;
import com.fitwise.entity.CircuitAndVoiceOverMapping;
import com.fitwise.entity.CircuitCompletion;
import com.fitwise.entity.CircuitSchedule;
import com.fitwise.entity.Duration;
import com.fitwise.entity.Equipments;
import com.fitwise.entity.ExerciseCompletion;
import com.fitwise.entity.ExerciseSchedulers;
import com.fitwise.entity.ExpertiseLevels;
import com.fitwise.entity.Images;
import com.fitwise.entity.InstructorRestActivity;
import com.fitwise.entity.PlatformWiseTaxDetail;
import com.fitwise.entity.ProgramExpertiseGoalsMapping;
import com.fitwise.entity.ProgramPriceByPlatform;
import com.fitwise.entity.ProgramPrices;
import com.fitwise.entity.ProgramPromoViews;
import com.fitwise.entity.ProgramSubTypes;
import com.fitwise.entity.ProgramTrailAudit;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.entity.ProgramWiseGoal;
import com.fitwise.entity.Programs;
import com.fitwise.entity.Promotions;
import com.fitwise.entity.QuickTourVideos;
import com.fitwise.entity.RestActivity;
import com.fitwise.entity.RestActivityToMetricMapping;
import com.fitwise.entity.RestMetric;
import com.fitwise.entity.SamplePrograms;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.VideoManagement;
import com.fitwise.entity.WorkoutCompletion;
import com.fitwise.entity.WorkoutDiscardFeedback;
import com.fitwise.entity.WorkoutFeedback;
import com.fitwise.entity.WorkoutMapping;
import com.fitwise.entity.WorkoutSchedule;
import com.fitwise.entity.Workouts;
import com.fitwise.entity.additionalResources.AdditionalResources;
import com.fitwise.entity.discounts.DiscountLevel;
import com.fitwise.entity.discounts.DiscountOfferMapping;
import com.fitwise.entity.discounts.OfferCodeDetail;
import com.fitwise.entity.instructor.InstructorTierDetails;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.model.UploadModel;
import com.fitwise.exercise.model.VimeoModel;
import com.fitwise.exercise.service.VimeoService;
import com.fitwise.program.daoImpl.ProgramsRepoImpl;
import com.fitwise.program.model.ProgramModel;
import com.fitwise.program.model.ProgramPlatformPriceModel;
import com.fitwise.program.model.ProgramPlatformPriceResponseModel;
import com.fitwise.program.model.ProgramResponseModel;
import com.fitwise.program.model.ProgramTileModel;
import com.fitwise.program.model.PromoUploadModel;
import com.fitwise.program.model.PromoUploadResponseModel;
import com.fitwise.program.model.RestActivityScheduleModel;
import com.fitwise.program.model.ScheduleModel;
import com.fitwise.program.model.WorkoutResponseModel;
import com.fitwise.program.model.WorkoutScheduleModel;
import com.fitwise.repository.AdditionalResourcesRepository;
import com.fitwise.repository.AppConfigKeyValueRepository;
import com.fitwise.repository.DurationRepo;
import com.fitwise.repository.ExpertiseLevelRepository;
import com.fitwise.repository.FlaggedExercisesSummaryRepository;
import com.fitwise.repository.ImageRepository;
import com.fitwise.repository.PlatformWiseTaxDetailRepository;
import com.fitwise.repository.ProgramExpertiseGoalsMappingRepository;
import com.fitwise.repository.ProgramPriceByPlatformRepository;
import com.fitwise.repository.ProgramPricesRepository;
import com.fitwise.repository.ProgramPromoViewsRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.ProgramSubTypeRepository;
import com.fitwise.repository.ProgramTrailAuditRepository;
import com.fitwise.repository.ProgramTypeRepository;
import com.fitwise.repository.ProgramWiseGoalRepository;
import com.fitwise.repository.PromotionRepository;
import com.fitwise.repository.QuickTourRepository;
import com.fitwise.repository.RestActivityRepository;
import com.fitwise.repository.RestActivityToMetricMappingRepository;
import com.fitwise.repository.SampleProgramsRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.VideoManagementRepo;
import com.fitwise.repository.WorkoutMappingRepository;
import com.fitwise.repository.WorkoutRepository;
import com.fitwise.repository.WorkoutScheduleRepository;
import com.fitwise.repository.discountsRepository.DiscountLevelRepository;
import com.fitwise.repository.discountsRepository.DiscountOfferMappingRepository;
import com.fitwise.repository.discountsRepository.OfferCodeDetailRepository;
import com.fitwise.repository.feedback.WorkoutDiscardFeedbackRepository;
import com.fitwise.repository.feedback.WorkoutFeedbackRepository;
import com.fitwise.repository.instructor.InstructorTierDetailsRepository;
import com.fitwise.repository.member.CircuitCompletionRepository;
import com.fitwise.repository.member.ExerciseCompletionRepository;
import com.fitwise.repository.member.WorkoutCompletionRepository;
import com.fitwise.repository.packaging.PackageProgramMappingRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.response.ProgramGoalsView;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.discountservice.DiscountsService;
import com.fitwise.service.payment.stripe.StripeService;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.ProgramSpecifications;
import com.fitwise.utils.AppUtils;
import com.fitwise.utils.Convertions;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.AdditionalResourcesListResponseView;
import com.fitwise.view.AdditionalResourcesResponseView;
import com.fitwise.view.ProgramPricesResponseView;
import com.fitwise.view.PromotionResponseView;
import com.fitwise.view.QuickTourVideosResponseView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.SampleProgramsView;
import com.fitwise.view.SubscriptionPackagePackageIdAndTitleView;
import com.fitwise.view.TourListResponseView;
import com.fitwise.view.discounts.ProgramDiscountMappingListResponseView;
import com.fitwise.view.discounts.ProgramDiscountMappingResponseView;
import com.fitwise.view.instructor.ProgramTypeWithSubTypeView;
import com.fitwise.view.program.ProgramTypeCountView;
import com.fitwise.view.program.RestActivityMetricView;
import com.fitwise.view.program.RestActivityResponse;
import com.fitwise.workout.service.WorkoutService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * The Class ProgramService.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProgramService {

    /**
     * The program impl.
     */
    @Autowired
    private ProgramsRepoImpl programImpl;

    /**
     * The program type repo.
     */
    @Autowired
    private ProgramTypeRepository programTypeRepo;

    /**
     * The program repository.
     */
    @Autowired
    private ProgramRepository programRepository;

    /**
     * The exp repo.
     */
    @Autowired
    private ExpertiseLevelRepository expRepo;

    /**
     * The workout repo.
     */
    @Autowired
    private WorkoutRepository workoutRepo;

    /**
     * The img repo.
     */
    @Autowired
    private ImageRepository imgRepo;

    /**
     * The duration repo.
     */
    @Autowired
    private DurationRepo durationRepo;

    @Autowired
    private ImageRepository imageRepository;

    /**
     * The user profile repository.
     */
    @Autowired
    private UserProfileRepository userProfileRepository;

    /**
     * The vimeo service.
     */
    @Autowired
    protected VimeoService vimeoService;

    /**
     * The promotion repository.
     */
    @Autowired
    private PromotionRepository promotionRepository;

    /**
     * The program wise goal repository.
     */
    @Autowired
    private ProgramWiseGoalRepository programWiseGoalRepository;

    /**
     * The program expertise goals mapping repository.
     */
    @Autowired
    private ProgramExpertiseGoalsMappingRepository programExpertiseGoalsMappingRepository;

    /**
     * The user components.
     */
    @Autowired
    private UserComponents userComponents;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private ProgramPriceByPlatformRepository programPriceByPlatformRepository;

    @Autowired
    private PlatformWiseTaxDetailRepository platformWiseTaxDetailRepository;

    @Autowired
    private WorkoutScheduleRepository workoutScheduleRepository;

    @Autowired
    private WorkoutMappingRepository workoutMappingRepository;

    @Autowired
    ValidationService validationService;

    @Autowired
    ProgramPromoViewsRepository programPromoCompletionStatusRepository;

    @Autowired
    private ProgramSubscriptionRepo programSubscriptionRepo;

    @Autowired
    private ProgramTrailAuditRepository programTrailAuditRepository;

    @Autowired
    private ProgramPricesRepository programPricesRepository;

    @Autowired
    private AppConfigKeyValueRepository appConfigKeyValueRepository;

    @Autowired
    private SampleProgramsRepository sampleProgramsRepository;

    @Autowired
    private QuickTourRepository quickTourRepository;

    @Autowired
    private VideoManagementRepo videoManagementRepo;

    @Autowired
    RestActivityRepository restActivityRepository;

    @Autowired
    RestActivityToMetricMappingRepository restActivityToMetricMappingRepository;

    @Autowired
    private FitwiseQboEntityService fitwiseQboEntityService;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    FlaggedExercisesSummaryRepository flaggedExercisesSummaryRepository;

    @Autowired
    ExerciseCompletionRepository exerciseCompletionRepository;
    @Autowired
    CircuitCompletionRepository circuitCompletionRepository;
    @Autowired
    WorkoutCompletionRepository workoutCompletionRepository;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private AdditionalResourcesRepository additionalResourcesRepository;
    
    /** Discount offers */
    @Autowired
    private DiscountOfferMappingRepository discountOfferMappingRepository;
    
    @Autowired
    private OfferCodeDetailRepository offerCodeDetailRepository;
    
    @Autowired
    private DiscountLevelRepository discountLevelRepository;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private DiscountsService discountsService;

    @Autowired
    private PackageProgramMappingRepository packageProgramMappingRepository;

    @Autowired
    WorkoutFeedbackRepository workoutFeedbackRepository;

    @Autowired
    WorkoutDiscardFeedbackRepository workoutDiscardFeedbackRepository;
    
    @Autowired
    private ProgramSubTypeRepository programSubTypeRepository;
    private final InstructorTierDetailsRepository instructorTierDetailsRepository;

    /**
     * Creates the program.
     *
     * @param model the model
     * @return the response model
     */
    @Transactional
    public ResponseModel createProgram(ProgramModel model) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.info("Create program starts.");
        long apiStartTimeMillis = new Date().getTime();
        ResponseModel response = new ResponseModel();
        User user = userComponents.getUser();
        doValidateRoleAsInstructor(user);
        log.info("Get user and validate role as instructor : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        Programs program;
        if (model.getProgramId() == null || model.getProgramId() == 0) {
            program = new Programs();
            program.setOwner(user);
            program.setPublish(false);
            log.info("Set program owner and publish status : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        } else {
            program = programRepository.findByProgramIdAndOwnerUserId(model.getProgramId(), user.getUserId());
            ValidationUtils.throwException(program == null, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, Constants.BAD_REQUEST);
            //Validation for edit restrictions for programs in publish, unpublish or block state
//            if (InstructorConstant.PUBLISH.equals(program.getStatus())) {
//                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PUBLISH_PROGRAM_RESTRICT_EDIT, MessageConstants.ERROR);
//            } else
            	if (InstructorConstant.UNPUBLISH.equals(program.getStatus()) || DBConstants.UNPUBLISH_EDIT.equals(program.getStatus())) {
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_UNPUBLISH_PROGRAM_RESTRICT_EDIT, MessageConstants.ERROR);
            } else if (InstructorConstant.BLOCK.equals(program.getStatus()) || DBConstants.BLOCK_EDIT.equals(program.getStatus())) {
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_BLOCK_PROGRAM_RESTRICT_EDIT, MessageConstants.ERROR);
            }
            log.info("Query: get program from DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }
        profilingEndTimeMillis = new Date().getTime();
        doConstructPrograms(program, model, false);
        log.info("Construct programs : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if(Objects.isNull(model.getProgramSubTypeId())){
        	program.setProgramSubType(null);
        }
        program = programImpl.saveProgram(program);
        log.info("Query: save program : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage("Successfully saved program");
        response.setPayload(constructProgramModel(program));
        log.info("Construct program response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Create program ends.");
        return response;
    }

    /**
     * Do construct programs.
     *
     * @param program the program
     * @param model   the model
     * @throws ApplicationException the application exception
     */
    private void doConstructPrograms(Programs program, ProgramModel model, boolean isRestrictedProgram) {
        //Plan phase changes
        boolean isStepperCompleted = constructProgramPlan(program, model, isRestrictedProgram);
        //Upload phase changes
        isStepperCompleted = constructProgramUpload(program, model, isRestrictedProgram, isStepperCompleted);
        //Schedule phase changes
        isStepperCompleted = constructProgramSchedule(program, model, isRestrictedProgram, isStepperCompleted);
        //Price phase changes & Discount offer addition
        constructProgramPrice(program, model, isRestrictedProgram, isStepperCompleted);
    }

    /**
     * construct Program Plan phase data
     *
     * @param program
     * @param model
     * @return
     */
    private boolean constructProgramPlan(Programs program, ProgramModel model, boolean isRestrictedProgram) {
        boolean isStepperCompleted = false;
        boolean isNewProgram = false;
        //Ignoring title changes for RestrictedProgram
        String programTitle = model.getTitle();
        if (!isRestrictedProgram) {
            if (program.getProgramId() == null || program.getProgramId() == 0) {
                isNewProgram = true;
            }
            program.setStatus(InstructorConstant.PLAN);
            //Plan - validation
            ValidationUtils.throwException(ValidationUtils.isEmptyString(programTitle), ValidationMessageConstants.MSG_TITLE_NULL, Constants.BAD_REQUEST);
            if (programTitle.length() < 2 || programTitle.length() > 30) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_PGM_TITLE_LENGTH, null);
            }
            //Duplicate Program title validation
            if (isNewProgram || !program.getTitle().equalsIgnoreCase(programTitle)) {
                Programs programWithSameTitle = programImpl.findByOwnerUserIdAndTitle(program.getOwner().getUserId(), programTitle);
                if (programWithSameTitle != null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PROGRAM_DUPLICATE_TITLE, MessageConstants.ERROR);
                }
            }
        } else {
//        	if (InstructorConstant.PUBLISH.equals(program.getStatus())) {
//                program.setStatus(InstructorConstant.PUBLISH);
//            }
            //setting initial program status if not available
            if (program.getPostCompletionStatus() == null) {
                program.setPostCompletionStatus(InstructorConstant.PLAN);
            }
        }
        if (isRestrictedProgram && !program.getTitle().equalsIgnoreCase(programTitle)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_RESTRICTED_PROGRAM_TITLE_CHANGE, MessageConstants.ERROR);
        }
        if (!isNewProgram && program.getWorkoutSchedules() != null && !program.getWorkoutSchedules().isEmpty() && model.getDurationId().intValue() != program.getDuration().getDurationId().intValue()) {
            throw new ApplicationException(Constants.RESET_CONTENT, MessageConstants.MSG_ERR_PGM_DURATION_UPDATE, null);
        }
        //Plan - saving data. Ignoring Title change for RestrictedProgram
        if (!isRestrictedProgram && !ValidationUtils.isEmptyString(programTitle)) {
            program.setTitle(programTitle);
        }
        if (model.getDescription() != null) {
            program.setDescription(model.getDescription());
        }
        if (!ValidationUtils.isEmptyString(model.getShortDescription()) && model.getShortDescription().length() < 10 && model.getShortDescription().length() > 45) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_PGM_SHORT_DESC_LENGTH, null);
        }
//        if (!model.isSaveAsDraft() && ValidationUtils.isEmptyString(model.getShortDescription())) {
//            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_PGM_SHORT_DESC_REQ, null);
//        }
        program.setShortDescription(model.getShortDescription());
        if (model.getProgramTypeId() != null) {
            doConstructProgramTypes(program, model);
        }
        if(model.getProgramSubTypeId() != null && model.getProgramSubTypeId() != 0) {
            doConstructProgramSubTypes(program, model);
        }
        else {
        	program.setProgramSubType(null);
        }
        if (model.getExpertiseId() != null) {
            doConstructProgramExpertise(program, model);
        }
        if (model.getProgramTypeLevelGoalMappingIds() != null && !model.getProgramTypeLevelGoalMappingIds().isEmpty()) {
            doConstructProgramGoals(program, model);
        }
        if (model.getDurationId() != null) {
            doSetProgramDuration(model.getDurationId(), program);
        }
        if (!ValidationUtils.isEmptyString(model.getDescription())) {
            program.setDescription(model.getDescription());
        }
        //Plan - Status change to Upload
//        if (!ValidationUtils.isEmptyString(model.getShortDescription()) && !ValidationUtils.isEmptyString(model.getTitle()) && model.getProgramTypeId() != null && model.getExpertiseId() != null && (model.getProgramTypeLevelGoalMappingIds() != null && !model.getProgramTypeLevelGoalMappingIds().isEmpty()) && model.getDurationId() != null) {
        if (!ValidationUtils.isEmptyString(model.getTitle()) && model.getProgramTypeId() != null && model.getExpertiseId() != null && (model.getProgramTypeLevelGoalMappingIds() != null && !model.getProgramTypeLevelGoalMappingIds().isEmpty()) && model.getDurationId() != null) {
        	     
        	if (!isRestrictedProgram) {
                program.setStatus(InstructorConstant.UPLOAD);
            } else {
                program.setPostCompletionStatus(InstructorConstant.UPLOAD);
            }
            isStepperCompleted = true;
        }
        return isStepperCompleted;
    }

    /**
     * construct Program Upload phase data
     *
     * @param program
     * @param model
     * @return
     */
    private boolean constructProgramUpload(Programs program, ProgramModel model, boolean isRestrictedProgram, boolean isPreviousStepperCompleted) {
        boolean isStepperCompleted = false;
        if (InstructorConstant.UPLOAD.equals(program.getStatus()) || (isRestrictedProgram && InstructorConstant.UPLOAD.equals(program.getPostCompletionStatus())) || model.isSaveAsDraft()) {
            //Upload - saving data
            if (model.getImageId() != 0 && model.getImageId() != null) {
                doConstructThumbnail(program, model);
            }
            if (model.getPromotionId() != 0 && model.getPromotionId() != null) {
                doConstructPromoVideo(program, model);
            }
            //Upload - validation
            //Ignoring Status changes for RestrictedProgram
            if (isPreviousStepperCompleted && model.getImageId() != 0 && model.getImageId() != null && model.getPromotionId() != 0 && model.getPromotionId() != null) {
            	if (isRestrictedProgram) {
                	if (InstructorConstant.PUBLISH.equals(program.getStatus())) {
                        program.setStatus(InstructorConstant.PUBLISH);
                    }
            	}
            	if (!isRestrictedProgram) {
                    program.setStatus(InstructorConstant.SCHEDULE);
                } else {
                    program.setPostCompletionStatus(InstructorConstant.SCHEDULE);
                }
                isStepperCompleted = true;
            }
        }
        return isStepperCompleted;
    }

    /**
     * construct Program Schedule phase data
     *
     * @param program
     * @param model
     * @return
     */
    private boolean constructProgramSchedule(Programs program, ProgramModel model, boolean isRestrictedProgram, boolean isPreviousStepperCompleted) {
        boolean isStepperCompleted = false;
        if (InstructorConstant.PUBLISH.equals(program.getStatus()) ||InstructorConstant.SCHEDULE.equals(program.getStatus()) || (isRestrictedProgram && InstructorConstant.SCHEDULE.equals(program.getPostCompletionStatus())) || model.isSaveAsDraft()) {
            if (model.getWorkoutIds() != null && !model.getWorkoutIds().isEmpty()) {
                doConstructWorkoutList(program, model);
            }
            if (model.getWorkoutSchedules() != null && !model.getWorkoutSchedules().isEmpty()) {
                boolean isFullyScheduled = doConstructScheduleModel(program, model);
                validateFlaggedVideosInProgram(program.getWorkoutSchedules());
                //Ignoring Status changes for RestrictedProgram
                if (isPreviousStepperCompleted && model.getWorkoutIds() != null && !model.getWorkoutIds().isEmpty() && isFullyScheduled) {
                	if (isRestrictedProgram) {
                    	if (InstructorConstant.PUBLISH.equals(program.getStatus())) {
                            program.setStatus(DBConstants.PUBLISH);
                        }
                	}
                    if (!isRestrictedProgram) {
                    	if (InstructorConstant.PUBLISH.equals(program.getStatus())) {
                            program.setStatus(InstructorConstant.PUBLISH);
                        }else {
                        program.setStatus(InstructorConstant.PRICE);
                        }
                    } else {
                        program.setPostCompletionStatus(InstructorConstant.PRICE);
                    }
                    isStepperCompleted = true;
                }
            }
        }
        return isStepperCompleted;
    }

    /**
     * Validation to check if a flagged video in block state is added to the program
     *
     * @param workoutSchedules
     */
    private void validateFlaggedVideosInProgram(List<WorkoutSchedule> workoutSchedules) {
        List<Long> exerciseIdList = new ArrayList<>();
        for (WorkoutSchedule workoutSchedule : workoutSchedules) {
            if (workoutSchedule.getWorkout() != null) {
                for (CircuitSchedule circuitSchedule : workoutSchedule.getWorkout().getCircuitSchedules()) {
                    if (!circuitSchedule.isRestCircuit() && (circuitSchedule.getIsAudio() == null || !circuitSchedule.getIsAudio())) {
                        for (ExerciseSchedulers exerciseScheduler : circuitSchedule.getCircuit().getExerciseSchedules()) {
                            if (exerciseScheduler.getExercise() != null) {
                                exerciseIdList.add(exerciseScheduler.getExercise().getExerciseId());
                            }
                        }
                    }
                }
            }
        }
        exerciseIdList = exerciseIdList.stream().distinct().collect(Collectors.toList());
        boolean containsFlaggedVideo = false;
        if (!exerciseIdList.isEmpty()) {
            containsFlaggedVideo = flaggedExercisesSummaryRepository.existsByExerciseExerciseIdInAndFlagStatus(exerciseIdList, KeyConstants.KEY_BLOCK);
        }
        if (containsFlaggedVideo) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PROGRAM_CANT_ADD_FLAGGED_VIDEO, MessageConstants.ERROR);
        }
    }

    /**
     * construct Program Price phase data
     *
     * @param program
     * @param model
     * @return
     */
    private boolean constructProgramPrice(Programs program, ProgramModel model, boolean isRestrictedProgram, boolean isPreviousStepperCompleted) {
        boolean isStepperCompleted = false;
        if (InstructorConstant.PRICE.equals(program.getStatus()) || (isRestrictedProgram && InstructorConstant.PRICE.equals(program.getPostCompletionStatus())) || model.isSaveAsDraft()) {
            if (model.getProgramPrice() != null && model.getProgramPrice() != 0) {
                doConstructPriceModel(program, model);
                //Ignoring Status changes for RestrictedProgram
                if (isPreviousStepperCompleted) {
                	if (isRestrictedProgram) {
                        program.setStatus(InstructorConstant.PUBLISH);
                    } else {
                        program.setPostCompletionStatus(InstructorConstant.PUBLISH);
                    }
                    if (!isRestrictedProgram) {
                        program.setStatus(InstructorConstant.PRE_PUBLISH);
                    } else {
                        program.setPostCompletionStatus(InstructorConstant.PRE_PUBLISH);
                    }
                    isStepperCompleted = true;
                }
            }
            // Add Discount Offer           
            if(model.getDiscountOffersIds()!=null && !model.getDiscountOffersIds().isEmpty()) {
            	 doConstructDiscountsOffer(program, model);
            }
            if(program.getProgramPrices() != null){
                doCheckProgramPriceWithOffers(program);
            }
        }
        return isStepperCompleted;
    }

    public void doConstructDiscountsOffer(Programs program, ProgramModel model) {
        List<Long> discountOfferIds = model.getDiscountOffersIds();
        doConstructDiscountsOffer(program,discountOfferIds);
    }

    public void doConstructDiscountsOffer(Programs program, List<Long> discountOfferIds) {
        long profilingStart;
        long profilingEnd;
		List<DiscountOfferMapping> discountOfferlist = new ArrayList<>();
		int newProgramOfferMappingsForExistingUser = 0;
		int existingProgramOfferMappingsForExistingUsers = 0;
        int newProgramOfferMappingsForNewUser = 0;
        int existingProgramOfferMappingsForNewUser = 0;
		boolean isNewProgram = program.getProgramId() == null || program.getProgramId() == 0;
        Date now = new Date();
        profilingStart = new Date().getTime();
        for (Long offerId : discountOfferIds) {
			Optional<OfferCodeDetail> offerCodeDetail = offerCodeDetailRepository.findById(offerId);
			ValidationUtils.throwException(!offerCodeDetail.isPresent(), "Offer Code not found", Constants.BAD_REQUEST);
			OfferCodeDetail ofCodeDetail = offerCodeDetail.get();
			DiscountOfferMapping discountOfferMapping = discountOfferMappingRepository
					.findByProgramsProgramIdAndOfferCodeDetailOfferCodeId(program.getProgramId(),
							ofCodeDetail.getOfferCodeId());
			if (discountOfferMapping == null) {
			    //Checking if offer code is used in some other program
                DiscountOfferMapping offerMapping = discountOfferMappingRepository.findByOfferCodeDetailOfferCodeId(ofCodeDetail.getOfferCodeId());
                if (offerMapping != null) {
                    throw new ApplicationException(Constants.FORBIDDEN, ValidationMessageConstants.MSG_OFFER_CODE_ALREADY_MAPPED_TO_PROGRAM, null);
                }
                if(ofCodeDetail.getOfferStartingDate().before(now) && ofCodeDetail.getOfferEndingDate().before(now)) {
                    throw new ApplicationException(Constants.BAD_REQUEST, "Expired Offers can not be added", null);
                }
                if(ofCodeDetail.getIsNewUser()){
                    List<DiscountOfferMapping> discountOfferMappings = discountOfferMappingRepository.findByProgramsProgramIdAndOfferCodeDetailIsNewUserAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus
                            (program.getProgramId(),true,true,DiscountsConstants.OFFER_ACTIVE);
                    for(DiscountOfferMapping discountOffer : discountOfferMappings){
                        //Cannot add two current offers for new user
                        if(discountOffer.getOfferCodeDetail().getOfferStartingDate().after(now) && ofCodeDetail.getOfferStartingDate().after(now)){
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_UPCOMING_OFFER_NEW_USERS, null);
                        }
                        //Cannot add two current offers for new user
                        if(discountOffer.getOfferCodeDetail().getOfferStartingDate().before(now) && ofCodeDetail.getOfferStartingDate().before(now)
                                && discountOffer.getOfferCodeDetail().getOfferEndingDate().after(now) && ofCodeDetail.getOfferEndingDate().after(now)){
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_CURRENT_OFFER_NEW_USERS, null);
                        }
                        //Adding upcoming offer first and then adding current
                        if((ofCodeDetail.getOfferStartingDate().before(discountOffer.getOfferCodeDetail().getOfferStartingDate()) && !fitwiseUtils.isSameDay(ofCodeDetail.getOfferEndingDate(),discountOffer.getOfferCodeDetail().getOfferStartingDate()) &&ofCodeDetail.getOfferEndingDate().after(discountOffer.getOfferCodeDetail().getOfferStartingDate()))) {
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_END_DATE_NEW_USERS, null);
                        }

                        //Adding current offer first and then adding upcoming
                        if(ofCodeDetail.getOfferStartingDate().after(discountOffer.getOfferCodeDetail().getOfferStartingDate()) && !fitwiseUtils.isSameDay(ofCodeDetail.getOfferStartingDate(),discountOffer.getOfferCodeDetail().getOfferEndingDate()) && ofCodeDetail.getOfferStartingDate().before(discountOffer.getOfferCodeDetail().getOfferEndingDate())){
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_START_DATE_NEW_USERS, null);
                        }
                    }
                }
				discountOfferMapping = new DiscountOfferMapping();
				//New Discount-iTMS upload needed
				discountOfferMapping.setNeedDiscountUpdate(true);
				discountOfferMapping.setDiscountStatus(DiscountsConstants.NEW_DISCOUNT);
				//Mail
				discountOfferMapping.setNeedMailUpdate(true);
                if(ofCodeDetail.getOfferPrice() != null && ofCodeDetail.getOfferPrice().getPrice() >= program.getProgramPrices().getPrice()){
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_OFFER_PRICE_GREATER_THAN_PROGRAM_PRICE, null);
                }
                //Creating coupon on Stripe for the offer code
                long start = new Date().getTime();
                stripeService.createCoupon(ofCodeDetail, program.getProgramPrices().getPrice());
                long end = new Date().getTime();
                log.info("Create stripe coupon for a program : Time taken in millis : "+(end-start));
                if(ofCodeDetail.getIsNewUser()){
				    newProgramOfferMappingsForNewUser++;
                }else{
                    newProgramOfferMappingsForExistingUser++;
                }
			}
			discountOfferMapping.setPrograms(program);
			DiscountLevel levelMapping = discountLevelRepository.findByDiscountLevelName(DiscountsConstants.PROGRAM_LEVEL);
			discountOfferMapping.setLevelMapping(levelMapping);
			discountOfferMapping.setOfferCodeDetail(ofCodeDetail);	
			discountOfferlist.add(discountOfferMapping);
		}
        profilingEnd = new Date().getTime();
        log.info("Offers validation and saving : Time taken in millis : "+(profilingEnd-profilingStart));
        profilingStart = new Date().getTime();
        if(!isNewProgram){
            List<DiscountOfferMapping> discountOfferMappings = discountOfferMappingRepository.findByProgramsProgramIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus
                    (program.getProgramId(),true,DiscountsConstants.OFFER_ACTIVE);
            for(DiscountOfferMapping discountOfferMapping : discountOfferMappings){
                if (!discountOfferMapping.getOfferCodeDetail().getOfferEndingDate().before(now)) {
                    if(discountOfferMapping.getOfferCodeDetail().getIsNewUser()){
                        existingProgramOfferMappingsForNewUser++;
                    }else{
                        existingProgramOfferMappingsForExistingUsers++;
                    }
                }
            }
        }
        profilingEnd = new Date().getTime();
        log.info("Getting active new and existing offers count : Time taken in millis : "+(profilingEnd-profilingStart));
        //Apple Validation : You can have up to 10 active promotional offers within the <promotional_offers> block.
        if(newProgramOfferMappingsForExistingUser + existingProgramOfferMappingsForExistingUsers > KeyConstants.KEY_MAX_OFFER_COUNT_EXISTING_USERS) {
			 throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_REACHED_MAX_COUNT_EXISTING_USERS, null);
		}
        if(newProgramOfferMappingsForNewUser + existingProgramOfferMappingsForNewUser > KeyConstants.KEY_MAX_OFFER_COUNT_NEW_USERS) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_OFFER_REACHED_MAX_COUNT_NEW_USERS, null);
        }
		program.setProgramDiscountMapping(discountOfferlist);
	}

	/**
     * Do set program duration.
     *
     * @param duration the duration
     * @param program  the program
     * @throws ApplicationException the application exception
     */
    private void doSetProgramDuration(Long duration, Programs program) {
        Duration durationObj = durationRepo.findByDurationId(duration);
        ValidationUtils.throwException(durationObj == null, "No duration exist with this duration", Constants.BAD_REQUEST);
        program.setDuration(durationObj);
    }

    /**
     * Do construct promo video.
     *
     * @param program the program
     * @param model   the model
     * @throws ApplicationException the application exception
     */
    private void doConstructPromoVideo(Programs program, ProgramModel model) {
        Promotions promotions = promotionRepository.findByPromotionId(model.getPromotionId());
        if (promotions == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_NOT_FOUND, null);
        }
        program.setPromotion(promotions);
    }

    /**
     * Do construct price model.
     *
     * @param program the program
     * @param model   the model
     * @throws ApplicationException the application exception
     */
    private void doConstructPriceModel(Programs program, ProgramModel model) {
        if (model.getProgramPrice() < 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_PRICE_INVALID, null);
        }
        Set<ProgramPriceByPlatform> programPriceByPlatforms = new HashSet<>();
        if (program.getProgramId() != null && program.getProgramId() != 0) {
            programPriceByPlatforms = programPriceByPlatformRepository.findByProgram(program);
        }
        if (!programPriceByPlatforms.isEmpty() && model.getProgramPrice().doubleValue() != program.getProgramPrice().doubleValue()) {
            programPriceByPlatformRepository.deleteByProgram(program);
            programPriceByPlatforms = new HashSet<>();
        }
        if (programPriceByPlatforms.isEmpty()) {
            for (ProgramPlatformPriceModel programPlatformPriceModel : model.getProgramPlatformPriceModels()) {
                PlatformWiseTaxDetail platformWiseTaxDetail = platformWiseTaxDetailRepository.findByPlatformWiseTaxDetailId(programPlatformPriceModel.getPlatformWiseTaxId());
                if (platformWiseTaxDetail == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PRICE_MODEL_NOT_FOUND, null);
                }
                ProgramPriceByPlatform programPriceByPlatform = new ProgramPriceByPlatform();
                programPriceByPlatform.setPlatformWiseTaxDetail(platformWiseTaxDetail);
                programPriceByPlatform.setPrice(programPlatformPriceModel.getPrice());
                programPriceByPlatform.setProgram(program);
                programPriceByPlatforms.add(programPriceByPlatform);
            }
        }
        program.setProgramPriceByPlatforms(programPriceByPlatforms);
        program.setProgramPrice(model.getProgramPrice());
        ProgramPrices programPrices = programPricesRepository.findByPrice(model.getProgramPrice());
        program.setProgramPrices(programPrices);
    }

    /**
     * Do construct schedule model.
     *
     * @param program the program
     * @param model   the model
     * @throws ApplicationException the application exception
     */
    private boolean doConstructScheduleModel(Programs program, ProgramModel model) {
        boolean isFullyScheduled = true;
        List<ScheduleModel> scheduleList = model.getWorkoutSchedules();
        List<WorkoutSchedule> programSchedules = new ArrayList<>();
        Set<Long> newScheduleOrderSet = new HashSet<>();
        int restCount = 0;
        Set<Long> uniqueWorkoutId = new HashSet<>();
        for (ScheduleModel schedule : scheduleList) {
            WorkoutSchedule newSchedule;
            if (schedule.getScheduleId() == null || schedule.getScheduleId() == 0) {
                newSchedule = new WorkoutSchedule();
            } else {
                newSchedule = workoutScheduleRepository.findByWorkoutScheduleId(schedule.getScheduleId());
            }
            //Workout Order validation
            if (schedule.getOrder() == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SCHEDULE_ORDER_MISSING, MessageConstants.ERROR);
            }
            if (schedule.getOrder().intValue() <= 0) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SCHEDULE_ORDER_CANT_BE_ZERO, MessageConstants.ERROR);
            }
            int oldUniqueScheduleOrderSize = newScheduleOrderSet.size();
            Long newScheduleOrder = schedule.getOrder();
            newScheduleOrderSet.add(newScheduleOrder);
            int newUniqueScheduleOrderSize = newScheduleOrderSet.size();
            if (oldUniqueScheduleOrderSize == newUniqueScheduleOrderSize || newScheduleOrder > scheduleList.size()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_WKT_ORDER, MessageConstants.ERROR);
            }
            newSchedule.setOrder(newScheduleOrder);
            newSchedule.setPrograms(program);
            newSchedule.setRestDay(schedule.isRestDay());
            if (!schedule.isRestDay()) {
                //if workout is not scheduled, it is not saved in program., it is skipped.
                if (model.isSaveAsDraft() && schedule.getWorkoutId() == null) {
                    isFullyScheduled = false;
                    continue;
                }
                newSchedule.setTitle(schedule.getTitle());
                ValidationUtils.throwException(!model.getWorkoutIds().contains(schedule.getWorkoutId()), ValidationMessageConstants.MSG_CANT_ADD_WORKOUT_TO_SCHEDULE, Constants.BAD_REQUEST);
                Optional<Workouts> workout = workoutRepo.findById(schedule.getWorkoutId());
                ValidationUtils.throwException(!workout.isPresent(), ValidationMessageConstants.MSG_WORKOUT_NOT_FOUND, Constants.BAD_REQUEST);
                newSchedule.setWorkout(workout.get());
                uniqueWorkoutId.add(schedule.getWorkoutId());
            } else {
                //Workout Schedule activity - saving
                RestActivityScheduleModel restActivityModel = schedule.getRestActivityModel();
                if (restActivityModel == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_REST_ACTIVITY_DATA_MISSING, null);
                }
                //Validation for rest activity
                Long activityId = restActivityModel.getActivityId();
                Optional<RestActivity> restActivityOptional = restActivityRepository.findById(activityId);
                if (!restActivityOptional.isPresent()) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_REST_ACTIVITY_NOT_FOUND, null);
                }
                RestActivity restActivity = restActivityOptional.get();
                String activityName = restActivity.getRestActivity();

                RestActivityToMetricMapping mapping;
                if (DBConstants.REST.equalsIgnoreCase(restActivity.getRestActivity())) {
                    mapping = restActivityToMetricMappingRepository.findByRestActivityRestActivityIdAndRestMetricRestMetricId(activityId, null);
                } else {
                    mapping = restActivityToMetricMappingRepository.findByRestActivityRestActivityIdAndRestMetricRestMetricId(activityId, restActivityModel.getMetricId());
                    if (mapping == null) {
                        throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_REST_ACTIVITY_METRIC_INCORRECT, null);
                    }
                    if (restActivityModel.getValue() > mapping.getMaximumValue()) {
                        throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_REST_ACTIVITY_VALUE_EXCEED_LIMIT, null);
                    }
                    if (DBConstants.OTHERS.equalsIgnoreCase(restActivity.getRestActivity())) {
                        activityName = restActivityModel.getActivityName();
                    }
                }
                //construction InstructorRestActivity entity
                InstructorRestActivity instructorRestActivity = new InstructorRestActivity();
                instructorRestActivity.setRestActivityToMetricMapping(mapping);
                instructorRestActivity.setActivityName(activityName);
                instructorRestActivity.setValue(restActivityModel.getValue());
                instructorRestActivity.setNotes(restActivityModel.getNotes());

                newSchedule.setInstructorRestActivity(instructorRestActivity);
                restCount++;
            }
            programSchedules.add(newSchedule);
        }

        //Workout schedule validation
        if (scheduleList.size() == restCount) {
            if (!model.isSaveAsDraft()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PROGRAM_CANT_HAVE_ALL_REST, null);
            } else {
                isFullyScheduled = false;
            }
        }
        if (uniqueWorkoutId.size() != model.getWorkoutIds().size()) {
            if (!model.isSaveAsDraft()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ALL_WORKOUTS_NOT_SCHEDULED, null);
            } else {
                isFullyScheduled = false;
            }
        }

        regulateRestInWorkoutSchedule(programSchedules);

        program.setWorkoutSchedules(programSchedules);

        return isFullyScheduled;
    }

    /**
     * Method to validate/ensure trial workouts schedules not set as Rest/Rest activity
     *
     * @param workoutSchedulesList
     */
    private void regulateRestInWorkoutSchedule(List<WorkoutSchedule> workoutSchedulesList) {
        for (WorkoutSchedule workoutSchedule : workoutSchedulesList) {
            int trialWorkoutScheduleCount = fitwiseUtils.getDefaultTrialWorkoutsCount();
            int order = workoutSchedule.getOrder().intValue();
            if (order <= trialWorkoutScheduleCount && workoutSchedule.isRestDay()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_TRIAL_WORKOUTS_CANT_BE_REST, MessageConstants.ERROR);
            }
        }
    }

    /**
     * Do construct workout list.
     *
     * @param program the program
     * @param model   the model
     * @throws ApplicationException the application exception
     */
    private void doConstructWorkoutList(Programs program, ProgramModel model) {
        List<WorkoutMapping> workoutMappingList = new ArrayList<>();
        Set<Long> workoutIdSet = new HashSet<>();
        for (Long workoutId : model.getWorkoutIds()) {
            Optional<Workouts> workoutsOptional = workoutRepo.findById(workoutId);
            ValidationUtils.throwException(!workoutsOptional.isPresent(), ValidationMessageConstants.MSG_WORKOUT_NOT_FOUND, Constants.BAD_REQUEST);
            Workouts workout = workoutsOptional.get();
            workoutService.validateFlaggedVideosInWorkout(workout.getCircuitSchedules(), false);
            WorkoutMapping mapping = workoutMappingRepository.findByProgramsProgramIdAndWorkoutWorkoutId(program.getProgramId(), workout.getWorkoutId());
            if (mapping == null) {
                mapping = new WorkoutMapping();
            }
            mapping.setWorkout(workout);
            mapping.setPrograms(program);
            workoutMappingList.add(mapping);
            //Validation for duplicate workout in Program
            int oldUniqueWorkoutIdCount = workoutIdSet.size();
            workoutIdSet.add(workout.getWorkoutId());
            int newUniqueWorkoutIdCount = workoutIdSet.size();
            if (oldUniqueWorkoutIdCount == newUniqueWorkoutIdCount) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_DUPLICATE_WORKOUT_IN_PROGRAM, MessageConstants.ERROR);
            }
        }
        program.setProgramMapping(workoutMappingList);
    }

    /**
     * Do construct thumbnail.
     *
     * @param program the program
     * @param model   the model
     * @throws ApplicationException the application exception
     */
    private void doConstructThumbnail(Programs program, ProgramModel model) {
        Images thumbnail = imgRepo.getOne(model.getImageId());
        ValidationUtils.throwException(thumbnail == null, "No image found in server with this ID", Constants.BAD_REQUEST);
        program.setImage(thumbnail);
    }

    /**
     * Do construct program expertise.
     *
     * @param program the program
     * @param model   the model
     * @throws ApplicationException the application exception
     */
    private void doConstructProgramExpertise(Programs program, ProgramModel model) {
        Optional<ExpertiseLevels> exLevel = expRepo.findById(model.getExpertiseId());
        ValidationUtils.throwException(!exLevel.isPresent(), "Invalid expertise leve", Constants.BAD_REQUEST);
        program.setProgramExpertiseLevel(exLevel.get());
    }

    /**
     * Do construct program goals.
     *
     * @param program the program
     * @param model   the model
     * @throws ApplicationException the application exception
     */
    private void doConstructProgramGoals(Programs program, ProgramModel model) {
        Optional<ProgramTypes> type = programTypeRepo.findById(model.getProgramTypeId());
        ValidationUtils.throwException(!type.isPresent(), "Invalid Program Type ", Constants.BAD_REQUEST);
        ProgramTypes pType = type.get();
        program.setProgramType(pType);
        if (program.getProgramId() != null && program.getProgramId() > 0) {
            programWiseGoalRepository.deleteByProgram(program);
        }
        List<ProgramWiseGoal> programWiseGoals = new ArrayList<>();
        for (Long programExpertiseGoalMappingId : model.getProgramTypeLevelGoalMappingIds()) {
            ProgramExpertiseGoalsMapping programExpertiseGoalsMapping = programExpertiseGoalsMappingRepository.findByProgramExpertiseGoalsMappingId(programExpertiseGoalMappingId);
            if (programExpertiseGoalsMapping != null) {
                ProgramWiseGoal programWiseGoal = new ProgramWiseGoal();
                programWiseGoal.setProgram(program);
                programWiseGoal.setProgramExpertiseGoalsMapping(programExpertiseGoalsMapping);
                programWiseGoals.add(programWiseGoal);
            }
        }
        if (!programWiseGoals.isEmpty()) {
            program.setProgramWiseGoals(programWiseGoals);
        }
    }

    /**
     * Do construct program types.
     *
     * @param program the program
     * @param model   the model
     * @throws ApplicationException the application exception
     */
    private void doConstructProgramTypes(Programs program, ProgramModel model) {
        Optional<ProgramTypes> programTypeToStore = programTypeRepo.findById(model.getProgramTypeId());
        ValidationUtils.throwException(!programTypeToStore.isPresent(), "Invalid Program Types", Constants.BAD_REQUEST);
        program.setProgramType(programTypeToStore.get());
    }

    /**
     * Do validate role as instructor.
     *
     * @param user the user
     * @throws ApplicationException the application exception
     */
    private void doValidateRoleAsInstructor(User user) {
        Set<UserRole> roles = AppUtils.getUserRoles(user); //AKHIL
        boolean isInstructor = false;
        for (UserRole role : roles) {
            if (role.getName().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)) {
                isInstructor = true;
                break;
            }
        }
        if (!isInstructor) {
            throw new ApplicationException(Constants.BAD_REQUEST, "Invalid role", Constants.RESPONSE_FAILURE);
        }
    }

    /**
     * Construct program model.
     *
     * @param program the program
     * @return the program response model
     */
    public ProgramResponseModel constructProgramModel(Programs program) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.info("Program response construction starts");
        long methodStartTimeMillis = new Date().getTime();
        long profilingStartTimeMillis = new Date().getTime();
        ProgramResponseModel responseModel = new ProgramResponseModel();
        List<Equipments> equipments = new ArrayList<>();
        responseModel.setProgramId(program.getProgramId());
        responseModel.setTitle(program.getTitle());
        responseModel.setDescription(program.getDescription());
        responseModel.setShortDescription(program.getShortDescription());
        if (program.getOwner() != null) {
            UserProfile userProfile = userProfileRepository.findByUserUserId(program.getOwner().getUserId());
            responseModel.setFirstName(userProfile.getFirstName());
            responseModel.setLastName(userProfile.getLastName());
            if (userProfile.getProfileImage() != null) {
                responseModel.setInstructorProfileUrl(userProfile.getProfileImage().getImagePath());
            }
        }
        if (program.getDuration() != null)
            responseModel.setDuration(program.getDuration());
		if (program.getProgramType() != null) {
			ProgramTypeWithSubTypeView viewObj = new ProgramTypeWithSubTypeView();
			viewObj.setProgramTypeId(program.getProgramType().getProgramTypeId());
			viewObj.setProgramTypeName(program.getProgramType().getProgramTypeName());
			viewObj.setIconUrl(program.getProgramType().getIconUrl());
			if (program.getProgramSubType() != null) {
				viewObj.setProgramTypeName(program.getProgramType().getProgramTypeName() + "--" + program.getProgramSubType().getName());
				viewObj.setProgramSubType(program.getProgramSubType());
			}
			responseModel.setProgramType(viewObj);
		}
        if (program.getProgramExpertiseLevel() != null)
            responseModel.setProgramExpertise(program.getProgramExpertiseLevel());
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Basic details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = new Date().getTime();
        // Setting promoVideo Id
        if (program.getPromotion() != null && program.getPromotion().getVideoManagement() != null && program.getPromotion().getVideoManagement().getUrl() != null) {
            responseModel.setPromoVideoId(program.getPromotion().getVideoManagement().getUrl());
            responseModel.setPromotionId(program.getPromotion().getPromotionId());
            String vimeoUrl = program.getPromotion().getVideoManagement().getUrl();
            String vimeoId = "";
            if (vimeoUrl.contains("/")) {
                String[] videoIds = vimeoUrl.split("/");
                vimeoId = videoIds[2];
            }
            if (!vimeoId.isEmpty()) {
                responseModel.setPromoVideoParsedUrl(vimeoService.getVimeoUrlFromId(Long.parseLong(vimeoId)));
            }
            if (program.getPromotion().getVideoManagement().getThumbnail() != null) {
                responseModel.setPromotionThumbnailImageId(program.getPromotion().getVideoManagement().getThumbnail().getImageId());
                responseModel.setPromotionThumbnailImageUrl(program.getPromotion().getVideoManagement().getThumbnail().getImagePath());
            }
            responseModel.setPromotionDuration(program.getPromotion().getVideoManagement().getDuration());
            responseModel.setPromotionUploadStatus(program.getPromotion().getVideoManagement().getUploadStatus());
            /*
             * If the video processing was failed first time, marking it as upload status
             * If the video processing was failed more than one time, marking it as re-upload status
             */
            if (program.getPromotion().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_PROCESSING_FAILED)) {
                responseModel.setPromotionUploadStatus(VideoUploadStatus.UPLOAD);
            } else if (program.getPromotion().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED)) {
                responseModel.setPromotionUploadStatus(VideoUploadStatus.REUPLOAD);
            }
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Promotion details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = new Date().getTime();
        if (program.getProgramPrice() != null) {
            InstructorTierDetails instructorTierDetails = instructorTierDetailsRepository.findByUserAndActive(program.getOwner(), true);
            Double trainnrTax = 15.0;
            if (instructorTierDetails == null) {
                trainnrTax = instructorTierDetails.getTier().getTierTypeDetails().getProgramsFees();
            }
            DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
            responseModel.setProgramPrice(decimalFormat.format(program.getProgramPrice()));
            responseModel.setFormattedProgramPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(program.getProgramPrice()));
            List<ProgramPlatformPriceResponseModel> programPlatformPriceModels = new ArrayList<>();
            
            for (ProgramPriceByPlatform programPriceByPlatform : program.getProgramPriceByPlatforms()) {
                ProgramPlatformPriceResponseModel programPlatformPriceResponseModel = new ProgramPlatformPriceResponseModel();
                programPlatformPriceResponseModel.setProgramPriceByPlatformId(programPriceByPlatform.getProgramPriceByPlatformId());
                programPlatformPriceResponseModel.setPlatformWiseTaxDetailId(programPriceByPlatform.getPlatformWiseTaxDetail().getPlatformWiseTaxDetailId());
                programPlatformPriceResponseModel.setPlatform(programPriceByPlatform.getPlatformWiseTaxDetail().getPlatformType().getPlatform());
                programPlatformPriceResponseModel.setPlatformId(programPriceByPlatform.getPlatformWiseTaxDetail().getPlatformType().getPlatformTypeId());
                programPlatformPriceResponseModel.setPrice(programPriceByPlatform.getPrice());
                programPlatformPriceResponseModel.setAppStoreTax(programPriceByPlatform.getPlatformWiseTaxDetail().getAppStoreTaxPercentage());
                programPlatformPriceResponseModel.setTrainnrTax(trainnrTax);
                programPlatformPriceResponseModel.setGeneralTax(programPriceByPlatform.getPlatformWiseTaxDetail().getGeneralTaxPercentage());
                AppConfigKeyValue appConfig = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX);
                if (appConfig != null) {
    	        	double flatTax = Double.parseDouble(appConfig.getValueString());
    	        	programPlatformPriceResponseModel.setFlatTax(flatTax);
    	        }
                programPlatformPriceModels.add(programPlatformPriceResponseModel);
            }
            responseModel.setProgramPlatformPriceResponseModels(programPlatformPriceModels);
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Pricing details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = new Date().getTime();
        if (program.getDuration() != null) {
            responseModel.setDuration(program.getDuration());
        }
        responseModel.setCreatedDate(program.getCreatedDate());
        responseModel.setCreatedDateFormatted(fitwiseUtils.formatDate(program.getCreatedDate()));
        if (program.getModifiedDate() != null)
            responseModel.setModifiedDate(program.getModifiedDate());
        responseModel.setModifiedDateFormatted(fitwiseUtils.formatDate(program.getModifiedDate()));
        responseModel.setPublish(program.isPublish());
        responseModel.setFlag(program.isFlag());
        responseModel.setStatus(program.getStatus());
        if (program.getPostCompletionStatus() != null) {
            responseModel.setPostCompletionStatus(program.getPostCompletionStatus());
        }
        if (program.getImage() != null) {
            responseModel.setImageId(program.getImage().getImageId());
            responseModel.setThumbnailUrl(program.getImage().getImagePath());
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Date and status details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = new Date().getTime();
        if (program.getProgramType() != null) {
            List<ProgramWiseGoal> programWiseGoals = program.getProgramWiseGoals();
            List<ProgramGoalsView> programGoalsViews = new ArrayList<>();
            for (ProgramWiseGoal programWiseGoal : programWiseGoals) {
                ProgramGoalsView programGoalsView = new ProgramGoalsView();
                programGoalsView.setProgramGoalId(programWiseGoal.getProgramExpertiseGoalsMapping().getProgramGoals().getProgramGoalId());
                programGoalsView.setProgramGoal(programWiseGoal.getProgramExpertiseGoalsMapping().getProgramGoals().getProgramGoal());
                programGoalsView.setProgramTypeLevelGoalMappingId(programWiseGoal.getProgramExpertiseGoalsMapping().getProgramExpertiseGoalsMappingId());
                programGoalsViews.add(programGoalsView);
            }
            responseModel.setGoalsList(programGoalsViews);
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Goal details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = new Date().getTime();
        List<SubscriptionPackagePackageIdAndTitleView> associatedPackages = new ArrayList<>();
        List<PackageProgramMapping> packageProgramMappingList = packageProgramMappingRepository.findByProgram(program);
        if(!packageProgramMappingList.isEmpty()){
            for(PackageProgramMapping packageProgramMapping : packageProgramMappingList){
                SubscriptionPackagePackageIdAndTitleView associatedPackageView = new SubscriptionPackagePackageIdAndTitleView();
                associatedPackageView.setSubscriptionPackageId(packageProgramMapping.getSubscriptionPackage().getSubscriptionPackageId());
                associatedPackageView.setTitle(packageProgramMapping.getSubscriptionPackage().getTitle());
                associatedPackages.add(associatedPackageView);
            }
        }
        responseModel.setAssociatedPackages(associatedPackages);
        profilingEndTimeMillis = new Date().getTime();
        log.info("Package details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = new Date().getTime();
        boolean isMaximumCountReachedForNewUsers = false;
        boolean isMaximumCountReachedForExistingUsers = false;
        if(program.getProgramDiscountMapping()!=null && !program.getProgramDiscountMapping().isEmpty()) {
            int newUserOffers = 0;
            int existingUserOffers = 0;
            List<ProgramDiscountMappingResponseView> currentDiscounts = new ArrayList<>();
            List<ProgramDiscountMappingResponseView> freeOffers = new ArrayList<>();
            List<ProgramDiscountMappingResponseView> paidOffers = new ArrayList<>();
            List<ProgramDiscountMappingResponseView> upcomingDiscounts = new ArrayList<>();
            List<ProgramDiscountMappingResponseView> expiredDiscounts = new ArrayList<>();
            for (DiscountOfferMapping disOffer : program.getProgramDiscountMapping()) {
                    ProgramDiscountMappingResponseView sResponseView = new ProgramDiscountMappingResponseView();
                    sResponseView.setOfferMappingId(disOffer.getOfferMappingId());
                    sResponseView.setOfferCodeId(disOffer.getOfferCodeDetail().getOfferCodeId());
                    sResponseView.setOfferName(disOffer.getOfferCodeDetail().getOfferName().trim());
                    sResponseView.setOfferCode(disOffer.getOfferCodeDetail().getOfferCode().toUpperCase());
                    sResponseView.setOfferMode(disOffer.getOfferCodeDetail().getOfferMode());
                    sResponseView.setOfferDuration(disOffer.getOfferCodeDetail().getOfferDuration());
                    sResponseView.setOfferStartDate(fitwiseUtils.formatDate(disOffer.getOfferCodeDetail().getOfferStartingDate()));
                    sResponseView.setOfferEndDate(fitwiseUtils.formatDate(disOffer.getOfferCodeDetail().getOfferEndingDate()));
                    sResponseView.setOfferPrice(disOffer.getOfferCodeDetail().getOfferPrice());
                    String formattedPrice;
                    if (disOffer.getOfferCodeDetail().getOfferPrice() != null) {
                        formattedPrice = fitwiseUtils.formatPrice(disOffer.getOfferCodeDetail().getOfferPrice().getPrice());
                    } else {
                        formattedPrice = KeyConstants.KEY_CURRENCY_US_DOLLAR+KeyConstants.KEY_DEFAULT_PRICE_FORMAT;
                    }
                sResponseView.setFormattedOfferPrice(formattedPrice);
                if (program.getProgramPrices() != null) {
                        double savingsAmount = program.getProgramPrices().getPrice();
                        if (disOffer.getOfferCodeDetail().getOfferPrice() != null) {
                            savingsAmount = program.getProgramPrices().getPrice() - disOffer.getOfferCodeDetail().getOfferPrice().getPrice();
                        }
                        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
                        sResponseView.setFormattedSavingsAmount(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(savingsAmount));
                    }
                    sResponseView.setOfferStatus(disOffer.getOfferCodeDetail().getOfferStatus());
                    sResponseView.setIsNewUser(disOffer.getOfferCodeDetail().getIsNewUser());
                    //Offer Validity check
                    Date now = new Date();
                    Date offerStart = disOffer.getOfferCodeDetail().getOfferStartingDate();
                    Date offerEnd = disOffer.getOfferCodeDetail().getOfferEndingDate();
                    if (disOffer.getOfferCodeDetail().getOfferStatus().equalsIgnoreCase(DiscountsConstants.OFFER_ACTIVE) && disOffer.getOfferCodeDetail().isInUse()) {
                    	// Current Offers
                        if ((offerStart.equals(now) || offerStart.before(now)) && (offerEnd.equals(now) || offerEnd.after(now))) {
                            if(disOffer.getOfferCodeDetail().getIsNewUser()){
                                newUserOffers++;
                            }else {
                                existingUserOffers++;
                            }
                            if (disOffer.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)) {
    							freeOffers.add(sResponseView);
    						} else if (disOffer.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_PAY_AS_YOU_GO)) {
    							paidOffers.add(sResponseView);
    						}
                        }
                        // UpComing Offers
                        else if (offerStart.after(now) && offerEnd.after(now)) {
                            if(disOffer.getOfferCodeDetail().getIsNewUser()){
                                newUserOffers++;
                            }else {
                                existingUserOffers++;
                            }
                            upcomingDiscounts.add(sResponseView);
                        }
                    }else {
                    	// Expired Offers
                         if (offerStart.before(now) && offerEnd.before(now)) {
                            expiredDiscounts.add(sResponseView);
                        }
                    }
            }
            ProgramDiscountMappingListResponseView discountOffers = new ProgramDiscountMappingListResponseView();
            paidOffers.sort((ProgramDiscountMappingResponseView f1,ProgramDiscountMappingResponseView f2) -> f1.compareTo(f2)); 
            freeOffers.sort((ProgramDiscountMappingResponseView f1,ProgramDiscountMappingResponseView f2) -> f1.compareTo(f2));
            Collections.sort(freeOffers, Collections.reverseOrder());
            freeOffers.stream().collect(Collectors.toCollection(()->currentDiscounts));
            paidOffers.stream().collect(Collectors.toCollection(()->currentDiscounts));
            discountOffers.setCurrentDiscounts(currentDiscounts);            
            discountOffers.setUpcomingDiscounts(upcomingDiscounts);
            discountOffers.setExpiredDiscounts(expiredDiscounts);
            if(newUserOffers >= KeyConstants.KEY_MAX_OFFER_COUNT_NEW_USERS ){
                isMaximumCountReachedForNewUsers = true;
            }
            if(existingUserOffers >= KeyConstants.KEY_MAX_OFFER_COUNT_EXISTING_USERS){
                isMaximumCountReachedForExistingUsers = true;
            }
            responseModel.setDiscountOffers(discountOffers);
            responseModel.setMaximumCountReachedForNewUsers(isMaximumCountReachedForNewUsers);
            responseModel.setMaximumCountReachedForExistingUsers(isMaximumCountReachedForExistingUsers);
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Discount details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = new Date().getTime();
        if (program.getProgramMapping() != null && !program.getProgramMapping().isEmpty()) {
            List<WorkoutResponseModel> workoutResponseModelList = new ArrayList<>();
            for (WorkoutMapping mapping : program.getProgramMapping()) {
                WorkoutResponseModel workout = new WorkoutResponseModel();
                workout.setWorkoutId(mapping.getWorkout().getWorkoutId());
                workout.setTitle(mapping.getWorkout().getTitle());
                workout.setDescription(mapping.getWorkout().getDescription());
                if (mapping.getWorkout().getImage() != null) {
                    workout.setImageId(mapping.getWorkout().getImage().getImageId());
                    workout.setThumbnailUrl(mapping.getWorkout().getImage().getImagePath());
                }
                Set<CircuitSchedule> circuitSchedules = mapping.getWorkout().getCircuitSchedules();
                int totalExercises = 0;
                int workoutDuration = 0;
                int circuitCount = 0;
                boolean isVideoProcessingPending = false;
                List<Long> exerciseIdList = new ArrayList<>();
                for (CircuitSchedule circuitSchedule : circuitSchedules) {
                    long circuitDuration = 0;
                    boolean isRestCircuit = circuitSchedule.isRestCircuit();
                    if (isRestCircuit) {
                        circuitDuration = circuitSchedule.getRestDuration();
                    } else if(circuitSchedule.getCircuit() != null && (circuitSchedule.getIsAudio() == null || !circuitSchedule.getIsAudio())) {
                        circuitCount++;
                        Set<ExerciseSchedulers> exerciseSchedulers = circuitSchedule.getCircuit().getExerciseSchedules();
                        Set<Long> newScheduleOrderSet = new HashSet<>();

                        for (ExerciseSchedulers exerciseScheduler : exerciseSchedulers) {
                            long exerciseDuration = 0;
                            if (exerciseScheduler.getExercise() != null) {
                                if (exerciseScheduler.getExercise().getVideoManagement() != null) {
                                    exerciseDuration = exerciseScheduler.getExercise().getVideoManagement().getDuration();

                                    if (exerciseScheduler.getLoopCount() != null && exerciseScheduler.getLoopCount() > 0) {
                                        //Repeat Count Change : Since repeat count is changes as no of times video should play
                                        exerciseDuration = exerciseDuration * exerciseScheduler.getLoopCount();
                                    }
                                    if (fitwiseUtils.isVideoProcessingPending(exerciseScheduler.getExercise().getVideoManagement())) {
                                        isVideoProcessingPending = true;
                                    }
                                }
                                if (exerciseScheduler.getExercise().getEquipments() != null) {
                                    equipments.addAll(exerciseScheduler.getExercise().getEquipments());
                                }
                                exerciseIdList.add(exerciseScheduler.getExercise().getExerciseId());

                                totalExercises++;

                                //Exercise Order validation
                                int oldUniqueScheduleOrderSize = newScheduleOrderSet.size();
                                Long newScheduleOrder = exerciseScheduler.getOrder();
                                newScheduleOrderSet.add(newScheduleOrder);
                                int newUniqueScheduleOrderSize = newScheduleOrderSet.size();
                                if (oldUniqueScheduleOrderSize == newUniqueScheduleOrderSize || exerciseScheduler.getOrder() > exerciseSchedulers.size()) {
                                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_SCHEDULE_ORDER_INVALID, MessageConstants.ERROR);
                                }
                            } else if(exerciseScheduler.getWorkoutRestVideo() != null) {
                                exerciseDuration = exerciseScheduler.getWorkoutRestVideo().getRestTime();
                            }else if(exerciseScheduler.getVoiceOver() != null){
                                exerciseDuration = exerciseScheduler.getVoiceOver().getAudios().getDuration();
                            }
                            circuitDuration += exerciseDuration;
                        }
                        Long repeat = circuitSchedule.getRepeat();
                        Long restBetweenRepeat = circuitSchedule.getRestBetweenRepeat();
                        if (repeat != null && repeat > 0) {
                            circuitDuration = circuitDuration * repeat;

                            long repeatRestDuration = 0;
                            if (restBetweenRepeat != null && restBetweenRepeat > 0) {
                                repeatRestDuration = restBetweenRepeat * (repeat-1);
                            }
                            circuitDuration = circuitDuration  + repeatRestDuration;
                        }
                    }else if(circuitSchedule.getIsAudio() != null && circuitSchedule.getIsAudio()){
                        for(CircuitAndVoiceOverMapping circuitAndVoiceOverMapping : circuitSchedule.getCircuit().getCircuitAndVoiceOverMappings()){
                            circuitDuration += circuitAndVoiceOverMapping.getVoiceOver().getAudios().getDuration();
                        }
                    }
                    workoutDuration += circuitDuration;
                }
                workout.setExerciseCount(totalExercises);
                workout.setCircuitCount(circuitCount);
                workout.setDuration(workoutDuration);
                workout.setVideoProcessingPending(isVideoProcessingPending);

                boolean containsBlockedExercise = false;
                if (!exerciseIdList.isEmpty()) {
                    containsBlockedExercise = flaggedExercisesSummaryRepository.existsByExerciseExerciseIdInAndFlagStatus(exerciseIdList, KeyConstants.KEY_BLOCK);
                }
                workout.setContainsBlockedExercise(containsBlockedExercise);

                workoutResponseModelList.add(workout);
            }
            responseModel.setWorkouts(workoutResponseModelList);
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Workout details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        if (program.getWorkoutSchedules() != null && !program.getWorkoutSchedules().isEmpty()) {
            List<WorkoutScheduleModel> schedules = new ArrayList<>();
            for (WorkoutSchedule workoutSchedule : program.getWorkoutSchedules()) {
                WorkoutScheduleModel workoutScheduleModel = new WorkoutScheduleModel();
                workoutScheduleModel.setRestDay(workoutSchedule.isRestDay());
                workoutScheduleModel.setOrder(workoutSchedule.getOrder());
                workoutScheduleModel.setDay(Convertions.getDayText(workoutSchedule.getOrder()));
                workoutScheduleModel.setScheduleId(workoutSchedule.getWorkoutScheduleId());
                if (!workoutSchedule.isRestDay()) {
                    workoutScheduleModel.setWorkoutId(workoutSchedule.getWorkout().getWorkoutId());
                    workoutScheduleModel.setTitle(workoutSchedule.getWorkout().getTitle());
                    workoutScheduleModel.setDescription(workoutSchedule.getWorkout().getDescription());
                    if (workoutSchedule.getWorkout().getImage() != null) {
                        workoutScheduleModel.setThumbnailUrl(workoutSchedule.getWorkout().getImage().getImagePath());
                        workoutScheduleModel.setImageId(workoutSchedule.getWorkout().getImage().getImageId());
                    }

                    Workouts workouts = workoutRepo.findByWorkoutId(workoutSchedule.getWorkout().getWorkoutId());
                    long workoutDuration = 0;
                    int circuitCount = 0;
                    boolean isVideoProcessingPending = false;
                    List<Long> exerciseIdList = new ArrayList<>();

                    Set<CircuitSchedule> circuitSchedules = workouts.getCircuitSchedules();
                    for (CircuitSchedule circuitSchedule : circuitSchedules) {
                        long circuitDuration = 0;

                        boolean isRestCircuit = circuitSchedule.isRestCircuit();
                        if (isRestCircuit) {
                            circuitDuration = circuitSchedule.getRestDuration();
                        } else if(circuitSchedule.getCircuit() != null && (circuitSchedule.getIsAudio() == null || !circuitSchedule.getIsAudio())){
                            circuitCount++;
                            Set<ExerciseSchedulers> exerciseSchedulers = circuitSchedule.getCircuit().getExerciseSchedules();
                            for (ExerciseSchedulers exerciseScheduler : exerciseSchedulers) {
                                long exerciseDuration = 0;
                                if (exerciseScheduler.getExercise() != null) {
                                    if (exerciseScheduler.getExercise().getVideoManagement() != null) {
                                        exerciseDuration = exerciseScheduler.getExercise().getVideoManagement().getDuration();

                                        if (exerciseScheduler.getLoopCount() != null && exerciseScheduler.getLoopCount() > 0) {
                                            //Repeat count Change :  no of times video is played
                                            exerciseDuration = exerciseDuration * exerciseScheduler.getLoopCount();
                                        }

                                        if (fitwiseUtils.isVideoProcessingPending(exerciseScheduler.getExercise().getVideoManagement())) {
                                            isVideoProcessingPending = true;
                                        }
                                    }
                                    if (exerciseScheduler.getExercise().getEquipments() != null) {
                                        equipments.addAll(exerciseScheduler.getExercise().getEquipments());
                                    }
                                    exerciseIdList.add(exerciseScheduler.getExercise().getExerciseId());
                                } else if(exerciseScheduler.getWorkoutRestVideo() != null){
                                    exerciseDuration = exerciseScheduler.getWorkoutRestVideo().getRestTime();
                                }else if(exerciseScheduler.getVoiceOver() != null){
                                    exerciseDuration = exerciseScheduler.getVoiceOver().getAudios().getDuration();
                                }
                                circuitDuration += exerciseDuration;
                            }
                            Long repeat = circuitSchedule.getRepeat();
                            Long restBetweenRepeat = circuitSchedule.getRestBetweenRepeat();
                            if (repeat != null && repeat > 0) {
                                //Repeat Count Change : Since repeat count is changes as no of times video should play
                                circuitDuration = circuitDuration * repeat;

                                long repeatRestDuration = 0;
                                if (restBetweenRepeat != null && restBetweenRepeat > 0) {
                                    //Repeat Count Change : Since repeat count is changes as no of times video should play
                                    repeatRestDuration = restBetweenRepeat * (repeat-1);
                                }
                                circuitDuration = circuitDuration  + repeatRestDuration;
                            }
                        }else if(circuitSchedule.getIsAudio() != null && circuitSchedule.getIsAudio()){
                            for(CircuitAndVoiceOverMapping circuitAndVoiceOverMapping : circuitSchedule.getCircuit().getCircuitAndVoiceOverMappings()){
                                circuitDuration += circuitAndVoiceOverMapping.getVoiceOver().getAudios().getDuration();
                            }
                        }
                        workoutDuration += circuitDuration;
                    }
                    workoutScheduleModel.setCircuitCount(circuitCount);
                    workoutScheduleModel.setDuration(workoutDuration);
                    workoutScheduleModel.setVideoProcessingPending(isVideoProcessingPending);

                    boolean containsBlockedExercise = false;
                    if (!exerciseIdList.isEmpty()) {
                        containsBlockedExercise = flaggedExercisesSummaryRepository.existsByExerciseExerciseIdInAndFlagStatus(exerciseIdList, KeyConstants.KEY_BLOCK);
                    }
                    workoutScheduleModel.setContainsBlockedExercise(containsBlockedExercise);

                } else {
                    //Rest Activity data in workout schedule response
                    InstructorRestActivity instructorRestActivity = workoutSchedule.getInstructorRestActivity();
                    String title = DBConstants.REST;
                    String activityName = DBConstants.REST;
                    RestActivity restActivity;
                    RestMetric restMetric = null;
                    Long value = null;
                    String notes = null;
                    String workoutThumbnail = null;
                    Long imageId = null;
                    if (instructorRestActivity != null) {
                        String restActivityName = instructorRestActivity.getActivityName();
                        if (!DBConstants.REST.equalsIgnoreCase(restActivityName)) {
                            restMetric = instructorRestActivity.getRestActivityToMetricMapping().getRestMetric();
                            String metric = restMetric.getRestMetric();
                            value = instructorRestActivity.getValue();
                            notes = instructorRestActivity.getNotes();
                            title = restActivityName + " - " + value + " " + metric;
                            activityName = restActivityName;
                        }

                        restActivity = instructorRestActivity.getRestActivityToMetricMapping().getRestActivity();
                    } else {
                        restActivity = restActivityRepository.findByRestActivity(DBConstants.REST);
                    }
                    if (restActivity.getImage() != null) {
                        workoutThumbnail = restActivity.getImage().getImagePath();
                        imageId = restActivity.getImage().getImageId();
                    }
                    workoutScheduleModel.setTitle(title);
                    workoutScheduleModel.setThumbnailUrl(workoutThumbnail);
                    workoutScheduleModel.setImageId(imageId);

                    //Setting RestActivityScheduleModel in response
                    RestActivityScheduleModel restActivityModel = new RestActivityScheduleModel();
                    restActivityModel.setActivityId(restActivity.getRestActivityId());
                    restActivityModel.setActivityName(activityName);
                    if (restMetric != null) {
                        restActivityModel.setMetricId(restMetric.getRestMetricId());
                    }
                    if (value != null) {
                        restActivityModel.setValue(value);
                    }
                    if( notes != null) {
                           	  
       			        restActivityModel.setNotes(notes);
                    }
                    workoutScheduleModel.setRestActivityModel(restActivityModel);

                }
                schedules.add(workoutScheduleModel);
            }
            schedules.sort(Comparator.comparing(WorkoutScheduleModel::getOrder));
            responseModel.setWorkoutSchedules(schedules);
            List<Equipments> equipmentsList = equipments.stream()
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(Equipments::getEquipmentId))),
                            ArrayList::new));
            responseModel.setEquipments(equipmentsList);
        }
        profilingEndTimeMillis = new Date().getTime();
        log.info("Workout schedule details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        long activeSubscriptionCount = subscriptionService.getActiveSubscriptionCountOfProgram(program.getProgramId());
        responseModel.setActiveSubscriptions((int) activeSubscriptionCount);
        profilingEndTimeMillis = new Date().getTime();
        log.info("subscription count : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long methodEndTimeMillis = new Date().getTime();
        log.info("Program response construction duration : Time taken in millis : " + (methodEndTimeMillis - methodStartTimeMillis));
        log.info("Program response construction ends.");

        return responseModel;
    }

    /**
     * Construct program tile model.
     *
     * @param program the program
     * @return the program response model
     * @throws ApplicationException the application exception
     */
    public ProgramTileModel constructProgramTileModel(Programs program, Map<Long, Long> offerCountMap) {
        long temp = new Date().getTime();
        ProgramTileModel programTileModel = new ProgramTileModel();
        programTileModel.setProgramId(program.getProgramId());
        programTileModel.setProgramTitle(program.getTitle());
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        if (program.getProgramType() != null)
            programTileModel.setProgramType(program.getProgramType().getProgramTypeName());
        if (program.getProgramExpertiseLevel() != null)
            programTileModel.setProgramExpertiseLevel(program.getProgramExpertiseLevel().getExpertiseLevel());
        if (program.getImage() != null) {
            programTileModel.setThumbnailUrl(program.getImage().getImagePath());
        }
        programTileModel.setStatus(program.getStatus());
        //for checking whether given program is a sample program
        boolean isSampleProgram = sampleProgramsRepository.existsByProgramsProgramId(program.getProgramId());
        if (isSampleProgram) {
            programTileModel.setHelpingText(KeyConstants.KEY_SAMPLE_PROGRAM_HELPING_TEXT);
        }
        log.info("Check sample program " + (new Date().getTime() - temp));
        if (program.getDuration() != null && program.getDuration().getDuration() != null)
            programTileModel.setProgramDuration(program.getDuration().getDuration());
        programTileModel.setCreatedOn(program.getCreatedDate());
        programTileModel.setCreatedOnFormatted(fitwiseUtils.formatDate(program.getCreatedDate()));
        programTileModel.setLastUpdatedOn(program.getModifiedDate());
        programTileModel.setLastUpdatedOnFormatted(fitwiseUtils.formatDate(program.getModifiedDate()));
        if (program.getProgramPrice() != null) {
            programTileModel.setPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(program.getProgramPrice()));
        }
        temp = new Date().getTime();
        long subscriptionCount = subscriptionService.getActiveSubscriptionCountOfProgram(program.getProgramId());
        log.info("subscription count new : time : "+(System.currentTimeMillis()-temp));
        programTileModel.setActiveSubscriptions((int) subscriptionCount);
        /** Discount offers **/
        long noOfCurrentAvailableOffers = offerCountMap.get(program.getProgramId());
        log.info("Get availed offers " + (new Date().getTime() - temp));
        programTileModel.setNumberOfCurrentAvailableOffers((int) noOfCurrentAvailableOffers);
        AppConfigKeyValue appConfig = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX);
        if (appConfig != null) {
        	double flatTax = Double.parseDouble(appConfig.getValueString());
        	programTileModel.setFlatTax(flatTax);
        }
        return programTileModel;
    }

    /**
     * Publish program.
     *
     * @param programId the program id
     * @return the response model
     * @throws ApplicationException the application exception
     */
    public ResponseModel publishProgram(Long programId) throws StripeException {
        log.info("Publish program starts.");
        long apiStartTimeMillis = new Date().getTime();
        boolean isProgramPublished = false;
        Programs program = null;
        long profilingEndTimeMillis;
        ResponseModel response = new ResponseModel();
        try {
            fitwiseUtils.validateCurrentInstructorBlocked();
            User user = userComponents.getUser();
            program = programImpl.findByProgramIdAndOwnerUserId(programId, user.getUserId());
            log.info("Validate block status, get user and get program from DB : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            if (program == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, null);
            }
            doValidateProgramForPublish(program);
            //Validate for flagged videos
            validateFlaggedVideosInProgram(program.getWorkoutSchedules());
            //validate for active trial/paid subscriptions
            long activeSubscriptionCount = subscriptionService.getActiveSubscriptionCountOfProgram(programId);
            log.info("Get paid subscriptions of the program : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();
            program.setPublish(true);
            program.setStatus(InstructorConstant.PUBLISH);
            programImpl.saveProgram(program);
            fitwiseQboEntityService.createOrUpdateQboProduct(program);
            log.info("Create or update QBO product : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            response.setStatus(Constants.SUCCESS_STATUS);
            response.setPayload(null);
            response.setMessage("Successfully published program");
            isProgramPublished = true;
            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
            log.info("Publish program ends.");
        } finally {
            if (isProgramPublished) {
                stripeService.createProgramInStripe(program.getProgramId());
                response = new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_STRIPE_PRICE_CREATED, null);
            }
        }
        return response;
    }

    /**
     * Do validate program for publish.
     *
     * @param program the program
     * @throws ApplicationException the application exception
     */
    private void doValidateProgramForPublish(Programs program) {
        if (program.getStatus().equalsIgnoreCase(InstructorConstant.BLOCK) || program.getStatus().equalsIgnoreCase(DBConstants.BLOCK_EDIT)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PGM_BLOCKED, null);
        }
        if (program.getStatus().equalsIgnoreCase(InstructorConstant.PLAN) || program.getStatus().equalsIgnoreCase(InstructorConstant.UPLOAD) || program.getStatus().equalsIgnoreCase(InstructorConstant.SCHEDULE)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PROGRAM_NOT_COMPLETED_YET, null);
        }
        if (program.getImage() == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_PGM_THUMBNAIL_REQUIRED, null);
        }
        if (program.getPromotion() == null || program.getPromotion().getVideoManagement() == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_PGM_PROMO_REQUIRED, null);
        } else if (program.getPromotion().getVideoManagement().getUploadStatus() == null || !program.getPromotion().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.COMPLETED)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_PGM_PROMO_UPLOAD_NOT_COMPLETED, null);
        }
        if (program.getProgramPrice() == null || program.getProgramPrice() == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PROGRAM_NOT_COMPLETED_YET, null);
        } else if (program.getProgramPrice() < StripeConstants.STRIPE_MINIMUM_PRICE) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_PRODUCT_MIN_PRICE, null);
        }
        ValidationUtils.throwException(program.getTitle().isEmpty(), ValidationMessageConstants.MSG_TITLE_NULL, Constants.BAD_REQUEST);
        ValidationUtils.throwException(program.getOwner() == null, MessageConstants.MSG_NO_OWNER_FOR_PROGRAM, Constants.BAD_REQUEST);
        if (program.getWorkoutSchedules().size() != program.getDuration().getDuration().intValue()) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_WORKOUT_SCHEDULE_INCOMPLETE, MessageConstants.ERROR);
        }
        Set<Long> scheduledWorkoutIds = new TreeSet<>();
        for (WorkoutSchedule workoutSchedule : program.getWorkoutSchedules()) {
            if (workoutSchedule.getWorkout() != null)
                scheduledWorkoutIds.add(workoutSchedule.getWorkout().getWorkoutId());
        }
        /*
         * Checking whether the workout schedule is not null
         */
        if (program.getWorkoutSchedules() == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_NO_WORKOUT_ADDED_TO_PROGRAMS, null);
        }
        boolean doesProgramContainsAWorkout = false;
        /*
         * Checking whether the workout schedule has a workout at-least
         */
        for (WorkoutSchedule workoutSchedule : program.getWorkoutSchedules()) {
            if (workoutSchedule.getWorkout() != null) {
                doesProgramContainsAWorkout = true;
                break;
            }
        }
        for (WorkoutSchedule workoutSchedule : program.getWorkoutSchedules()) {
            if (workoutSchedule.getWorkout() != null) {
                if (workoutSchedule.getWorkout().getCircuitSchedules() == null || workoutSchedule.getWorkout().getCircuitSchedules().isEmpty()) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_WKT_CIRCUIT_SCHEDULE_EMPTY + workoutSchedule.getWorkout().getTitle(), null);
                } else {
                    for (CircuitSchedule circuitSchedule : workoutSchedule.getWorkout().getCircuitSchedules()) {
                        if (!circuitSchedule.isRestCircuit()) {
                            Set<ExerciseSchedulers> exerciseSchedulers = circuitSchedule.getCircuit().getExerciseSchedules();

                            for (ExerciseSchedulers exerciseScheduler : exerciseSchedulers) {
                                if (exerciseScheduler.getExercise() != null && (exerciseScheduler.getExercise().getVideoManagement().getUploadStatus() == null || !(exerciseScheduler.getExercise().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.COMPLETED) || exerciseScheduler.getExercise().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.REUPLOAD) || exerciseScheduler.getExercise().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.REUPLOAD_INPROGRESS)) ||
                                        exerciseScheduler.getExercise().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED) || isSupportVideoNotUploaded(exerciseScheduler))) {
                                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_EXERCISE_UPLOAD_NOT_COMPLETED, null);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!doesProgramContainsAWorkout) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_NO_WORKOUT_ADDED_TO_PROGRAMS, null);
        }
        regulateRestInWorkoutSchedule(program.getWorkoutSchedules());
        Set<Long> scheduleOrderSet = new HashSet<>();
        for (WorkoutSchedule schedule : program.getWorkoutSchedules()) {
            int oldUniqueScheduleOrderSize = scheduleOrderSet.size();
            Long newScheduleOrder = schedule.getOrder();
            scheduleOrderSet.add(newScheduleOrder);
            int newUniqueScheduleOrderSize = scheduleOrderSet.size();
            if (oldUniqueScheduleOrderSize == newUniqueScheduleOrderSize || newScheduleOrder > program.getWorkoutSchedules().size()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_WKT_ORDER, MessageConstants.ERROR);
            }
        }
    }

    private boolean isSupportVideoNotUploaded(ExerciseSchedulers exerciseScheduler) {
        boolean isSupportVideoNotUploaded = false;
        VideoManagement videoManagement = exerciseScheduler.getExercise().getSupportVideoManagement();
        if ((videoManagement != null) && (videoManagement.getUploadStatus() == null || !(videoManagement.getUploadStatus().equalsIgnoreCase(VideoUploadStatus.COMPLETED) || videoManagement.getUploadStatus().equalsIgnoreCase(VideoUploadStatus.REUPLOAD)
        || videoManagement.getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED) || videoManagement.getUploadStatus().equalsIgnoreCase(VideoUploadStatus.REUPLOAD_INPROGRESS))))
        {
            isSupportVideoNotUploaded = true;
        }
        return isSupportVideoNotUploaded;
    }

    /**
     * Gets the programs.
     *
     * @param programId the program id
     * @return the programs
     * @throws ApplicationException the application exception
     * @throws ParseException 
     */
    public ProgramResponseModel getProgram(final Long programId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        log.info("Instructor getProgram starts.");
        long apiStartTimeMillis = new Date().getTime();
        long profilingStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        Programs program = programImpl.findByProgramIdAndOwnerUserId(programId, user.getUserId());
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, null);
        }
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        ProgramResponseModel programResponseModel = constructProgramModel(program);
        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("Instructor getProgram ends.");
        return programResponseModel;
    }

    public ProgramResponseModel getSampleProgram(Long programId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.info("Get sample program starts");
        long start = new Date().getTime();
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramId(programId);
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }
        boolean isSampleProgram = sampleProgramsRepository.existsByProgramsProgramId(programId);
        if (!isSampleProgram) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SAMPLE_PROGRAMS_NOT_FOUND, MessageConstants.ERROR);
        }
        ProgramResponseModel programResponseModel = constructProgramModel(program);
        long end = new Date().getTime();
        log.info("Get sample program API : Total time taken in millis : "+(end-start));
        log.info("Get Sample program ends");
        return programResponseModel;
    }

    /**
     * Gets the programs.
     *
     * @return the programs
     * @throws ApplicationException the application exception
     */
    public Map<String, Object> getAllPrograms(final String status, int pageNo, final int pageSize, Optional<String> searchName, Optional<String> sortOrderOptional, Optional<String> sortByOptional) {
        long startTime = new Date().getTime();
        long temp = new Date().getTime();
        log.info("Get All program started");
        User user = userComponents.getUser();
        log.info("Get user " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        String sortBy;
        String sortOrder;
        if ((sortOrderOptional.isPresent() && !sortOrderOptional.get().isEmpty()) && (sortByOptional.isPresent() && !sortByOptional.get().isEmpty())) {
            sortBy = sortByOptional.get();
            sortOrder = sortOrderOptional.get();
            if (!(sortBy.equalsIgnoreCase(SearchConstants.TITLE) || sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE))) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_SORT_BY, null);
            }
            if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
            }
        } else {
            sortBy = SearchConstants.CREATED_DATE;
            sortOrder = SearchConstants.ORDER_DSC;
        }
        if (searchName.isPresent() && !searchName.get().isEmpty()) {
            return getSearchedPrograms(user.getUserId(), searchName.get(), status, pageNo, pageSize, sortOrder, sortBy);
        }
        log.info("Field validation " + (new Date().getTime() - temp));
        Map<String, Object> response = getProgramsByType(user.getUserId(), status, pageNo, pageSize, sortOrder, sortBy);
        log.info("Get all program completed " + (new Date().getTime() - startTime));
        return response;
    }

    private Map<String, Object> getSearchedPrograms(long userId, String searchName, String status, int pageNo, int pageSize, String sortOrder, String sortBy) {
        Sort sort = sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE) ? Sort.by("modifiedDate") : Sort.by("title");
        sort = sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC) ? sort.ascending() : sort.descending();
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<Programs> programPage;
        if (status.isEmpty()) {
            programPage = programRepository.findByOwnerUserIdAndTitleIgnoreCaseContaining(userId, searchName, pageRequest);
        } else {
            List<String> statusList;
            if (InstructorConstant.UNPUBLISH.equalsIgnoreCase(status)) {
                statusList = Arrays.asList(InstructorConstant.UNPUBLISH, DBConstants.UNPUBLISH_EDIT);
            } else if (InstructorConstant.BLOCK.equalsIgnoreCase(status)) {
                statusList = Arrays.asList(InstructorConstant.BLOCK, DBConstants.BLOCK_EDIT);
            } else if (InstructorConstant.INPROGRESS.equalsIgnoreCase(status)) {
                statusList = Arrays.asList(InstructorConstant.PLAN, InstructorConstant.UPLOAD, InstructorConstant.SCHEDULE, InstructorConstant.PRICE, InstructorConstant.PRE_PUBLISH);
            } else {
                statusList = Arrays.asList(status);
            }
            programPage = programRepository.findByOwnerUserIdAndStatusInAndTitleIgnoreCaseContaining(userId, statusList, searchName,  pageRequest);
        }
        if (programPage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        long temp = System.currentTimeMillis();
        List<Long> programIdList = programPage.stream().map(Programs::getProgramId).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForInstructor(programIdList);
        log.info(StringConstants.LOG_OFFER_COUNT_QUERY + (System.currentTimeMillis() - temp));
        List<ProgramTileModel> programTileModels = new ArrayList<>();
        for (Programs program : programPage) {
            programTileModels.add(constructProgramTileModel(program, offerCountMap));
        }
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(KeyConstants.KEY_PROGRAMS, programTileModels);
        responseMap.put(KeyConstants.KEY_TOTAL_COUNT, programPage.getTotalElements());
        return responseMap;
    }

    public Map<String, Object> getProgramsByType(long userId, String status, int pageNo, int pageSize, String sortOrder, String sortBy) {
        long temp = new Date().getTime();
    	Sort sort = sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE) ? Sort.by("modifiedDate") : Sort.by("title");
        sort = sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC) ? sort.ascending() : sort.descending();
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<Programs> programsPage;
        if (!status.isEmpty()) {
            List<String> statuslist = null;
            if (status.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
                statuslist = Arrays.asList(InstructorConstant.PUBLISH);
            } else if (status.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {
                statuslist = Arrays.asList(InstructorConstant.UNPUBLISH, DBConstants.UNPUBLISH_EDIT);
            } else if (status.equalsIgnoreCase(InstructorConstant.BLOCK)) {
                statuslist = Arrays.asList(InstructorConstant.BLOCK, DBConstants.BLOCK_EDIT);
            } else if (status.equalsIgnoreCase(InstructorConstant.INPROGRESS)) {
                statuslist = Arrays.asList(InstructorConstant.PLAN, InstructorConstant.UPLOAD, InstructorConstant.SCHEDULE, InstructorConstant.PRICE, InstructorConstant.PRE_PUBLISH);
            }
            if (statuslist == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_STATUS_INCORRECT, null);
            }
            programsPage = programRepository.findByOwnerUserIdAndStatusIn(userId, statuslist, pageRequest);
        } else {
            programsPage = programRepository.findByOwnerUserId(userId, pageRequest);
        }
        log.info("Get programs " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        if (programsPage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        long profilingStart = System.currentTimeMillis();
        List<Long> programIdList = programsPage.stream().map(Programs::getProgramId).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForInstructor(programIdList);
        log.info(StringConstants.LOG_OFFER_COUNT_QUERY + (System.currentTimeMillis() - profilingStart));
        List<ProgramTileModel> programTileModels = new ArrayList<>();
        for (Programs program : programsPage) {
            programTileModels.add(constructProgramTileModel(program, offerCountMap));
        }
        log.info("Construct response " + (new Date().getTime() - temp));
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(KeyConstants.KEY_PROGRAMS, programTileModels);
        responseMap.put(KeyConstants.KEY_TOTAL_COUNT, programsPage.getTotalElements());
        return responseMap;
    }

    /**
     * Upload promo
     *
     * @param promoUploadModel
     * @return
     * @throws IOException
     */
    @Transactional
    public PromoUploadResponseModel uploadPromotion(final PromoUploadModel promoUploadModel, String type) throws IOException {
        User user = userComponents.getUser();
        UserProfile userProfile = userProfileRepository.findByUser(user);
        return uploadPromotion(promoUploadModel, userProfile, type);
    }

        /**
         * Upload promotion.
         *
         * @param promoUploadModel the promo upload model
         * @return the promo upload response model
         * @throws IOException          Signals that an I/O exception has occurred.
         * @throws ApplicationException the application exception
         */
    @Transactional
    public PromoUploadResponseModel uploadPromotion(final PromoUploadModel promoUploadModel, UserProfile userProfile, String type) throws IOException {
        log.info("Upload promo starts");
        long start = new Date().getTime();
        long profilingStartTimeInMillis;
        long profilingEndTimeInMillis;
        VimeoModel vimeoModel = new VimeoModel();
        if (!promoUploadModel.getFileName().isEmpty() && userProfile.getFirstName() != null && userProfile.getLastName() != null) {
            promoUploadModel.setFileName(userProfile.getFirstName() + " " + userProfile.getLastName() + " - " + promoUploadModel.getFileName());
        }
        vimeoModel.setName(promoUploadModel.getFileName());
        UploadModel upload = new UploadModel();
        upload.setSize(promoUploadModel.getFileSize());
        upload.setApproach(VimeoConstants.APPROACH);
        vimeoModel.setUpload(upload);
        profilingStartTimeInMillis = new Date().getTime();
        vimeoModel = vimeoService.createVideoPlaceholder(vimeoModel);
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Creating placeholder in vimeo : time taken in millis : "+(profilingEndTimeInMillis-profilingStartTimeInMillis));
        Promotions promotions = new Promotions();
        promotions.setActive(true);
        promotions.setDescription(promoUploadModel.getDescription());
        promotions.setTitle(promoUploadModel.getTitle());
        VideoManagement videoManagement = new VideoManagement();
        videoManagement.setOwner(userProfile.getUser());
        videoManagement.setTitle(promoUploadModel.getTitle());
        videoManagement.setDescription(promoUploadModel.getDescription());
        videoManagement.setUrl(vimeoModel.getUri());
        if (promoUploadModel.getImageId() != null && promoUploadModel.getImageId() != 0) {
            Images images = imageRepository.findByImageId(promoUploadModel.getImageId());
            if (images != null)
                videoManagement.setThumbnail(images);
        }
        videoManagement.setUploadStatus(VideoUploadStatus.UPLOAD);

        promotions.setVideoManagement(videoManagement);
        promotionRepository.save(promotions);
        if(type.equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)){
            userProfile.setPromotion(promotions);
        }
        PromoUploadResponseModel promoUploadResponseModel = new PromoUploadResponseModel();
        promoUploadResponseModel.setPromotions(promotions);
        promoUploadResponseModel.setVimeoModel(vimeoModel);
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Promo upload : total time taken in millis : "+(profilingEndTimeInMillis-start));
        log.info("Upload promo ends");
        return promoUploadResponseModel;
    }

    /**
     * Delete program.
     *
     * @param programId the program id
     * @throws ApplicationException the application exception
     */
    public void deleteProgram(final Long programId) {
        User user = userComponents.getUser();
        Programs program = programImpl.findByProgramIdAndOwnerUserId(programId, user.getUserId());
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, null);
        }
        List<String> restrictedStatusList = Arrays.asList(InstructorConstant.PUBLISH, InstructorConstant.UNPUBLISH, InstructorConstant.BLOCK, DBConstants.UNPUBLISH_EDIT, DBConstants.BLOCK_EDIT);
        boolean isProgramRestricted = restrictedStatusList.stream().anyMatch(program.getStatus()::equalsIgnoreCase);

        if (isProgramRestricted) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_PUBLISH_UN_PUBLISH_BLOCK_PROGRAM_DELETE_INVALID, null);
        }
        programRepository.delete(program);
    }

    /**
     * Get promotion details
     *
     * @param promotionId
     * @return
     * @throws ApplicationException
     */
    public ResponseModel getPromotions(Long promotionId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if (promotionId == null || promotionId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_ID_NULL, MessageConstants.ERROR);
        }
        ResponseModel responseModel = new ResponseModel();
        PromotionResponseView promotionResponseView = new PromotionResponseView();
        Promotions promotions = promotionRepository.findByPromotionId(promotionId);
        if (promotions == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_NOT_FOUND, MessageConstants.ERROR);
        }
        VideoManagement videoManagement = promotions.getVideoManagement();
        String vimeoUrl;
        String vimeoId = "";
        if (videoManagement != null) {
            vimeoUrl = videoManagement.getUrl();
            if (vimeoUrl != null) {
                /*
                 * Video url saved from vimeo in Video management table will be of /videos/8989xxxx format
                 * Parsing the above format and getting only the video id to get .mp4 url
                 */
                try {
                    if (vimeoUrl.contains("/")) {
                        String[] videoIds = vimeoUrl.split("/");
                        vimeoId = videoIds[2];
                    }
                } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                    log.error("The index you have entered is invalid", arrayIndexOutOfBoundsException);
                } catch (Exception exception) {
                    log.error("Unexpected error", exception);
                }
            }
        }
        if (!vimeoId.isEmpty()) {
            promotionResponseView.setVideoStandards(vimeoService.getVimeoVideos(Long.parseLong(vimeoId)));
        }
        promotionResponseView.setPromotionId(promotions.getPromotionId());
        promotionResponseView.setVideoManagement(promotions.getVideoManagement());
        promotionResponseView.setDescription(promotions.getDescription());
        promotionResponseView.setActive(promotions.isActive());
        promotionResponseView.setTitle(promotions.getTitle());
        responseModel.setPayload(promotionResponseView);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_PROMOTION_FETCHED);
        return responseModel;
    }

    /**
     * Delete Promo video
     *
     * @param promotionId
     * @return
     * @throws ApplicationException
     */
    public ResponseModel deletePromotion(Long promotionId, Long programId) {
        log.info("Delete promotions starts.");
        long apiStartTimeMillis = new Date().getTime();
        ResponseModel responseModel = new ResponseModel();
        if (promotionId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_ID_NULL, MessageConstants.ERROR);
        }
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Promotions promotions = promotionRepository.findByPromotionId(promotionId);
        if (promotions == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_NOT_FOUND, MessageConstants.ERROR);
        }
        Programs program = programImpl.getProgram(programId);
        if (program == null) {
            throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }
        List<Programs> programsList = programRepository.findByPromotionPromotionId(promotionId);
        if (programsList.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_NOT_FOUND, MessageConstants.ERROR);
        }
        if (!programsList.contains(program)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_INCORRECT, MessageConstants.ERROR);
        }
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        boolean isRelatedToOneProgram = programsList.size() == 1;
        program.setPromotion(null);
        programRepository.save(program);
        log.info("Query to save program : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        if (isRelatedToOneProgram) {
            //deleting promo video on vimeo
            VideoManagement videoManagement = promotions.getVideoManagement();
            try {
                if (videoManagement != null) {
                    profilingEndTimeMillis = new Date().getTime();
                    vimeoService.deleteVimeoVideo(videoManagement.getUrl());
                    log.info("Query to delete promotion from vimeo : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                }
            } catch (Exception exception) {
                log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
            }
            profilingEndTimeMillis = new Date().getTime();
            promotionRepository.delete(promotions);
            log.info("Query to delete promotion from DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }
        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_PROMOTION_DELETED);
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Delete promotion ends.");
        return responseModel;
    }

    /**
     * Delete thumbnail
     *
     * @param imageId
     * @return
     * @throws ApplicationException
     */
    public ResponseModel deleteThumbnail(Long imageId, Long programId) {
        ResponseModel responseModel = new ResponseModel();
        if (imageId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_IMAGE_ID_NULL, MessageConstants.ERROR);
        }
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Images images = imageRepository.findByImageId(imageId);
        if (images == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_IMAGE_NOT_FOUND, MessageConstants.ERROR);
        }
        Programs program = programImpl.getProgram(programId);
        if (program == null) {
            throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, MessageConstants.ERROR);
        }
        List<Programs> programsList = programRepository.findByImageImageId(imageId);
        boolean isPresent = false;
        for (Programs programs : programsList) {
            if (programs.getProgramId().equals(program.getProgramId())) {
                isPresent = true;
                break;
            }
        }
        if (!isPresent) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_INCORRECT, MessageConstants.ERROR);
        }
        program.setImage(null);
        programRepository.save(program);
        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_THUMBNAIL_DELETED);
        return responseModel;
    }

    /**
     * Get the sample program and sample video
     *
     * @return
     */
    public ResponseModel getSampleAndTourDetails() {
        AppConfigKeyValue appConfigKeyValue = appConfigKeyValueRepository.findByKeyString(KeyConstants.KEY_SAMPLE_PROGRAM_ID);
        ProgramTileModel programTileModel = null;
        if (appConfigKeyValue != null) {
            Programs program = programRepository.findByProgramId(Long.parseLong(appConfigKeyValue.getValueString()));
            if (program != null) {
                List<Long> programIdList = Arrays.asList(program.getProgramId());
                Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForInstructor(programIdList);
                programTileModel = constructProgramTileModel(program, offerCountMap);
            }
        }
        Map<String, Object> resMap = new HashMap<>();
        resMap.put(KeyConstants.KEY_SAMPLE_PROGRAM, programTileModel);
        resMap.put(KeyConstants.KEY_QUICK_TOUR_VIDEO, null);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_INSTRUCTOR_PROGRAMS_FETCHED, resMap);
    }

    /**
     * Delete all workout schedule from the given program
     *
     * @param programId
     * @return
     */
    @Transactional
    public ResponseModel resetWorkoutFromSchedule(Long programId) {
        log.info("Reset workout from schedule starts");
        long start = new Date().getTime();
        long profilingStart;
        long profilingEnd;
        User user = userComponents.getUser();
        Programs program = programImpl.findByProgramIdAndOwnerUserId(programId, user.getUserId());
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, null);
        }
//        String[] restrictedSatusArray = {InstructorConstant.PUBLISH, InstructorConstant.UNPUBLISH, DBConstants.UNPUBLISH_EDIT, InstructorConstant.BLOCK, DBConstants.BLOCK_EDIT};
        String[] restrictedSatusArray = {InstructorConstant.UNPUBLISH, DBConstants.UNPUBLISH_EDIT, InstructorConstant.BLOCK, DBConstants.BLOCK_EDIT};
        
        List<String> restrictedStatusList = Arrays.asList(restrictedSatusArray);
        boolean isProgramRestricted = restrictedStatusList.stream().anyMatch(program.getStatus()::equalsIgnoreCase);
        if (isProgramRestricted) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_PGM_WKT_SCH_RESET_RESTRICTED, MessageConstants.ERROR);
        }
        profilingStart = new Date().getTime();
        if (!program.getWorkoutSchedules().isEmpty()) {
            List<Long> workoutScheduleIdList = program.getWorkoutSchedules().stream().map(WorkoutSchedule::getWorkoutScheduleId).collect(Collectors.toList());
            //Deleting completions related to workout schedules
            List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByWorkoutScheduleIdIn(workoutScheduleIdList);
            workoutCompletionRepository.deleteInBatch(workoutCompletionList);

            List<CircuitCompletion> circuitCompletionList = circuitCompletionRepository.findByWorkoutScheduleIdIn(workoutScheduleIdList);
            circuitCompletionRepository.deleteInBatch(circuitCompletionList);

            List<ExerciseCompletion> exerciseCompletionList = exerciseCompletionRepository.findByWorkoutScheduleIdIn(workoutScheduleIdList);
            exerciseCompletionRepository.deleteInBatch(exerciseCompletionList);

            List<WorkoutFeedback> workoutFeedbackList = workoutFeedbackRepository.findByWorkoutScheduleWorkoutScheduleIdIn(workoutScheduleIdList);
            workoutFeedbackRepository.deleteInBatch(workoutFeedbackList);

            List<WorkoutDiscardFeedback> workoutDiscardFeedbackList = workoutDiscardFeedbackRepository.findByWorkoutScheduleWorkoutScheduleIdIn(workoutScheduleIdList);
            workoutDiscardFeedbackRepository.deleteInBatch(workoutDiscardFeedbackList);

            workoutScheduleRepository.deleteByPrograms(program);
        }
        profilingEnd = new Date().getTime();
        log.info("Deleting Respective Data from DB : Time aken in millis : "+(profilingEnd-profilingStart));
        if (InstructorConstant.PUBLISH.equals(program.getStatus())) {
        	program.setStatus(InstructorConstant.PUBLISH);
        } else {
        program.setStatus(InstructorConstant.SCHEDULE);
        }
        programRepository.save(program);
        profilingEnd = new Date().getTime();
        log.info("Reset workout from schedule : Total time taken in millis : "+(profilingEnd-start));
        log.info("Reset workout from schedule ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SCS_PGM_WKT_SCH_DELETED, null);
    }

    public void validateName(String programName) {
        User user = userComponents.getUser();
        Programs programs = programImpl.findByOwnerUserIdAndTitle(user.getUserId(), programName);
        if (programs != null) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_PROGRAM_DUPLICATE_TITLE, null);
        }
    }

    public ResponseModel programPromoCompletionStatus(long programId) {
        log.info("Program promo completion starts");
        long start = new Date().getTime();
        User user = userComponents.getAndValidateUser();
        Programs program = validationService.validateProgramIdBlocked(programId);
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime()-start));
        long profilingStart = new Date().getTime();
        ProgramPromoViews programPromoCompletionStatus = new ProgramPromoViews();
        programPromoCompletionStatus.setProgram(program);
        if(user != null){
            programPromoCompletionStatus.setUser(user);
        }
        programPromoCompletionStatus.setDate(new Date());
        programPromoCompletionStatus.setStatus(KeyConstants.KEY_WATCHED);
        programPromoCompletionStatusRepository.save(programPromoCompletionStatus);
        log.info("DB update : Time taken in millis : "+(new Date().getTime()-profilingStart));
        log.info("Program promo completion : Total Time taken in millis : "+(new Date().getTime()-start));
        log.info("Program promo completion ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROMO_STATUS_SAVED, null);
    }

    public ResponseModel postUserProgramAudit(long programId) {
        Programs program = validationService.validateProgramIdBlocked(programId);

        ProgramSubscription programSubscription = programSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(userComponents.getUser().getUserId(), programId);
        if (programSubscription != null) {
            SubscriptionStatus subscriptionStatus = programSubscription.getSubscriptionStatus();
            if (subscriptionStatus != null) {
                String subscriptionStatusName = subscriptionStatus.getSubscriptionStatusName();
                if (subscriptionStatusName.equalsIgnoreCase(KeyConstants.KEY_TRIAL) || subscriptionStatusName.equalsIgnoreCase(KeyConstants.KEY_UNSUBSCRIBED)) {
                    ProgramTrailAudit programTrailAudit = new ProgramTrailAudit();
                    programTrailAudit.setProgram(program);
                    programTrailAudit.setUser(userComponents.getUser());
                    programTrailAudit.setDate(new Date());
                    programTrailAuditRepository.save(programTrailAudit);
                }
            }
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROGRAM_AUDIT_SAVED, null);
    }

    /**
     * Getting program prices
     *
     * @return
     */
    public ResponseModel getProgramPrices() {
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        List<ProgramPrices> programPrices = programPricesRepository.findByMinimumPrice();
        List<ProgramPricesResponseView> programPricesList = new ArrayList<>();
        AppConfigKeyValue appConfig = appConfigKeyValueRepository.findByKeyString(AppConfigConstants.ADMIN_CONFIG_FLAT_TAX);
        for (ProgramPrices programPrice : programPrices) {
            ProgramPricesResponseView programPricesResponseView = new ProgramPricesResponseView();
            programPricesResponseView.setProgramPricesId(programPrice.getProgramPricesId());
            programPricesResponseView.setCountry(programPrice.getCountry());
            programPricesResponseView.setPrice(programPrice.getPrice());
            programPricesResponseView.setFormattedPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(programPrice.getPrice()));
            if (appConfig != null) {
        		double flatTax = Double.parseDouble(appConfig.getValueString());
        		programPricesResponseView.setFlatTax(flatTax);
        	}
            programPricesList.add(programPricesResponseView);
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROGRAM_PRICE_DATA_FETCHED, programPricesList);

    }


    /**
     * To get sample programs list
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    public ResponseModel getSamplePrograms(int pageNo, int pageSize) {
        log.info("Get sample program starts.");
        long apiStartTimeMillis = new Date().getTime();
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        List<SamplePrograms> samplePrograms = sampleProgramsRepository.findAll();
        log.info("Query to get sample program list : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        if (samplePrograms.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        List<Long> sampleProgramIdList = samplePrograms.stream().map(sampleProg -> sampleProg.getPrograms().getProgramId()).collect(Collectors.toList());
        log.info("Extract sample program id list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        Specification<Programs> sampleProgramSpec = ProgramSpecifications.getProgramsInIdList(sampleProgramIdList);
        Page<Programs> programPage = programRepository.findAll(sampleProgramSpec, PageRequest.of(pageNo - 1, pageSize));
        log.info("Query to get programs : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        if (programPage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        long temp = System.currentTimeMillis();
        List<Long> programIdList = programPage.stream().map(Programs::getProgramId).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForInstructor(programIdList);
        log.info(StringConstants.LOG_OFFER_COUNT_QUERY + (System.currentTimeMillis() - temp));
        profilingEndTimeMillis = new Date().getTime();
        List<ProgramTileModel> programTileModelList = new ArrayList<>();
        for (Programs program : programPage) {
            programTileModelList.add(constructProgramTileModel(program, offerCountMap));
        }
        log.info("Construct program tile model list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        SampleProgramsView sampleProgramsView = new SampleProgramsView();
        sampleProgramsView.setSampleProgramsCount(Math.toIntExact(programPage.getTotalElements()));
        sampleProgramsView.setSamplePrograms(programTileModelList);
        log.info("Construct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get sample programs ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SAMPLE_PROGRAMS_FETCHED, sampleProgramsView);
    }

    /**
     * To get quick tour videos
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    public ResponseModel getQuickTourVideos(int pageNo, int pageSize) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RequestParamValidator.pageSetup(pageNo, pageSize);
        TourListResponseView tourListResponseView = new TourListResponseView();
        Page<QuickTourVideos> quickTourPage = quickTourRepository.findAll(PageRequest.of(pageNo - 1, pageSize));
        List<QuickTourVideos> quickTourVideosList = quickTourPage.getContent();
        List<QuickTourVideosResponseView> quickTourVideosResponseViews = new ArrayList<>();
        if (!quickTourVideosList.isEmpty()) {
            tourListResponseView.setTourVideosCount((int)quickTourPage.getTotalElements());
            for (QuickTourVideos quickTourVideos : quickTourVideosList) {
                QuickTourVideosResponseView quickTourVideosResponseView = new QuickTourVideosResponseView();
                VideoManagement videoManagement = videoManagementRepo.findByVideoManagementId(quickTourVideos.getVideoManagement().getVideoManagementId());
                quickTourVideosResponseView.setQuickTourVideoId(quickTourVideos.getQuickTourVideoId());
                quickTourVideosResponseView.setTitle(videoManagement.getTitle());
                quickTourVideosResponseView.setThumbnailUrl(videoManagement.getThumbnail().getImagePath());
                quickTourVideosResponseView.setVideoUrl(videoManagement.getUrl());
                quickTourVideosResponseView.setDuration(videoManagement.getDuration());
                String vimeoUrl = videoManagement.getUrl();
                String vimeoId = "";
                if (vimeoUrl.contains("/")) {
                    String[] videoIds = vimeoUrl.split("/");
                    vimeoId = videoIds[2];
                }
                if (!vimeoId.isEmpty())
                    quickTourVideosResponseView.setParsedVideoUrl(vimeoService.getVimeoUrlFromId(Long.parseLong(vimeoId)));
                quickTourVideosResponseViews.add(quickTourVideosResponseView);
            }
        }
        tourListResponseView.setQuickTourVideos(quickTourVideosResponseViews);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_QUICK_TOUR_VIDEOS_FETCHED, tourListResponseView);
    }

    /**
     * API to return RestActivityTypes for program workout schedule
     *
     * @return
     */
    public List<RestActivityResponse> restActivityTypes() {
        log.info("Get Rest activity types starts");
        long start = new Date().getTime();
        List<RestActivity> restActivities = restActivityRepository.findAll();
        if(restActivities.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, "", "");
        }
        log.info("Query : Time taken in millis : "+(new Date().getTime() - start));
        long temp = new Date().getTime();
        List<RestActivityToMetricMapping> restActivityToMetricMappings = restActivityToMetricMappingRepository.findAll();
        Map<Long, List<RestActivityToMetricMapping>> restActiviryMetricMappingMap = new HashMap<>();
        for(RestActivityToMetricMapping restActivityToMetricMapping : restActivityToMetricMappings){
            if(restActiviryMetricMappingMap.get(restActivityToMetricMapping.getRestActivity().getRestActivityId()) == null){
                restActiviryMetricMappingMap.put(restActivityToMetricMapping.getRestActivity().getRestActivityId(), new ArrayList<>());
            }
            restActiviryMetricMappingMap.get(restActivityToMetricMapping.getRestActivity().getRestActivityId()).add(restActivityToMetricMapping);
        }
        List<RestActivityResponse> restActivityResponses = new ArrayList<>();
        for (RestActivity restActivity : restActivities) {
            RestActivityResponse activityResponse = new RestActivityResponse();
            activityResponse.setActivityId(restActivity.getRestActivityId());
            activityResponse.setActivityName(restActivity.getRestActivity());
            if (restActivity.getImage() != null) {
                activityResponse.setIconUrl(restActivity.getImage().getImagePath());
            }
            //All Rest types except Rest will have magnitude and a metric to measure it. Rest does not.
            if (!DBConstants.REST.equalsIgnoreCase(restActivity.getRestActivity()) && restActiviryMetricMappingMap.get(restActivity.getRestActivityId()) != null) {
                List<RestActivityToMetricMapping> mappings = restActiviryMetricMappingMap.get(restActivity.getRestActivityId());
                List<RestActivityMetricView> metricViews = new ArrayList<>();
                for (RestActivityToMetricMapping mapping : mappings) {
                    RestActivityMetricView restActivityMetricView = new RestActivityMetricView();
                    restActivityMetricView.setMetricId(mapping.getRestMetric().getRestMetricId());
                    restActivityMetricView.setMetricName(mapping.getRestMetric().getRestMetric());
                    restActivityMetricView.setMaxValue(mapping.getMaximumValue());
                    metricViews.add(restActivityMetricView);
                }
                activityResponse.setMetrics(metricViews);
            }
            restActivityResponses.add(activityResponse);
        }
        log.info("Response construction : Time taken in millis : "+(new Date().getTime() - temp));
        log.info("Get Rest Activity types : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get Rest activity types ends");
        return restActivityResponses;
    }

    /**
     * method to edit Unpublish or block programs
     *
     * @param model
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws ParseException 
     * @throws ApplicationException 
     */
    @Transactional
    public ProgramResponseModel restrictedProgramEdit(ProgramModel model) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        log.info("Restricted program edit starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();
        doValidateRoleAsInstructor(user);
        if (model.getProgramId() == null || model.getProgramId() == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramIdAndOwnerUserId(model.getProgramId(), user.getUserId());
        ValidationUtils.throwException(program == null, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, Constants.BAD_REQUEST);

        if (program.getStatus() == null || program.getStatus().isEmpty()) {
            throw new ApplicationException(Constants.CONFLICT, ValidationMessageConstants.MSG_PROGRAM_STATUS_NULL, MessageConstants.ERROR);
        }
        //Validation to allow only programs in unpublish, unpublish_edit or block, block_edit state
        List<String> restrictedStatusList = Arrays.asList(InstructorConstant.PUBLISH, DBConstants.PUBLISH, InstructorConstant.UNPUBLISH, DBConstants.UNPUBLISH_EDIT, InstructorConstant.BLOCK, DBConstants.BLOCK_EDIT);
        boolean isProgramRestricted = restrictedStatusList.stream().anyMatch(program.getStatus()::equalsIgnoreCase);
        if (!isProgramRestricted) {
            throw new ApplicationException(Constants.CONFLICT, ValidationMessageConstants.MSG_PROGRAMS_NOT_RESTRICTED, MessageConstants.ERROR);
        }
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        long activeSubscriptionCount = subscriptionService.getActiveSubscriptionCountOfProgram(model.getProgramId());
        if (!InstructorConstant.PUBLISH.equals(program.getStatus())) {
        	if (activeSubscriptionCount > 0) {
                throw new ApplicationException(Constants.CONFLICT, ValidationMessageConstants.MSG_RESTRICTED_PROGRAM_SUBSCRIBED.replace(StringConstants.LITERAL_COUNT, String.valueOf(activeSubscriptionCount)), MessageConstants.ERROR);
            }
        }
        
        log.info("Query to get active subscription count : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        doConstructPrograms(program, model, true);
        log.info("Construct programs : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        //Changing status of program
        if (InstructorConstant.PUBLISH.equals(program.getStatus())) {
            program.setStatus(DBConstants.PUBLISH);
        }
        if (InstructorConstant.UNPUBLISH.equals(program.getStatus())) {
            program.setStatus(DBConstants.UNPUBLISH_EDIT);
        }
        if (InstructorConstant.BLOCK.equals(program.getStatus())) {
            program.setStatus(DBConstants.BLOCK_EDIT);
        }
        program = programImpl.saveProgram(program);
        log.info("Query to update program status : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        ProgramResponseModel programResponseModel = constructProgramModel(program);
        log.info("Construct program response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Restricted program edit ends.");
        return programResponseModel;
    }

    /**
     * Reset all workout schedule from unpublish/block program
     *
     * @param programId
     * @return
     */
    @Transactional
    public ResponseModel resetWorkoutScheduleForRestrictedProgram(Long programId) {
        User user = userComponents.getUser();
        Programs program = programImpl.findByProgramIdAndOwnerUserId(programId, user.getUserId());
        if (program == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, null);
        }
        //Validation to allow only programs in unpublish, unpublish_edit or block, block_edit state
        List<String> restrictedStatusList = Arrays.asList(DBConstants.PUBLISH, InstructorConstant.UNPUBLISH, DBConstants.UNPUBLISH_EDIT, InstructorConstant.BLOCK, DBConstants.BLOCK_EDIT);
        boolean isProgramRestricted = restrictedStatusList.stream().anyMatch(program.getStatus()::equalsIgnoreCase);
        if (!isProgramRestricted) {
            throw new ApplicationException(Constants.CONFLICT, ValidationMessageConstants.MSG_PROGRAMS_NOT_RESTRICTED, MessageConstants.ERROR);
        }
        long activeSubscriptionCount = subscriptionService.getActiveSubscriptionCountOfProgram(programId);
        if (!InstructorConstant.PUBLISH.equals(program.getStatus())) {
        	if (activeSubscriptionCount > 0) {
                throw new ApplicationException(Constants.CONFLICT, ValidationMessageConstants.MSG_RESTRICTED_PROGRAM_SUBSCRIBED.replace(StringConstants.LITERAL_COUNT, String.valueOf(activeSubscriptionCount)), MessageConstants.ERROR);
            }
        }
        if (!program.getWorkoutSchedules().isEmpty()) {
            List<Long> workoutScheduleIdList = program.getWorkoutSchedules().stream().map(WorkoutSchedule::getWorkoutScheduleId).collect(Collectors.toList());
            //Deleting completions related to workout schedules
            List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByWorkoutScheduleIdIn(workoutScheduleIdList);
            workoutCompletionRepository.deleteInBatch(workoutCompletionList);
            List<CircuitCompletion> circuitCompletionList = circuitCompletionRepository.findByWorkoutScheduleIdIn(workoutScheduleIdList);
            circuitCompletionRepository.deleteInBatch(circuitCompletionList);
            List<ExerciseCompletion> exerciseCompletionList = exerciseCompletionRepository.findByWorkoutScheduleIdIn(workoutScheduleIdList);
            exerciseCompletionRepository.deleteInBatch(exerciseCompletionList);
            List<WorkoutFeedback> workoutFeedbackList = workoutFeedbackRepository.findByWorkoutScheduleWorkoutScheduleIdIn(workoutScheduleIdList);
            workoutFeedbackRepository.deleteInBatch(workoutFeedbackList);
            List<WorkoutDiscardFeedback> workoutDiscardFeedbackList = workoutDiscardFeedbackRepository.findByWorkoutScheduleWorkoutScheduleIdIn(workoutScheduleIdList);
            workoutDiscardFeedbackRepository.deleteInBatch(workoutDiscardFeedbackList);
            workoutScheduleRepository.deleteByPrograms(program);
        }
        //Changing status of program
        if (InstructorConstant.PUBLISH.equals(program.getStatus())) {
            program.setStatus(DBConstants.PUBLISH);
        }
        if (InstructorConstant.UNPUBLISH.equals(program.getStatus())) {
            program.setStatus(DBConstants.UNPUBLISH_EDIT);
        }
        if (InstructorConstant.BLOCK.equals(program.getStatus())) {
            program.setStatus(DBConstants.BLOCK_EDIT);
        }
        programRepository.save(program);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SCS_PGM_WKT_SCH_DELETED, null);
    }

    /**
     * Get all additional resources
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    public ResponseModel getAdditionalResources(int pageNo, int pageSize)  {
        RequestParamValidator.pageSetup(pageNo, pageSize);
        AdditionalResourcesListResponseView additionalResourcesListResponseView = new AdditionalResourcesListResponseView();
        Page<AdditionalResources> additionalResourcesList = additionalResourcesRepository.findAll(PageRequest.of(pageNo-1, pageSize));
        List<AdditionalResourcesResponseView> additionalResourcesResponseViews = new ArrayList<>();
        additionalResourcesListResponseView.setAdditionalResourcesCount(additionalResourcesList.getTotalElements());
        for (AdditionalResources additionalResources : additionalResourcesList) {
            AdditionalResourcesResponseView additionalResourcesResponseView = new AdditionalResourcesResponseView();
            additionalResourcesResponseView.setId(additionalResources.getId());
            additionalResourcesResponseView.setTitle(additionalResources.getTitle());
            additionalResourcesResponseView.setDescription(additionalResources.getDescription());
            additionalResourcesResponseView.setResourcePath(additionalResources.getResourcesPath());
            additionalResourcesResponseViews.add(additionalResourcesResponseView);
        }
        additionalResourcesListResponseView.setAdditionalResources(additionalResourcesResponseViews);
        if (additionalResourcesResponseViews.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_ADDITIONAL_RESOURCES_FETCHED, additionalResourcesListResponseView);
    }

    public ResponseModel getTourVideo(Long quickTourVideoId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        QuickTourVideos tourVideos = quickTourRepository.findByQuickTourVideoId(quickTourVideoId);
        if (tourVideos == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_TOUR_VIDEO_NOT_FOUND, MessageConstants.ERROR);
        }

        QuickTourVideosResponseView quickTourVideosResponseView = new QuickTourVideosResponseView();
        quickTourVideosResponseView.setQuickTourVideoId(tourVideos.getQuickTourVideoId());
        quickTourVideosResponseView.setTitle(tourVideos.getVideoManagement().getTitle());
        quickTourVideosResponseView.setThumbnailUrl(tourVideos.getVideoManagement().getThumbnail().getImagePath());
        quickTourVideosResponseView.setDuration(tourVideos.getVideoManagement().getDuration());
        quickTourVideosResponseView.setVideoUrl(tourVideos.getVideoManagement().getUrl());
        String vimeoUrl = tourVideos.getVideoManagement().getUrl();
        String vimeoId = "";
        if (vimeoUrl.contains("/")) {
            String[] videoIds = vimeoUrl.split("/");
            vimeoId = videoIds[2];
        }
        if (!vimeoId.isEmpty()) {
            quickTourVideosResponseView.setParsedVideoUrl(vimeoService.getVimeoUrlFromId(Long.parseLong(vimeoId)));
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_QUICK_TOUR_VIDEOS_FETCHED, quickTourVideosResponseView);

    }

    /**
     * Method for program library
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param programTypeIdOptional
     * @param searchName
     * @return
     */
    public Map<String, Object> getAllInstructorPrograms(int pageNo, int pageSize, String sortOrder, String sortBy, Optional<Long> programTypeIdOptional, Optional<String> searchName) {
        log.info("Get All Instructor programs starts");
        long start = new Date().getTime();
        long profilingStart;
        RequestParamValidator.pageSetup(pageNo, pageSize);
        if (!(sortBy.equalsIgnoreCase(SearchConstants.TITLE) || sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
        }
        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }
        User user = userComponents.getUser();
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - start));
        profilingStart = new Date().getTime();
        List<Long> programTypeIdList = new ArrayList<>();
        if (programTypeIdOptional.isPresent()) {
            Long programTypeId = programTypeIdOptional.get();
            if (!programTypeRepo.existsById(programTypeId)) {
                throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PROGRAM_TYPE_EMPTY, null);
            }
            programTypeIdList.add(programTypeId);
        } else {
            programTypeIdList = programTypeRepo.findByOrderByProgramTypeNameAsc().stream().map(ProgramTypes::getProgramTypeId).collect(Collectors.toList());
        }
        log.info("Program Type query : Time taken in millis : "+(new Date().getTime() - profilingStart));
        Sort sort = Sort.by(SearchConstants.CREATED_DATE);
        if (sortBy.equalsIgnoreCase(SearchConstants.TITLE)) {
            sort = Sort.by(SearchConstants.TITLE);
        }
        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            sort = sort.descending();
        }
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
        profilingStart = new Date().getTime();
        Page<Programs> programPage;
        if (searchName.isPresent() && !searchName.get().isEmpty()) {
            programPage = programRepository.findByOwnerUserIdAndProgramTypeProgramTypeIdInAndStatusAndTitleIgnoreCaseContaining(user.getUserId(), programTypeIdList, KeyConstants.KEY_PUBLISH, searchName.get(), pageRequest);
        } else {
            programPage = programRepository.findByOwnerUserIdAndProgramTypeProgramTypeIdInAndStatus(user.getUserId(), programTypeIdList, KeyConstants.KEY_PUBLISH, pageRequest);
        }
        log.info("Programs query : Time taken in millis : "+(new Date().getTime() - profilingStart));
        if (programPage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        long temp = System.currentTimeMillis();
        List<Long> programIdList = programPage.stream().map(Programs::getProgramId).collect(Collectors.toList());
        Map<Long, Long> offerCountMap = discountsService.getNoOfCurrentAvailableOffersOfProgramsForInstructor(programIdList);
        log.info(StringConstants.LOG_OFFER_COUNT_QUERY + (System.currentTimeMillis() - temp));
        profilingStart = new Date().getTime();
        List<ProgramTileModel> programTileModels = new ArrayList<>();
        for (Programs program : programPage) {
            programTileModels.add(constructProgramTileModel(program, offerCountMap));
        }
        log.info("Response construction : Time taken in millis : "+(new Date().getTime() - profilingStart));
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(KeyConstants.KEY_PROGRAMS, programTileModels);
        responseMap.put(KeyConstants.KEY_TOTAL_COUNT, programPage.getTotalElements());
        log.info("Get All instructor programs : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get All Instructor programs ends");
        return responseMap;
    }

    /**
     * Method to get Instructor program count by type
     * @return
     */
    public List<ProgramTypeCountView> getProgramCountByType() {
        User user = userComponents.getUser();

        List<String> statusList = Arrays.asList(KeyConstants.KEY_PUBLISH);
        List<ProgramTypeCountView> programTypeCountViewList = new ArrayList<>();

        long allCount = programRepository.countByOwnerAndStatusIn(user, statusList);

        if (allCount > 0) {
            ProgramTypeCountView allProgramTypeCountView = new ProgramTypeCountView();
            allProgramTypeCountView.setProgramTypeName(KeyConstants.KEY_ALL_PROGRAMS);
            allProgramTypeCountView.setCount(allCount);
            programTypeCountViewList.add(allProgramTypeCountView);
        }

        List<ProgramTypes> programTypes = programTypeRepo.findByOrderByProgramTypeNameAsc();
        for (ProgramTypes programType : programTypes) {
            long count = programRepository.countByOwnerAndProgramTypeProgramTypeIdAndStatusIn(user, programType.getProgramTypeId(), statusList);
            if (count > 0) {
                ProgramTypeCountView programTypeCountView = new ProgramTypeCountView();
                programTypeCountView.setProgramTypeId(programType.getProgramTypeId());
                programTypeCountView.setProgramTypeName(programType.getProgramTypeName());
                programTypeCountView.setCount(count);
                programTypeCountViewList.add(programTypeCountView);
            }
        }
        if (programTypeCountViewList.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        return programTypeCountViewList;
    }

    public void doCheckProgramPriceWithOffers(Programs program){
        double programPrice = program.getProgramPrices().getPrice();
        double offerPrice;
        List<DiscountOfferMapping> discountOfferMappings = discountOfferMappingRepository.findByProgramsProgramIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus(program.getProgramId(), true,DiscountsConstants.OFFER_ACTIVE);
        if(!discountOfferMappings.isEmpty()){
            for(DiscountOfferMapping discountOfferMapping : discountOfferMappings){
                OfferCodeDetail offerCodeDetail = offerCodeDetailRepository.findByOfferCodeId(discountOfferMapping.getOfferCodeDetail().getOfferCodeId());
                if(offerCodeDetail.getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_PAY_AS_YOU_GO)){
                    offerPrice = offerCodeDetail.getOfferPrice().getPrice();
                    if(offerPrice >= programPrice){
                        throw new ApplicationException(Constants.CONTENT_NEEDS_TO_BE_VALIDATE, MessageConstants.MSG_ERR_PGM_OFFER_UPDATE, null);
                    }
                }
            }
        }
    }
    
	private void doConstructProgramSubTypes(Programs program, ProgramModel model) {
		Optional<ProgramSubTypes> programSubTypeToStore = programSubTypeRepository
				.findById(model.getProgramSubTypeId());
		ValidationUtils.throwException(!programSubTypeToStore.isPresent(), "Invalid Program Sub Type Id",
				Constants.BAD_REQUEST);
		program.setProgramSubType(programSubTypeToStore.get());
	}
}