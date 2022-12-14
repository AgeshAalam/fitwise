package com.fitwise.service.instructor;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.CircuitSchedule;
import com.fitwise.entity.Equipments;
import com.fitwise.entity.ExerciseSchedulers;
import com.fitwise.entity.Exercises;
import com.fitwise.entity.Gender;
import com.fitwise.entity.InstructorProgramExperience;
import com.fitwise.entity.InstructorRestActivity;
import com.fitwise.entity.OtherExpertise;
import com.fitwise.entity.ProgramExpertiseMapping;
import com.fitwise.entity.ProgramPriceByPlatform;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.entity.Programs;
import com.fitwise.entity.Promotions;
import com.fitwise.entity.RestActivity;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserProgramGoalsMapping;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.VideoManagement;
import com.fitwise.entity.WorkoutCompletion;
import com.fitwise.entity.WorkoutSchedule;
import com.fitwise.entity.Workouts;
import com.fitwise.entity.YearsOfExpertise;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.payments.stripe.billing.StripeProductAndProgramMapping;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.subscription.SubscriptionStatus;
import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.service.VimeoService;
import com.fitwise.model.instructor.VideoVersioningModel;
import com.fitwise.model.instructor.VimeoVersioningModel;
import com.fitwise.program.model.ProgramPlatformPriceResponseModel;
import com.fitwise.repository.ExerciseRepository;
import com.fitwise.repository.GenderRepository;
import com.fitwise.repository.InstructorExperienceRepository;
import com.fitwise.repository.OtherExpertiseRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.ProgramTypeRepository;
import com.fitwise.repository.PromotionRepository;
import com.fitwise.repository.RestActivityRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserProgramGoalsMappingRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.YearsOfExpertiseRepository;
import com.fitwise.repository.member.WorkoutCompletionRepository;
import com.fitwise.repository.packaging.PackageProgramMappingRepository;
import com.fitwise.repository.payments.stripe.billing.StripeProductAndProgramMappingRepository;
import com.fitwise.repository.subscription.PackageSubscriptionRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.UserService;
import com.fitwise.service.itms.FitwiseITMSUploadEntityService;
import com.fitwise.service.payments.appleiap.IAPServerNotificationService;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.service.video.VideoUploadService;
import com.fitwise.specifications.jpa.SubscriptionPackageJpa;
import com.fitwise.utils.Convertions;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.mail.AsyncMailerUtil;
import com.fitwise.utils.parsing.VideoVersioningModelParsing;
import com.fitwise.view.InstructorExperienceView;
import com.fitwise.view.ProgramExpertiseLevelsView;
import com.fitwise.view.ProgramsResponseView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.UpdateProfileView;
import com.fitwise.view.admin.MemberPackageHistoryView;
import com.fitwise.view.circuit.CircuitScheduleResponseView;
import com.fitwise.view.instructor.ClientDetailsResponseView;
import com.fitwise.view.instructor.InstructorProgramDetailsResponseView;
import com.fitwise.view.instructor.InstructorProgramsResponseView;
import com.fitwise.view.instructor.InstructorWorkoutDetailResponseView;
import com.fitwise.view.instructor.InstructorWorkoutResponseView;
import com.fitwise.view.instructor.OtherExpertiseView;
import com.google.gson.internal.LinkedTreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * The Class InstructorProgramService.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorProgramService {

    /**
     * The validation service.
     */
    @Autowired
    ValidationService validationService;

    /**
     * The vimeo service.
     */
    @Autowired
    VimeoService vimeoService;


    /**
     * The user service.
     */
    @Autowired
    UserService userService;

    /**
     * The program repository.
     */
    @Autowired
    ProgramRepository programRepository;

    /**
     * The user profile repository.
     */
    @Autowired
    UserProfileRepository userProfileRepository;

    /**
     * The years of expertise repository.
     */
    @Autowired
    YearsOfExpertiseRepository yearsOfExpertiseRepository;

    /**
     * The program type repository.
     */
    @Autowired
    ProgramTypeRepository programTypeRepository;

    /**
     * The instructor experience repository.
     */
    @Autowired
    InstructorExperienceRepository instructorExperienceRepository;

    /**
     * The other expertise repository.
     */
    @Autowired
    OtherExpertiseRepository otherExpertiseRepository;

    /**
     * The gender repository.
     */
    @Autowired
    GenderRepository genderRepository;

    /**
     * The user program goals mapping repository.
     */
    @Autowired
    UserProgramGoalsMappingRepository userProgramGoalsMappingRepository;

    /**
     * The user components.
     */
    @Autowired
    UserComponents userComponents;

    @Autowired
    FitwiseQboEntityService fitwiseQboEntityService;

    @Autowired
    RestActivityRepository restActivityRepository;

    @Autowired
    ExerciseRepository exerciseRepository;

    @Autowired
    PromotionRepository promotionRepository;

    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    ProgramSubscriptionRepo programSubscriptionRepo;

    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    WorkoutCompletionRepository workoutCompletionRepository;

    @Autowired
    VideoUploadService videoUploadService;

    @Autowired
    IAPServerNotificationService iapServerNotificationService;

    @Autowired
    FitwiseITMSUploadEntityService fitwiseITMSUploadEntityService;
    @Autowired
    StripeProductAndProgramMappingRepository stripeProductAndProgramMappingRepository;
    @Autowired
    PackageSubscriptionRepository packageSubscriptionRepository;
    @Autowired
    PackageProgramMappingRepository packageProgramMappingRepository;

    private final AsyncMailerUtil asyncMailerUtil;

    private final SubscriptionPackageJpa subscriptionPackageJpa;

    /**
     * Method used to get the programs list published by an Instructor.
     *
     * @return the instructor programs
     */
    public ResponseModel getInstructorPrograms() {
        User user = userComponents.getUser();
        // Checking whether the instructor Id from the Client is valid
        validationService.validateInstructorId(user.getUserId());
        List<Programs> programsList = validationService.programRepository.findByOwnerUserId(user.getUserId());
        List<InstructorProgramsResponseView> instructorProgramsList = new ArrayList<>();
        /*
         * Getting the list of programs published by the user
         */
        for (Programs program : programsList) {
            if (program != null && program.isPublish()) {
                InstructorProgramsResponseView programsResponseView = new InstructorProgramsResponseView();
                programsResponseView.setProgramId(program.getProgramId());
                programsResponseView.setProgramTitle(program.getTitle());
                if (program.getDuration() != null && program.getDuration().getDuration() != null)
                    programsResponseView.setProgramDuration(program.getDuration().getDuration());
                if (program.getCreatedDate() != null){
                    programsResponseView.setProgramPublishedDate(program.getCreatedDate());
                    programsResponseView.setProgramPublishedDateFormatted(fitwiseUtils.formatDate(program.getCreatedDate()));
                }
                if (program.getImage() != null)
                    programsResponseView.setProgramThumbnail(program.getImage().getImagePath());
                instructorProgramsList.add(programsResponseView);

            }
        }
        /*
         * Getting 'Get Started' program
         */
        // Set<Exercises> exercises
        HashMap<String, Object> resMap = new HashMap<>();
        resMap.put(KeyConstants.KEY_SAMPLE_PROGRAM, null);
        resMap.put(KeyConstants.KEY_QUICK_TOUR_VIDEO, null);
        resMap.put(KeyConstants.KEY_PUBLISHED_PROGRAMS_SIZE, instructorProgramsList.size());
        resMap.put(KeyConstants.KEY_PUBLISHED_PROGRAMS, instructorProgramsList);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_INSTRUCTOR_PROGRAMS_FETCHED, resMap);
    }

    /**
     * Method to un publish a program.
     *
     * @param programId the program id
     * @return the response model
     */
    public ResponseModel unPublishProgram(long programId) {
        log.info("Un-publish program starts.");
        long apiStartTimeMillis = new Date().getTime();
    	User user = userComponents.getUser();
        validationService.validateIfProgramBelongsToInstructor(user.getUserId(), programId);
        Programs program = programRepository.findByProgramId(programId);
        log.info("Get user, validate program and query to get program : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        if (program.isPublish()) {
            /**
             * If the program is subscribed by other users, cancelling the subscriptions before un-publishing it
             */
            List<ProgramSubscription> programSubscriptions = subscriptionService.getPaidSubscriptionsOfProgram(programId);
            log.info("Query to get program subscriptions : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            for (ProgramSubscription programSubscription : programSubscriptions) {
                if (programSubscription != null && programSubscription.isAutoRenewal() && programSubscription.getSubscribedViaPlatform() != null) {
                    if (programSubscription.getSubscribedViaPlatform().getPlatformTypeId() == 2) {

                        profilingEndTimeMillis = new Date().getTime();
                        // Subscribed via Apple IAP
                        iapServerNotificationService.cancelSubscription(programId, programSubscription.getSubscribedViaPlatform().getPlatformTypeId(), programSubscription.getUser());
                        log.info("Cancel subscriptions in apple IAP : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                    } else {
                        profilingEndTimeMillis = new Date().getTime();
                        // Subscribed via Authorize.net
                        subscriptionService.cancelRecurringProgramSubscription(programId, programSubscription.getSubscribedViaPlatform().getPlatformTypeId(), programSubscription.getUser(), false);
                        log.info("Cancel subscriptions in Authorize.net or stripe : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
                    }
                }

            }

            profilingEndTimeMillis = new Date().getTime();
            /*Sending mail to subscribers*/
            asyncMailerUtil.triggerUnpublishProgramMail(programSubscriptions);
            log.info("Send mail to subscribers : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();

            program.setPublish(false);
            program.setStatus(InstructorConstant.UNPUBLISH);
            programRepository.save(program);
            log.info("Query to save program : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();

            //Marking Stripe product mapping as inactive
            StripeProductAndProgramMapping stripeProductMapping = stripeProductAndProgramMappingRepository.findByProgramProgramIdAndIsActive(program.getProgramId(), true);
            if(stripeProductMapping != null){
                stripeProductMapping.setActive(false);
                stripeProductAndProgramMappingRepository.save(stripeProductMapping);
            }
            log.info("Queries to get and save stripe product mapping : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            profilingEndTimeMillis = new Date().getTime();

            // Unpublish/Block From App store by changing Ready For Sale flag as false
            //fitwiseITMSUploadEntityService.unPublish(program);
            log.info("Un-publish program in ITMS tool : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PROGRAM_NOT_PUBLISHED_YET, MessageConstants.ERROR);
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Un-publish program ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROGRAM_UNPUBLISHED, null);
    }

    /**
     * Method to get the program details and display in Instructor mobile app.
     *
     * @param programId the program id
     * @return the instructor program details
     * @throws ApplicationException     the application exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws KeyStoreException        the key store exception
     * @throws KeyManagementException   the key management exception
     */
    public ResponseModel getInstructorProgramDetails(long programId) throws ApplicationException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        validationService.validateProgramIdBlocked(programId);
        int trialCount = 0;
        Programs program = validationService.programRepository.findByProgramId(programId);
        InstructorProgramDetailsResponseView programDetail = new InstructorProgramDetailsResponseView();
        programDetail.setProgramId(program.getProgramId());
        if (program.getImage() != null)
            programDetail.setProgramThumbnail(program.getImage().getImagePath());
        if (program.getPromotion() != null && program.getPromotion().getVideoManagement() != null) {
            String vimeoUrl = program.getPromotion().getVideoManagement().getUrl();
            String vimeoId = "";
            if (vimeoUrl.contains("/")) {
                String[] videoIds = vimeoUrl.split("/");
                vimeoId = videoIds[2];
            }
            if (!vimeoId.isEmpty())
                programDetail.setProgramPromoUrl(vimeoService.getVimeoUrlFromId(Long.parseLong(vimeoId)));
        }
        programDetail.setProgramTitle(program.getTitle());
        programDetail.setProgramDescription(program.getDescription());

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        programDetail.setProgramPrice(decimalFormat.format(program.getProgramPrice()));
        programDetail.setFormattedProgramPrice(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(program.getProgramPrice()));
        ProgramPlatformPriceResponseModel programPlatformPriceResponseModel = new ProgramPlatformPriceResponseModel();
        List<ProgramPlatformPriceResponseModel> programPlatformPriceResponseModels = new ArrayList<>();
        for (ProgramPriceByPlatform programPriceByPlatform : program.getProgramPriceByPlatforms()) {
            programPlatformPriceResponseModel.setProgramPriceByPlatformId(programPriceByPlatform.getProgramPriceByPlatformId());
            programPlatformPriceResponseModel.setPrice(programPriceByPlatform.getPrice());
            programPlatformPriceResponseModels.add(programPlatformPriceResponseModel);
        }
        programDetail.setProgramPlatformPriceResponseModels(programPlatformPriceResponseModels);
        if (program.getProgramType() != null)
            programDetail.setProgramType(program.getProgramType().getProgramTypeName());
        List<WorkoutSchedule> workoutSchedules = program.getWorkoutSchedules();
        List<InstructorWorkoutResponseView> workoutResponseViews = new ArrayList<>();
        List<Equipments> programEquipments = new ArrayList<>();
        /*
         * Iterating through the workoutScheduleResponseViews to get the duration, total Exercise count,
         * workout completion status and Equipments list.
         */
        for (WorkoutSchedule workoutSchedule : workoutSchedules) {
            if (workoutSchedule != null) {
                InstructorWorkoutResponseView workoutResponseView = new InstructorWorkoutResponseView();
                workoutResponseView.setWorkoutOrder(workoutSchedule.getOrder());
                workoutResponseView.setDay(Convertions.getDayText(workoutSchedule.getOrder()));
                if (workoutSchedule.isRestDay()) {
                    workoutResponseView.setRestDay(true);

                    //Rest Activity data in workout schedule response
                    InstructorRestActivity instructorRestActivity = workoutSchedule.getInstructorRestActivity();
                    String title = DBConstants.REST;
                    String workoutThumbnail = null;
                    if (instructorRestActivity != null) {
                        String restActivityName = instructorRestActivity.getActivityName();
                        if (!DBConstants.REST.equalsIgnoreCase(restActivityName)) {
                            String metric = instructorRestActivity.getRestActivityToMetricMapping().getRestMetric().getRestMetric();
                            title = restActivityName + " - " + instructorRestActivity.getValue() + " " + metric;
                        }

                        RestActivity restActivity = instructorRestActivity.getRestActivityToMetricMapping().getRestActivity();
                        if (restActivity.getImage() != null) {
                            workoutThumbnail = restActivity.getImage().getImagePath();
                        }
                    } else {
                        RestActivity restActivity = restActivityRepository.findByRestActivity(DBConstants.REST);
                        if (restActivity.getImage() != null) {
                            workoutThumbnail = restActivity.getImage().getImagePath();
                        }
                    }
                    workoutResponseView.setWorkoutTitle(title);
                    workoutResponseView.setWorkoutThumbnail(workoutThumbnail);

                } else {
                    if (workoutSchedule.getWorkout() != null) {
                        Workouts workout = workoutSchedule.getWorkout();
                        // Setting trial as true to first two workouts
                        // If second workout is a rest, then third should be treated as Trial
                        if (trialCount < 2) {
                            trialCount++;
                            workoutResponseView.setTrail(true);
                        } else {
                            workoutResponseView.setTrail(false);
                        }
                        workoutResponseView.setWorkoutId(workout.getWorkoutId());
                        workoutResponseView.setWorkoutTitle(workout.getTitle());
                        if (workout.getImage() != null)
                            workoutResponseView.setWorkoutThumbnail(workout.getImage().getImagePath());
                        Set<CircuitSchedule> circuitSchedules = workout.getCircuitSchedules();
                        int totalExercises = 0;
                        int workoutDuration = 0;
                        boolean isVideoProcessingPending = false;
                        for (CircuitSchedule circuitSchedule : circuitSchedules) {
                            long circuitDuration = 0;
                            if (circuitSchedule.isRestCircuit()) {
                                circuitDuration = circuitSchedule.getRestDuration();
                            } else {
                                Set<ExerciseSchedulers> exerciseSchedulers = circuitSchedule.getCircuit().getExerciseSchedules();
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
                                        if (exerciseScheduler.getExercise().getEquipments() != null)
                                            programEquipments.addAll(exerciseScheduler.getExercise().getEquipments());
                                        totalExercises++;
                                    } else {
                                        exerciseDuration = exerciseScheduler.getWorkoutRestVideo().getRestTime();

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
                            }
                            workoutDuration += circuitDuration;
                        }
                        //Setting total number of exercises count
                        workoutResponseView.setExercisesCount(totalExercises);
                        workoutResponseView.setWorkoutDuration(workoutDuration);
                        workoutResponseView.setVideoProcessingPending(isVideoProcessingPending);
                    }
                }
                workoutResponseViews.add(workoutResponseView);
            }
        }

        /*
         *Removing duplicates from the Equipments list
         */
        List<Equipments> equipmentsList = programEquipments.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingLong(Equipments::getEquipmentId))),
                        ArrayList::new));

        // Setting program expertise level
        programDetail.setProgramLevel(program.getProgramExpertiseLevel().getExpertiseLevel());

        // Setting program duration
        programDetail.setProgramDuration(program.getDuration().getDuration());

        // Setting equipments
        programDetail.setEquipments(equipmentsList);

        // Sorting workouts based on order
        workoutResponseViews.sort(Comparator.comparing(InstructorWorkoutResponseView::getWorkoutOrder));

        // Setting workout data
        programDetail.setWorkouts(workoutResponseViews);

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROGRAM_DETAIL_FETCHED, programDetail);
    }

    /**
     * Method to get workout details from instructor app.
     *
     * @param workoutId the workout id
     * @return the instructor workout details
     * @throws ApplicationException     the application exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws KeyStoreException        the key store exception
     * @throws KeyManagementException   the key management exception
     */
    public ResponseModel getInstructorWorkoutDetails(long workoutId) throws ApplicationException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        Workouts workout = validationService.validateWorkoutId(workoutId);
        InstructorWorkoutDetailResponseView responseView = new InstructorWorkoutDetailResponseView();
        responseView.setWorkoutId(workout.getWorkoutId());
        long duration = 0;
        boolean isWorkoutVideoProcessingPending = false;
        if (workout.getImage() != null)
            responseView.setWorkoutThumbnail(workout.getImage().getImagePath());

        if (workout.getCircuitSchedules() != null && !workout.getCircuitSchedules().isEmpty()) {
            List<CircuitScheduleResponseView> circuitScheduleViewList = new ArrayList<>();

            for (CircuitSchedule circuitSchedule : workout.getCircuitSchedules()) {
                CircuitScheduleResponseView circuitScheduleView = new CircuitScheduleResponseView();
                circuitScheduleView.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
                circuitScheduleView.setOrder(circuitSchedule.getOrder());


                long circuitDuration = 0;

                boolean isRestCircuit = circuitSchedule.isRestCircuit();
                circuitScheduleView.setRestCircuit(isRestCircuit);

                if (isRestCircuit) {
                    circuitDuration = circuitSchedule.getRestDuration();
                } else {
                    circuitScheduleView.setCircuitId(circuitSchedule.getCircuit().getCircuitId());
                    circuitScheduleView.setCircuitTitle(circuitSchedule.getCircuit().getTitle());
                    Long repeat = circuitSchedule.getRepeat();
                    circuitScheduleView.setRepeat(repeat);
                    Long restBetweenRepeat = circuitSchedule.getRestBetweenRepeat();
                    circuitScheduleView.setRestBetweenRepeat(restBetweenRepeat);

                    Set<ExerciseSchedulers> exerciseSchedules = circuitSchedule.getCircuit().getExerciseSchedules();

                    boolean isCircuitVideoProcessingPending = false;
                    int exerciseCount = 0;
                    List<String> exerciseThumbnails = new ArrayList<>();
                    for (ExerciseSchedulers schedule : exerciseSchedules) {
                        long exerciseDuration = 0;

                        if (schedule.getExercise() != null) {
                            if (schedule.getExercise().getVideoManagement() != null) {
                                exerciseDuration = schedule.getExercise().getVideoManagement().getDuration();

                                if (schedule.getLoopCount() != null && schedule.getLoopCount() > 0) {
                                    //Repeat Count Change : Since repeat count is changes as no of times video should play
                                    exerciseDuration = exerciseDuration * schedule.getLoopCount();
                                }

                                if (schedule.getExercise().getVideoManagement().getThumbnail() != null) {
                                    exerciseThumbnails.add(schedule.getExercise().getVideoManagement().getThumbnail().getImagePath());
                                }

                                if (fitwiseUtils.isVideoProcessingPending(schedule.getExercise().getVideoManagement())) {
                                    isWorkoutVideoProcessingPending = true;
                                    isCircuitVideoProcessingPending = true;
                                }
                            }
                            exerciseCount++;
                        } else {
                            exerciseDuration = schedule.getWorkoutRestVideo().getRestTime();
                        }
                        circuitDuration += exerciseDuration;
                    }

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

                    circuitScheduleView.setExerciseCount(exerciseCount);
                    circuitScheduleView.setVideoProcessingPending(isCircuitVideoProcessingPending);

                    circuitScheduleView.setExerciseThumbnails(exerciseThumbnails);
                }

                duration += circuitDuration;
                circuitScheduleView.setDuration(circuitDuration);

                circuitScheduleViewList.add(circuitScheduleView);
            }

            Collections.sort(circuitScheduleViewList, Comparator.comparing(CircuitScheduleResponseView::getOrder));
            responseView.setCircuitSchedules(circuitScheduleViewList);
            responseView.setWorkoutDuration(duration);
            responseView.setVideoProcessingPending(isWorkoutVideoProcessingPending);
        }

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_WORKOUT_DETAIL_FETCHED, responseView);
    }

    /**
     * To update Instructor profile.
     *
     * @param updateProfileView the update profile view
     * @return ResponseModel
     */
    public ResponseModel updateInstructorProfile(UpdateProfileView updateProfileView) {
        ResponseModel responseModel = new ResponseModel();
        User user = userComponents.getUser();
        UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId());
        if (ValidationUtils.isEmptyString(updateProfileView.getFirstName()) || updateProfileView.getFirstName().length() > 50
                || !validationService.isStringContainsOnlyAlphabets(updateProfileView.getFirstName())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FIRST_NAME_ERROR,
                    Constants.RESPONSE_INVALID_DATA);
        }
        if (ValidationUtils.isEmptyString(updateProfileView.getLastName()) || updateProfileView.getLastName().length() > 50
                || !validationService.isStringContainsOnlyAlphabets(updateProfileView.getLastName())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_LAST_NAME_ERROR,
                    Constants.RESPONSE_INVALID_DATA);
        }
        userProfile.setFirstName(updateProfileView.getFirstName());
        userProfile.setLastName(updateProfileView.getLastName());
        userProfile.setBiography(updateProfileView.getAbout());
        Gender gender = genderRepository.findByGenderId(updateProfileView.getGenderId());
        if (gender == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_GENDER_ERROR,
                    Constants.RESPONSE_INVALID_DATA);
        }
        userProfile.setGender(gender);
        userProfileRepository.save(userProfile);
        List<InstructorExperienceView> instructorExperienceViews = updateProfileView.getExpertise();
        updateExperience(user, instructorExperienceViews);
        saveOtherExpertise(updateProfileView.getOtherExpertise());
        userService.addTaxId(updateProfileView.getTaxIdView());
        fitwiseQboEntityService.createOrUpdateQboUser(user, SecurityFilterConstants.ROLE_INSTRUCTOR);
        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_USER_PROFILE_SAVED);
        return responseModel;
    }

    /**
     * To save other expertise types.
     *
     * @param otherExpertiseViewList the other expertise view list
     */
    public ResponseModel saveOtherExpertise(List<OtherExpertiseView> otherExpertiseViewList) {
        log.info("Add Other Expertise starts");
        long start = new Date().getTime();
        long profilingStart;
        long profilingEnd;
        ResponseModel responseModel = new ResponseModel();
        User user = userComponents.getUser();
        List<OtherExpertise> otherExpertisesList = otherExpertiseRepository.findByUserUserId(user.getUserId());

        profilingStart = new Date().getTime();
        for (int i = 0; i < otherExpertiseViewList.size(); i++) {
            for (int j = i + 1; j < otherExpertiseViewList.size(); j++) {
                if (otherExpertiseViewList.get(i).getProgramType().equalsIgnoreCase(otherExpertiseViewList.get(j).getProgramType())) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_DUPLICATES_NOT_ALLOWED_PROGRAM_TYPES, MessageConstants.ERROR);
                }
            }
        }

        profilingEnd = new Date().getTime();
        log.info("Validating duplicates in other expertise : time taken in millis : "+(profilingEnd-profilingStart));

        profilingStart = new Date().getTime();
        for (OtherExpertise otherExpertise : otherExpertisesList) {
            otherExpertiseRepository.delete(otherExpertise);
        }
        profilingEnd = new Date().getTime();
        log.info("Deleting existing expertise : time taken in millis : "+(profilingEnd-profilingStart));


        profilingStart = new Date().getTime();
        for (OtherExpertiseView otherExpertiseView : otherExpertiseViewList) {

            YearsOfExpertise yearsOfExpertise = yearsOfExpertiseRepository.findByExperienceId(otherExpertiseView.getNoOfYearsId());
            if (yearsOfExpertise == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INSTRUCTOR_EXPERIENCE_ID_NOT_FOUND, MessageConstants.ERROR);
            }

            OtherExpertise otherExpertise = new OtherExpertise();
            otherExpertise.setExpertiseType(otherExpertiseView.getProgramType());
            otherExpertise.setExperience(yearsOfExpertise);
            otherExpertise.setUser(user);
            otherExpertiseRepository.save(otherExpertise);

        }
        profilingEnd = new Date().getTime();
        log.info("DB update for other expertise : time taken in millis : "+(profilingEnd-profilingStart));

        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_OTHER_EXPERTISE_SAVED);
        profilingEnd = new Date().getTime();
        log.info("Add Other expertise : total time taken in millis : "+(profilingEnd-start));
        log.info("Add other expertise ends");
        return responseModel;


    }

    /**
     * update Instructor Experience.
     *
     * @param instructorExperienceViewList the instructor experience view list
     * @param user                         the user
     */
    public void updateExperience(User user, List<InstructorExperienceView> instructorExperienceViewList) {
        long profilingStart;
        long profilingEnd;

        List<InstructorProgramExperience> instructorProgramExperienceList = instructorExperienceRepository.findByUserUserId(user.getUserId());
        profilingStart = new Date().getTime();
        for (InstructorProgramExperience instructorProgramExperience : instructorProgramExperienceList) {
            instructorExperienceRepository.delete(instructorProgramExperience);
        }
        profilingEnd = new Date().getTime();
        log.info("Delete existing experience : Time take in millis : "+(profilingEnd-profilingStart));

        profilingStart = new Date().getTime();
        for (InstructorExperienceView instructorExperienceView : instructorExperienceViewList) {

            ProgramTypes programTypes = programTypeRepository.findByProgramTypeId(instructorExperienceView.getProgramTypeId());
            YearsOfExpertise yearsOfExpertise = yearsOfExpertiseRepository.findByExperienceId(instructorExperienceView.getNoOfYearsId());
            if (programTypes == null || yearsOfExpertise == null) {
                throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_INVALID_PROGRAM_EXPERIENCE_ID,
                        MessageConstants.ERROR);

            } else {

                InstructorProgramExperience instructorProgramExperience = instructorExperienceRepository.findByProgramTypeProgramTypeIdAndUserUserId(programTypes.getProgramTypeId(), user.getUserId());
                if (instructorProgramExperience == null) {
                    instructorProgramExperience = new InstructorProgramExperience();
                }

                instructorProgramExperience.setProgramType(programTypes);
                instructorProgramExperience.setExperience(yearsOfExpertise);

                instructorProgramExperience.setUser(user);
                instructorExperienceRepository.save(instructorProgramExperience);
            }
        }
        profilingEnd = new Date().getTime();
        log.info("DB update - instructor experience : Time take in millis : "+(profilingEnd-profilingStart));
    }

    /**
     * To get client details.
     *
     * @param userId
     * @return ResponseModel
     */
    public ClientDetailsResponseView getClientDetails(Long userId) {
        User client = validationService.validateMemberId(userId);

        ClientDetailsResponseView clientDetailsResponseView = new ClientDetailsResponseView();
        UserProfile userProfile = userProfileRepository.findByUserUserId(client.getUserId());
        clientDetailsResponseView.setFirstName(userProfile.getFirstName());
        clientDetailsResponseView.setLastName(userProfile.getLastName());
        clientDetailsResponseView.setBio(userProfile.getBiography());
        if (userProfile.getProfileImage() != null) {
            clientDetailsResponseView.setProfileImage(userProfile.getProfileImage().getImagePath());
        }


        UserRole userRole = userRoleRepository.findByName(KeyConstants.KEY_MEMBER);
        boolean isActive = fitwiseUtils.isUserActive(userProfile.getUser(), userRole);
        String status = isActive ? KeyConstants.KEY_ACTIVE_CAMEL_CASE : KeyConstants.KEY_INACTIVE_CAMEL_CASE;
        clientDetailsResponseView.setUserStatus(status);

        List<UserProgramGoalsMapping> userProgramGoalsMappingList = userProgramGoalsMappingRepository.findByUserUserId(userId);

        //Goals of client
        List<String> goals = userProgramGoalsMappingList.stream()
                .map(userProgramGoalsMapping -> userProgramGoalsMapping.getProgramExpertiseGoalsMapping().getProgramGoals().getProgramGoal())
                .distinct()
                .collect(Collectors.toList());

        clientDetailsResponseView.setProgramGoalsList(goals);

        //ExpertiseLevels of client
        List<ProgramExpertiseLevelsView> programExpertiseLevelsViewList = getExpertiseLevelsOfClient(userProgramGoalsMappingList);
        clientDetailsResponseView.setProgramExpertiseLevels(programExpertiseLevelsViewList);

        //Subscribed programs of client
        List<ProgramsResponseView> programsResponseViewList = getSubscribedProgramsOfClient(client);
        clientDetailsResponseView.setPrograms(programsResponseViewList);

        return clientDetailsResponseView;

    }

    /**
     * ExpertiseLevels of client
     *
     * @param userProgramGoalsMappingList
     * @return
     */
    private List<ProgramExpertiseLevelsView> getExpertiseLevelsOfClient(List<UserProgramGoalsMapping> userProgramGoalsMappingList) {
        List<ProgramExpertiseMapping> programExpertiseMappingList = userProgramGoalsMappingList.stream()
                .map(userProgramGoalsMapping -> userProgramGoalsMapping.getProgramExpertiseGoalsMapping().getProgramExpertiseMapping())
                .collect(Collectors.toList());

        programExpertiseMappingList = programExpertiseMappingList.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<ProgramExpertiseMapping>(comparingLong(ProgramExpertiseMapping::getProgramExpertiseMappingId))), ArrayList::new));

        List<ProgramExpertiseLevelsView> programExpertiseLevelsViewList = new ArrayList<>();
        for (ProgramExpertiseMapping programExpertiseMapping : programExpertiseMappingList) {
            ProgramExpertiseLevelsView programExpertiseLevelsView = new ProgramExpertiseLevelsView();
            programExpertiseLevelsView.setProgramType(programExpertiseMapping.getProgramType().getProgramTypeName());
            programExpertiseLevelsView.setExpertiseLevel(programExpertiseMapping.getExpertiseLevel().getExpertiseLevel());
            programExpertiseLevelsViewList.add(programExpertiseLevelsView);
        }
        return programExpertiseLevelsViewList;
    }

    /**
     * @param client
     * @return
     */
    private List<ProgramsResponseView> getSubscribedProgramsOfClient(User client) {
        User user = userComponents.getUser();

        List<String> statusList = new ArrayList<>();
        statusList.add(KeyConstants.KEY_TRIAL);
        statusList.add(KeyConstants.KEY_PAID);
        statusList.add(KeyConstants.KEY_PAYMENT_PENDING);

        //All trial and paid subscriptions of instructor
        List<ProgramSubscription> totalProgramSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(user.getUserId(), client.getUserId(), statusList);

        List<ProgramsResponseView> programsResponseViewList = new ArrayList<>();
        for (ProgramSubscription programSubscription : totalProgramSubscriptions) {
            Programs programs = programSubscription.getProgram();

            ProgramsResponseView programsResponseView = new ProgramsResponseView();
            programsResponseView.setProgramId(programs.getProgramId());
            programsResponseView.setProgramTitle(programs.getTitle());
            if (programs.getImage() != null) {
                programsResponseView.setThumbnailUrl(programs.getImage().getImagePath());
            }

            /**
             * To show program subscription status - TRIAL/SUBSCRIBED/EXPIRED
             */
            String subscriptionStatus = subscriptionService.getMemberProgramSubscriptionStatus(programSubscription).getSubscriptionStatusName();
            if (subscriptionStatus != null && !subscriptionStatus.isEmpty()) {
                if (subscriptionStatus.equals(KeyConstants.KEY_PAYMENT_PENDING) || subscriptionStatus.equals(KeyConstants.KEY_PAID)) {
                    programsResponseView.setSubscriptionStatus(KeyConstants.KEY_SUBSCRIBED);
                } else if (subscriptionStatus.equals(KeyConstants.KEY_TRIAL)) {
                    programsResponseView.setSubscriptionStatus(KeyConstants.KEY_TRIAL);
                } else if (subscriptionStatus.equals(KeyConstants.KEY_EXPIRED)) {
                    programsResponseView.setSubscriptionStatus(KeyConstants.KEY_EXPIRED);
                }
            }



            programsResponseView.setSubscribedDate(programSubscription.getSubscribedDate());
            String subscribedDateString = fitwiseUtils.formatDate(programSubscription.getSubscribedDate());
            programsResponseView.setSubscribedDateFormatted(subscribedDateString);

            int duration = Math.toIntExact(programs.getDuration().getDuration());
            programsResponseView.setDuration(duration);

            List<WorkoutCompletion> workoutCompletionList = workoutCompletionRepository.findByMemberUserIdAndProgramProgramId(client.getUserId(), programs.getProgramId());
            int completedWorkouts = workoutCompletionList.size();
            programsResponseView.setCompletedDays(completedWorkouts);

            if (completedWorkouts > 0) {
                Date startedDate = workoutCompletionList.get(0).getCompletedDate();
                programsResponseView.setStartedDate(startedDate);
                programsResponseView.setStartedDateFormatted(fitwiseUtils.formatDate(startedDate));
            }

            long progressPercent;
            String completionStatus;
            if (completedWorkouts == duration) {
                completionStatus = KeyConstants.KEY_COMPLETED;
                Date completedDate = workoutCompletionList.get(workoutCompletionList.size() - 1).getCompletedDate();
                programsResponseView.setCompletedDate(completedDate);
                programsResponseView.setCompletedDateFormatted(fitwiseUtils.formatDate(completedDate));
                progressPercent = 100;
            } else {
                completionStatus = KeyConstants.KEY_IN_PROGRESS_WITH_SPACE;
                progressPercent = (completedWorkouts * 100) / duration;
            }
            programsResponseView.setStatus(completionStatus);
            programsResponseView.setProgressPercent(progressPercent);

            programsResponseViewList.add(programsResponseView);
        }
        return programsResponseViewList;
    }


    /**
     * Creating the video version for the mentioned type and the corresponding video
     *
     * @param videoVersioningModel
     * @param type
     * @return
     */
    public VideoVersioningModel createVideoVersion(VideoVersioningModel videoVersioningModel, String type, boolean isByAdmin) {
        log.info("Creating version for the " + type + " id " + videoVersioningModel.getVersioningEntityId());
        User user = userComponents.getUser();
        String videoUrl = null;
        try {
            if (type.equalsIgnoreCase(InstructorConstant.VIDEO_TYPE_EXERCISE_VIDEO)) {
                Exercises exercises;
                if(isByAdmin){
                    exercises = exerciseRepository.findByExerciseIdAndIsByAdmin(videoVersioningModel.getVersioningEntityId(),true);
                }else{
                    exercises = exerciseRepository.findByExerciseIdAndOwnerUserId(videoVersioningModel.getVersioningEntityId(), user.getUserId());
                }
                videoUrl = exercises.getVideoManagement().getUrl();
            } else if (type.equalsIgnoreCase(InstructorConstant.VIDEO_TYPE_EXERCISE_SUPPORT_VIDEO)) {
                Exercises exercises;
                if(isByAdmin){
                    exercises = exerciseRepository.findByExerciseIdAndIsByAdmin(videoVersioningModel.getVersioningEntityId(),true);
                }else{
                    exercises = exerciseRepository.findByExerciseIdAndOwnerUserId(videoVersioningModel.getVersioningEntityId(), user.getUserId());
                }
                videoUrl = exercises.getSupportVideoManagement().getUrl();
            } else if (type.equalsIgnoreCase(InstructorConstant.VIDEO_TYPE_PROMO_VIDEO)) {
                Promotions promotions = promotionRepository.findByPromotionId(videoVersioningModel.getVersioningEntityId());
                if (promotions.getVideoManagement().getOwner().getUserId().longValue() == user.getUserId().longValue()) {
                    videoUrl = promotions.getVideoManagement().getUrl();
                }
            }
        } catch (Exception exception) {
            videoUrl = null;
        }
        if (videoUrl == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_VIDEO_VERSION_INVALID_PARAM, null);
        }
        VimeoVersioningModel vimeoModel = VideoVersioningModelParsing.parseToVimeoModel(videoVersioningModel);
        try {
            LinkedTreeMap<String, Object> res = vimeoService.crateVideoVersion(videoUrl, vimeoModel);
            LinkedTreeMap<String, Object> uploadJsonObject = (LinkedTreeMap) res.get(KeyConstants.KEY_UPLOAD);
            String uploadUrl = (String) uploadJsonObject.get(KeyConstants.KEY_UPLOAD_LINK);
            videoVersioningModel.setUploadLink(uploadUrl);
        } catch (Exception exception) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_VIMEO_INTERACTION, null);
        }
        return videoVersioningModel;
    }

    /**
     * Updating the video upload status as Updated
     *
     * @param entityId
     * @param type
     * @return
     */
    public VideoVersioningModel updateVideoUploaded(Long entityId, String type) {
        log.info("Update video upload starts");
        long start = new Date().getTime();
        long profilingStart;
        User user = userComponents.getUser();
        VideoManagement videoManagement = null;
        log.info("Get user details : Time taken in millis : "+(new Date().getTime() - start));
        profilingStart = new Date().getTime();
        try {
            if (type.equalsIgnoreCase(InstructorConstant.VIDEO_TYPE_EXERCISE_VIDEO) || type.equalsIgnoreCase(InstructorConstant.VIDEO_TYPE_EXERCISE_SUPPORT_VIDEO)) {
                Exercises exercises = exerciseRepository.findByExerciseIdAndOwnerUserId(entityId, user.getUserId());
                if (type.equalsIgnoreCase(InstructorConstant.VIDEO_TYPE_EXERCISE_SUPPORT_VIDEO)) {
                    videoManagement = exercises.getSupportVideoManagement();
                } else {
                    videoManagement = exercises.getVideoManagement();
                }
            } else if (type.equalsIgnoreCase(InstructorConstant.VIDEO_TYPE_PROMO_VIDEO)) {
                Promotions promotions = promotionRepository.findByPromotionId(entityId);
                if (promotions.getVideoManagement().getOwner().getUserId().equals(user.getUserId())) {
                    videoManagement = promotions.getVideoManagement();
                }
            }
        } catch (Exception exception) {
            videoManagement = null;
        }
        if (videoManagement == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_VIDEO_VERSION_INVALID_PARAM, null);
        }
        log.info("Get video management from DB : Time taken in millis : "+(new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        try {
            videoUploadService.videoUploadUpdate(videoManagement);
        } catch (Exception exception) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_VIMEO_INTERACTION, null);
        }
        log.info("Upload video on vimeo : Time taken in millis : "+(new Date().getTime() - profilingStart));

        log.info("Update video upload : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Update video upload ends");
        return null;
    }

    /**
     * Member to get Packages of Instructor's client
     * @param memberId
     * @return
     */
    public Map<String, Object> getClientPackages(Long memberId) {
        User member = validationService.validateMemberId(memberId);
        User instructor = userComponents.getUser();

        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_EXPIRED);

        Sort sort = Sort.by("modifiedDate").descending();

        List<PackageSubscription> packageSubscriptions = packageSubscriptionRepository.findByUserAndSubscriptionPackageOwnerAndSubscriptionStatusSubscriptionStatusNameIn(member, instructor, statusList, sort);

        List<MemberPackageHistoryView> packageList = new ArrayList<>();
        for (PackageSubscription packageSubscription : packageSubscriptions) {
            MemberPackageHistoryView memberProgramTileView = new MemberPackageHistoryView();

            SubscriptionPackage subscriptionPackage = packageSubscription.getSubscriptionPackage();

            memberProgramTileView.setSubscriptionPackageId(subscriptionPackage.getSubscriptionPackageId());
            memberProgramTileView.setTitle(subscriptionPackage.getTitle());
            memberProgramTileView.setDuration(subscriptionPackage.getPackageDuration().getDuration() + KeyConstants.KEY_DAYS);
            if (subscriptionPackage.getImage() != null) {
                memberProgramTileView.setImageUrl(subscriptionPackage.getImage().getImagePath());
            }

            int programCount = packageProgramMappingRepository.countBySubscriptionPackage(packageSubscription.getSubscriptionPackage());
            memberProgramTileView.setNoOfPrograms(programCount);

            memberProgramTileView.setSessionCount(subscriptionPackageJpa.getBookableSessionCountForPackage(subscriptionPackage.getSubscriptionPackageId()));

            //Setting subscription status of the package
            SubscriptionStatus subscriptionStatus = subscriptionService.getMemberPackageSubscriptionStatus(packageSubscription);
            if (subscriptionStatus != null) {
                if (KeyConstants.KEY_PAID.equals(subscriptionStatus.getSubscriptionStatusName())) {
                    // Program is subscribed
                    memberProgramTileView.setSubscriptionStatus(KeyConstants.KEY_SUBSCRIBED);
                } else {
                    memberProgramTileView.setSubscriptionStatus(subscriptionStatus.getSubscriptionStatusName());
                }
            }

            packageList.add(memberProgramTileView);
        }

        if (packageList.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        Map<String, Object> memberDetails = new HashMap<>();
        memberDetails.put(KeyConstants.KEY_SUBSCRIPTION_PACKAGES, packageList);

        return memberDetails;

    }

}

