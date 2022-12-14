package com.fitwise.service.v2.program;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.VideoUploadStatus;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.entity.CircuitAndVoiceOverMapping;
import com.fitwise.entity.CircuitSchedule;
import com.fitwise.entity.Equipments;
import com.fitwise.entity.ExerciseSchedulers;
import com.fitwise.entity.InstructorRestActivity;
import com.fitwise.entity.ProgramPriceByPlatform;
import com.fitwise.entity.ProgramWiseGoal;
import com.fitwise.entity.Programs;
import com.fitwise.entity.RestActivity;
import com.fitwise.entity.RestMetric;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.WorkoutMapping;
import com.fitwise.entity.WorkoutSchedule;
import com.fitwise.entity.Workouts;
import com.fitwise.entity.discounts.DiscountOfferMapping;
import com.fitwise.entity.instructor.InstructorTierDetails;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.exception.ApplicationException;
import com.fitwise.program.daoImpl.ProgramsRepoImpl;
import com.fitwise.program.model.ProgramPlatformPriceResponseModel;
import com.fitwise.program.model.ProgramResponseModel;
import com.fitwise.program.model.RestActivityScheduleModel;
import com.fitwise.program.model.WorkoutResponseModel;
import com.fitwise.program.model.WorkoutScheduleModel;
import com.fitwise.repository.FlaggedExercisesSummaryRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.RestActivityRepository;
import com.fitwise.repository.SampleProgramsRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.WorkoutRepository;
import com.fitwise.repository.instructor.InstructorTierDetailsRepository;
import com.fitwise.repository.packaging.PackageProgramMappingRepository;
import com.fitwise.response.ProgramGoalsView;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.admin.FitwiseShareService;
import com.fitwise.utils.Convertions;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.view.SubscriptionPackagePackageIdAndTitleView;
import com.fitwise.view.discounts.ProgramDiscountMappingListResponseView;
import com.fitwise.view.discounts.ProgramDiscountMappingResponseView;
import com.fitwise.view.instructor.ProgramTypeWithSubTypeView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProgramV2Service {

    private final UserComponents userComponents;
    private final ProgramsRepoImpl programImpl;
    private final UserProfileRepository userProfileRepository;
    private final FitwiseUtils fitwiseUtils;
    private final PackageProgramMappingRepository packageProgramMappingRepository;
    private final FlaggedExercisesSummaryRepository flaggedExercisesSummaryRepository;
    private final WorkoutRepository workoutRepo;
    private final RestActivityRepository restActivityRepository;
    private final SubscriptionService subscriptionService;
    private final ProgramRepository programRepository;
    private final SampleProgramsRepository sampleProgramsRepository;
    private final FitwiseShareService fitwiseShareService;
    private final InstructorTierDetailsRepository instructorTierDetailsRepository;

    /**
     * Get Programs details for Instructor
     * @param programId
     * @return
     * @throws ApplicationException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
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

    /**
     * Get sample program L2 details
     * @param programId
     * @return
     * @throws ApplicationException
     */
    public ProgramResponseModel getSampleProgram(Long programId) {
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
     * Construct response for instructor program L2
     * @param program
     * @return
     * @throws ApplicationException
     */
    public ProgramResponseModel constructProgramModel(Programs program) {
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
				viewObj.setProgramSubType(program.getProgramSubType());
			}
			responseModel.setProgramType(viewObj);
		}
        if (program.getProgramExpertiseLevel() != null)
            responseModel.setProgramExpertise(program.getProgramExpertiseLevel());
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Basic details : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        profilingStartTimeMillis = new Date().getTime();
        if (program.getPromotion() != null && program.getPromotion().getVideoManagement() != null && program.getPromotion().getVideoManagement().getUrl() != null) {
            responseModel.setPromoVideoId(program.getPromotion().getVideoManagement().getUrl());
            responseModel.setPromotionId(program.getPromotion().getPromotionId());
            if (program.getPromotion().getVideoManagement().getThumbnail() != null) {
                responseModel.setPromotionThumbnailImageId(program.getPromotion().getVideoManagement().getThumbnail().getImageId());
                responseModel.setPromotionThumbnailImageUrl(program.getPromotion().getVideoManagement().getThumbnail().getImagePath());
            }
            responseModel.setPromotionDuration(program.getPromotion().getVideoManagement().getDuration());
            responseModel.setPromotionUploadStatus(program.getPromotion().getVideoManagement().getUploadStatus());
            /**
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
            DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
            responseModel.setProgramPrice(decimalFormat.format(program.getProgramPrice()));
            responseModel.setFormattedProgramPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(program.getProgramPrice()));
            List<ProgramPlatformPriceResponseModel> programPlatformPriceModels = new ArrayList<>();
            InstructorTierDetails instructorTierDetails = instructorTierDetailsRepository.findByUserAndActive(program.getOwner(), true);
            Double trainnrTax = 15.0;
            if (instructorTierDetails == null) {
                trainnrTax = instructorTierDetails.getTier().getTierTypeDetails().getProgramsFees();
            }
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
                if(disOffer.getOfferCodeDetail().getOfferPrice() != null) {
                    formattedPrice = fitwiseUtils.formatPrice(disOffer.getOfferCodeDetail().getOfferPrice().getPrice());
                }else {
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
                    String workoutThumbnail = null;
                    Long imageId = null;
                    if (instructorRestActivity != null) {
                        String restActivityName = instructorRestActivity.getActivityName();
                        if (!DBConstants.REST.equalsIgnoreCase(restActivityName)) {
                            restMetric = instructorRestActivity.getRestActivityToMetricMapping().getRestMetric();
                            String metric = restMetric.getRestMetric();
                            value = instructorRestActivity.getValue();
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
}