package com.fitwise.workout.service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Circuit;
import com.fitwise.entity.CircuitAndVoiceOverMapping;
import com.fitwise.entity.CircuitCompletion;
import com.fitwise.entity.CircuitRepeatCount;
import com.fitwise.entity.CircuitSchedule;
import com.fitwise.entity.ExerciseCompletion;
import com.fitwise.entity.ExerciseSchedulers;
import com.fitwise.entity.Images;
import com.fitwise.entity.Programs;
import com.fitwise.entity.SamplePrograms;
import com.fitwise.entity.User;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.WorkoutMapping;
import com.fitwise.entity.WorkoutSchedule;
import com.fitwise.entity.Workouts;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.circuit.CircuitScheduleModel;
import com.fitwise.repository.CircuitAndVoiceOverMappingRepository;
import com.fitwise.repository.ExerciseScheduleRepository;
import com.fitwise.repository.FlaggedExercisesSummaryRepository;
import com.fitwise.repository.ImageRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.SampleProgramsRepository;
import com.fitwise.repository.WorkoutMappingRepository;
import com.fitwise.repository.WorkoutRepository;
import com.fitwise.repository.WorkoutScheduleRepository;
import com.fitwise.repository.circuit.CircuitRepeatCountRepository;
import com.fitwise.repository.circuit.CircuitRepository;
import com.fitwise.repository.circuit.CircuitScheduleRepository;
import com.fitwise.repository.member.CircuitCompletionRepository;
import com.fitwise.repository.member.ExerciseCompletionRepository;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.utils.AppUtils;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.AudioResponseView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.circuit.CircuitScheduleResponseView;
import com.fitwise.view.workout.WorkoutLibraryView;
import com.fitwise.workout.daoImpl.WorkoutRepositoryImpl;
import com.fitwise.workout.model.WorkoutModel;
import com.fitwise.workout.model.WorkoutResponseView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import javax.transaction.Transactional;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WorkoutService {

    @Autowired
    private WorkoutRepositoryImpl workoutImpl;

    @Autowired
    private ImageRepository imgRepo;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private WorkoutMappingRepository workoutMappingRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private WorkoutScheduleRepository workoutScheduleRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    private ExerciseScheduleRepository exerciseScheduleRepository;

    @Autowired
    private SampleProgramsRepository sampleProgramsRepository;

    @Autowired
    CircuitRepository circuitRepository;

    @Autowired
    CircuitScheduleRepository circuitScheduleRepository;

    @Autowired
    CircuitRepeatCountRepository circuitRepeatCountRepository;

    @Autowired
    FlaggedExercisesSummaryRepository flaggedExercisesSummaryRepository;

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    CircuitCompletionRepository circuitCompletionRepository;
    @Autowired
    ExerciseCompletionRepository exerciseCompletionRepository;

    @Autowired
    private CircuitAndVoiceOverMappingRepository circuitAndVoiceOverMappingRepository;

    /**
     * Create the workout
     *
     * @param workoutModel
     * @return
     * @throws ApplicationException
     */
    @Transactional
    public ResponseModel createWorkouts(WorkoutModel workoutModel) throws ApplicationException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        long startTime = new Date().getTime();
        log.info("Create Workout started");
        long temp = new Date().getTime();
        User user = userComponents.getUser();
        doValidateWorkouts(workoutModel);
        log.info("Field validation " + (new Date().getTime() - temp));
        Workouts workout = new Workouts();
        if (workoutModel.getWorkoutId() != 0) {
            temp = new Date().getTime();
            List<Workouts> workouts = workoutRepository.findByWorkoutIdAndOwnerUserId(workoutModel.getWorkoutId(), user.getUserId());
            if (workouts.isEmpty()) {
                throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_WORKOUT_NOT_FOUND, null);
            }
            workout = workouts.get(0);
            log.info("Get Existing workout " + (new Date().getTime() - temp));
            temp = new Date().getTime();
            allowOrRestrictWorkoutEdit(workout.getWorkoutId());
            log.info("Edit restriction validation " + (new Date().getTime() - temp));
            temp = new Date().getTime();
            //Reflect Workout changes across programs
            allowOrRestrictChangesAcrossPrograms(workout.getWorkoutId(), workoutModel.getReflectChangesAcrossPrograms());
            log.info("allowOrRestrictChangesAcrossPrograms " + (new Date().getTime() - temp));
        }
        temp = new Date().getTime();
        doConstructWorkout(workout, workoutModel);
        log.info("Workout construction " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        workout = workoutImpl.saveWorkout(workout);
        log.info("Save workout " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        ResponseModel response = new ResponseModel();
        response.setMessage(MessageConstants.MSG_WORKOUT_SAVED);
        response.setPayload(constructWorkoutResponseView(workout));
        log.info("Construct workout response " + (new Date().getTime() - temp));
        response.setStatus(Constants.SUCCESS_STATUS);
        log.info("Create workout completed " + (new Date().getTime() - startTime));
        return response;
    }

    //Validation for edit restrictions for workouts associated with programs in publish, unpublish or block state
    private void allowOrRestrictWorkoutEdit(Long workoutId) {
        log.info("Start validate access to edit");
        long temp = new Date().getTime();
        String[] publishedStatuslist = {InstructorConstant.PUBLISH};
        List<WorkoutMapping> publishedProgramWorkoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdAndProgramsStatusIn(workoutId, Arrays.asList(publishedStatuslist));
        log.info("Get workout mapping with program " + (new Date().getTime() - temp));
        temp = new Date().getTime();
//        if (!publishedProgramWorkoutMappingList.isEmpty()) {
//            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_WORKOUT_EDIT_IN_PUBLISHED_PROGRAM, MessageConstants.ERROR);
//        }

        
        String[] unpublishedBlockStatuslist = {InstructorConstant.UNPUBLISH, InstructorConstant.BLOCK};
        List<WorkoutMapping> unpublishedBlockprogramWorkoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdAndProgramsStatusIn(workoutId, Arrays.asList(unpublishedBlockStatuslist));
        log.info("Get workout mapping with program in unpublish or block status " + (new Date().getTime() - temp));
        if (unpublishedBlockprogramWorkoutMappingList.size() > 0) {
            temp = new Date().getTime();
            List<Long> programIdList = unpublishedBlockprogramWorkoutMappingList.stream().map(mapping -> mapping.getPrograms().getProgramId()).collect(Collectors.toList());
            long subscriptionCount = subscriptionService.getOverallActiveSubscriptionCountForProgramsList(programIdList);
            log.info("Get paid subscriptions " + (new Date().getTime() - temp));
            if (subscriptionCount > 0) {
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_WORKOUT_IN_ACTIVE_SUBCRIPTION_PROGRAM.replace(StringConstants.LITERAL_COUNT, String.valueOf(subscriptionCount)), MessageConstants.ERROR);
            }
//            if (publishedProgramWorkoutMappingList.isEmpty()) {
//            	if (subscriptionCount > 0) {
//                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_WORKOUT_IN_ACTIVE_SUBCRIPTION_PROGRAM.replace(StringConstants.LITERAL_COUNT, String.valueOf(subscriptionCount)), MessageConstants.ERROR);
//            }
//            }
        }
    }

    //Reflect Workout changes across all associated inprogress programs
    private void allowOrRestrictChangesAcrossPrograms(Long workoutId, Boolean reflectChangesAcrossPrograms) {
        long temp = new Date().getTime();
        String[] statuslist = {InstructorConstant.PLAN, InstructorConstant.UPLOAD, InstructorConstant.SCHEDULE, InstructorConstant.PRICE, InstructorConstant.PRE_PUBLISH};
        List<WorkoutMapping> programWorkoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdAndProgramsStatusIn(workoutId, Arrays.asList(statuslist));
        log.info("Get workout associated with pgm plan, upoad, schedule, price, prepublish  " + (new Date().getTime() - temp));
        if (programWorkoutMappingList.size() > 1 && reflectChangesAcrossPrograms == null) {
            throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.MSG_WORKOUT_IN_INPROGRESS_PROGRAM, MessageConstants.ERROR);
        } else if (programWorkoutMappingList.size() > 1 && reflectChangesAcrossPrograms == Boolean.FALSE) {
            throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.MSG_WORKOUT_CREATE_NEW, MessageConstants.ERROR);
        }
    }

    private void doConstructWorkout(Workouts workout, WorkoutModel request) throws ApplicationException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.info("Construct workout entity ");
        long temp = new Date().getTime();
        User user = userComponents.getUser();
        ValidationUtils.throwException(user.getUserId() == null, "UserId cant be null", Constants.BAD_REQUEST);
        ValidationUtils.throwException(user == null, "User not exist with this Id", Constants.BAD_REQUEST);
        log.info("User validation " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        doValidateRoleAsInstructor(user);
        log.info("Validate role " + (new Date().getTime() - temp));
        workout.setDescription(request.getDescription());
        String workoutTitle = request.getTitle();

        //Duplicate Workout title validation
        boolean isNewWorkout = false;
        if (workout.getWorkoutId() == null || workout.getWorkoutId() == 0) {
            isNewWorkout = true;
        }
        if (isNewWorkout || (!isNewWorkout && !workout.getTitle().equalsIgnoreCase(workoutTitle))) {
            temp = new Date().getTime();
            Workouts workoutWithSameTitle = workoutImpl.findByOwnerUserIdAndTitle(user.getUserId(), workoutTitle);
            if (workoutWithSameTitle != null) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_WORKOUT_DUPLICATE_TITLE, MessageConstants.ERROR);
            }
            log.info("Validate duplicate workout " + (new Date().getTime() - temp));
        }
        temp = new Date().getTime();
        workout.setTitle(workoutTitle);
        workout.setOwner(user);
        Images thumbnail = imgRepo.getOne(request.getImageId());
        ValidationUtils.throwException(thumbnail == null, ValidationMessageConstants.MSG_IMAGE_NOT_FOUND, Constants.BAD_REQUEST);
        workout.setImage(thumbnail);
        log.info("Get image " + (new Date().getTime() - temp));
        constructWorkoutCircuits(request, workout);
    }

    private void constructWorkoutCircuits(WorkoutModel request, Workouts workout) {
        log.info("Construct workout circuit");
        long temp = new Date().getTime();
        User user = userComponents.getUser();
        regulateRestInCircuitSchedule(request.getCircuitSchedules());
        log.info("Regulate rest in circuit schedule " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        Set<Long> newScheduleOrderSet = new HashSet<>();
        Set<CircuitSchedule> circuitScheduleSet = new HashSet<>();
        Set<String> circuitNameSet = new HashSet<>();
        int circuitCount = 0;
        for (CircuitScheduleModel circuitScheduleModel : request.getCircuitSchedules()) {
            //Schedule Order validation
            if (circuitScheduleModel.getOrder() == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SCHEDULE_ORDER_MISSING, MessageConstants.ERROR);
            }
            if (circuitScheduleModel.getOrder().intValue() <= 0) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SCHEDULE_ORDER_CANT_BE_ZERO, MessageConstants.ERROR);
            }
            int oldUniqueScheduleOrderSize = newScheduleOrderSet.size();
            Long newScheduleOrder = circuitScheduleModel.getOrder();
            newScheduleOrderSet.add(newScheduleOrder);
            int newUniqueScheduleOrderSize = newScheduleOrderSet.size();
            if (oldUniqueScheduleOrderSize == newUniqueScheduleOrderSize || newScheduleOrder > request.getCircuitSchedules().size()) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SCHEDULE_ORDER_INCORRECT, MessageConstants.ERROR);
            }
            CircuitSchedule circuitSchedule = new CircuitSchedule();
            if (circuitScheduleModel.getCircuitScheduleId() != null && circuitScheduleModel.getCircuitScheduleId() != 0) {
                Optional<CircuitSchedule> circuitScheduleOptional = circuitScheduleRepository.findById(circuitScheduleModel.getCircuitScheduleId());
                if (!circuitScheduleOptional.isPresent()) {
                    throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_CIRCUIT_SCHEDULE_NOT_FOUND, null);
                }
                circuitSchedule = circuitScheduleOptional.get();
            }
            circuitSchedule.setWorkout(workout);
            circuitSchedule.setOrder(circuitScheduleModel.getOrder());
            boolean isRestCircuit = circuitScheduleModel.isRestCircuit();
            circuitSchedule.setRestCircuit(isRestCircuit);
            if (isRestCircuit) {
                if (circuitScheduleModel.getRestDuration() == null || circuitScheduleModel.getRestDuration() <= 0) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUITS_REST_DURATION_MISSING, MessageConstants.ERROR);
                }
                circuitSchedule.setRestDuration(circuitScheduleModel.getRestDuration());
            }else if(circuitScheduleModel.isAudio()){
                Circuit circuit = circuitRepository.findByCircuitIdAndOwnerUserId(circuitScheduleModel.getCircuitId(), user.getUserId());
                ValidationUtils.throwException(circuit == null, ValidationMessageConstants.MSG_CIRCUIT_NOT_FOUND, Constants.BAD_REQUEST);
                circuitSchedule.setCircuit(circuit);
            } else {
                long repeat = circuitScheduleModel.getRepeat();
                List<CircuitRepeatCount> repeatCounts = circuitRepeatCountRepository.findAll();
                List<Long> circuitRepeatList = repeatCounts.stream().map(repeatCount -> repeatCount.getRepeatCount()).collect(Collectors.toList());
                if (!circuitRepeatList.contains(repeat)) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUITS_REPEAT_INCORRECT, MessageConstants.ERROR);
                }
                circuitSchedule.setRepeat(repeat);
                if (circuitScheduleModel.getRestBetweenRepeat() != null) {
                    circuitSchedule.setRestBetweenRepeat(circuitScheduleModel.getRestBetweenRepeat());
                }
                Circuit circuit = circuitRepository.findByCircuitIdAndOwnerUserId(circuitScheduleModel.getCircuitId(), user.getUserId());
                ValidationUtils.throwException(circuit == null, ValidationMessageConstants.MSG_CIRCUIT_NOT_FOUND, Constants.BAD_REQUEST);
                circuitSchedule.setCircuit(circuit);
                circuitNameSet.add(circuit.getTitle());
                circuitCount++;
            }
            circuitSchedule.setIsAudio(circuitScheduleModel.isAudio());
            circuitScheduleSet.add(circuitSchedule);
        }
        log.info("Circuit schedule construction " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        workout.setCircuitSchedules(circuitScheduleSet);
        validateFlaggedVideosInWorkout(circuitScheduleSet, true);
        log.info("Validate flagged video " + (new Date().getTime() - temp));
        if (circuitNameSet.size() < circuitCount) {
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_CIRCUIT_DUPLICATE_TITLE_IN_WORKOUT, null);
        }
    }

    //Regulating rest distribution in a circuit
    private void regulateRestInCircuitSchedule(List<CircuitScheduleModel> schedules) {
        schedules.sort(Comparator.comparing(CircuitScheduleModel::getOrder));
        if (schedules.size() > 0) {
            //First circuit in a workout can not be a rest circuit
            if (schedules.get(0).isRestCircuit()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FIRST_CIRCUIT_NO_REST, MessageConstants.ERROR);
            }
            //A workout can not have 2 consecutive rest circuit
            for (int i = 0; i < schedules.size() - 1; i++) {
                if (schedules.get(i).isRestCircuit() && schedules.get(i + 1).isRestCircuit()) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONSECUTIVE_CIRCUIT_NO_REST, MessageConstants.ERROR);
                }
            }
            //Last circuit in a workout can not be a rest circuit
            if (schedules.get(schedules.size() - 1).isRestCircuit()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_LAST_CIRCUIT_NO_REST, MessageConstants.ERROR);
            }
        }
    }

    /**
     * Validation to check if a flagged video in block state is added to the workout
     * @param circuitSchedules
     */
    public void validateFlaggedVideosInWorkout(Set<CircuitSchedule> circuitSchedules, boolean isWorkoutValidation) {
        List<Long> exerciseIdList = new ArrayList<>();

        for (CircuitSchedule circuitSchedule : circuitSchedules) {
            if (!circuitSchedule.isRestCircuit() && (circuitSchedule.getIsAudio() == null || !circuitSchedule.getIsAudio().booleanValue())) {
                for (ExerciseSchedulers exerciseScheduler : circuitSchedule.getCircuit().getExerciseSchedules()) {
                    if (exerciseScheduler.getExercise() != null) {
                        exerciseIdList.add(exerciseScheduler.getExercise().getExerciseId());
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
            if (isWorkoutValidation) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_WORKOUT_CANT_ADD_FLAGGED_VIDEO, MessageConstants.ERROR);
            } else {
                throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_PROGRAM_CANT_ADD_WORKOUT_WITH_FLAGGED_VIDEO, MessageConstants.ERROR);
            }
        }

    }

    private void doValidateWorkouts(WorkoutModel workout) throws ApplicationException {
        ValidationUtils.throwException(workout == null, ValidationMessageConstants.MSG_WORKOUT_MODEL_EMPTY, Constants.BAD_REQUEST);
        ValidationUtils.throwException(workout.getImageId() == null, ValidationMessageConstants.MSG_THUMBNAIL_ID_NULL, Constants.BAD_REQUEST);
        ValidationUtils.throwException(ValidationUtils.isEmptyString(workout.getTitle()), ValidationMessageConstants.MSG_TITLE_NULL, Constants.BAD_REQUEST);
        ValidationUtils.throwException(workout.getCircuitSchedules() == null || workout.getCircuitSchedules().isEmpty(), ValidationMessageConstants.MSG_CIRCUITS_EMPTY, Constants.BAD_REQUEST);
    }

    private void doValidateRoleAsInstructor(User user) throws ApplicationException {
        Set<UserRole> roles = AppUtils.getUserRoles(user); //AKHIL
        boolean isInstructor = false;
        for (UserRole role : roles) {
            if (role.getName().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR)) {
                isInstructor = true;
                break;
            }
        }
        if (!isInstructor) {
            ValidationUtils.throwException(true, "Invalid role", Constants.BAD_REQUEST);
        }
    }

    /**
     * Constructing the workout response
     *
     * @param workouts
     * @return
     */
    public WorkoutResponseView constructWorkoutResponseView(Workouts workouts) {
        log.info("Workout response construction starts");
        long start = new Date().getTime();
        long profilingStart;
        User user = userComponents.getUser();
        WorkoutResponseView workoutResponse = new WorkoutResponseView();
        long duration = 0;
        workoutResponse.setWorkoutId(workouts.getWorkoutId());
        workoutResponse.setTitle(workouts.getTitle());
        workoutResponse.setDescription(workouts.getDescription());
        workoutResponse.setFlag(workouts.isFlag());
        workoutResponse.setInstructorId(user.getUserId());
        if (workouts.getImage() != null && workouts.getImage().getImageId() != null)
            workoutResponse.setImageId(workouts.getImage().getImageId());
        if (workouts.getImage() != null) {
            workoutResponse.setThumbnailUrl(workouts.getImage().getImagePath());
        }
        log.info("Basic details : Time taken in millis : "+(new Date().getTime() - start));
        if (workouts.getCircuitSchedules() != null && workouts.getCircuitSchedules().size() > 0) {
            List<CircuitScheduleResponseView> circuitScheduleViewList = new ArrayList<>();
            int circuitCount = 0;
            boolean isWorkoutVideoProcessingPending = false;
            profilingStart = new Date().getTime();
            for (CircuitSchedule circuitSchedule : workouts.getCircuitSchedules()) {
                CircuitScheduleResponseView circuitScheduleView = new CircuitScheduleResponseView();
                circuitScheduleView.setCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
                circuitScheduleView.setOrder(circuitSchedule.getOrder());
                long circuitDuration = 0;
                boolean isRestCircuit = circuitSchedule.isRestCircuit();
                circuitScheduleView.setRestCircuit(isRestCircuit);
                if (isRestCircuit) {
                    circuitDuration = circuitSchedule.getRestDuration();
                } else if(circuitSchedule.getIsAudio() != null && circuitSchedule.getIsAudio()){
                    boolean isAudio = (circuitSchedule.getIsAudio() != null && circuitSchedule.getIsAudio().booleanValue()) ? true : false;
                    circuitScheduleView.setAudio(isAudio);
                    List<AudioResponseView> audioResponseViews = new ArrayList<>();
                    for (CircuitAndVoiceOverMapping circuitAndVoiceOverMapping : circuitSchedule.getCircuit().getCircuitAndVoiceOverMappings()) {
                        AudioResponseView audioResponseView = new AudioResponseView();
                        audioResponseView.setAudioId(circuitAndVoiceOverMapping.getVoiceOver().getAudios().getAudioId());
                        audioResponseView.setFilePath(circuitAndVoiceOverMapping.getVoiceOver().getAudios().getFilePath());
                        audioResponseView.setDuration(circuitAndVoiceOverMapping.getVoiceOver().getAudios().getDuration());
                        audioResponseView.setTitle(circuitAndVoiceOverMapping.getVoiceOver().getTitle());
                        audioResponseView.setVoiceOverId(circuitAndVoiceOverMapping.getVoiceOver().getVoiceOverId());
                        audioResponseView.setCircuitAndVoiceOverMappingId(circuitAndVoiceOverMapping.getCircuitAndVoiceOverMappingId());
                        audioResponseViews.add(audioResponseView);
                        circuitDuration += circuitAndVoiceOverMapping.getVoiceOver().getAudios().getDuration();
                    }
                    circuitScheduleView.setCircuitId(circuitSchedule.getCircuit().getCircuitId());
                    circuitScheduleView.setAudioResponseView(audioResponseViews);
                    circuitScheduleView.setDuration(duration);
                } else {
                    circuitCount++;
                    circuitScheduleView.setCircuitId(circuitSchedule.getCircuit().getCircuitId());
                    circuitScheduleView.setCircuitTitle(circuitSchedule.getCircuit().getTitle());
                    Long repeat = circuitSchedule.getRepeat();
                    circuitScheduleView.setRepeat(repeat);
                    Long restBetweenRepeat = circuitSchedule.getRestBetweenRepeat();
                    circuitScheduleView.setRestBetweenRepeat(restBetweenRepeat);
                    Set<ExerciseSchedulers> exerciseSchedules = circuitSchedule.getCircuit().getExerciseSchedules();
                    List<String> exerciseThumbnails = new ArrayList<>();
                    int exerciseCount = 0;
                    boolean isCircuitProcessingPending = false;
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
                                    isCircuitProcessingPending = true;
                                }
                            }
                            exerciseCount++;
                        } else if(schedule.getWorkoutRestVideo() != null){
                            exerciseDuration = schedule.getWorkoutRestVideo().getRestTime();
                        }else if(schedule.getVoiceOver() != null){
                            exerciseDuration = schedule.getVoiceOver().getAudios().getDuration();
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
                    circuitScheduleView.setVideoProcessingPending(isCircuitProcessingPending);

                    circuitScheduleView.setExerciseThumbnails(exerciseThumbnails);
                }

                circuitScheduleView.setDuration(circuitDuration);
                duration += circuitDuration;

                circuitScheduleViewList.add(circuitScheduleView);
            }
            log.info("Circuit schedules construction : total time taken in millis : "+(new Date().getTime() - profilingStart));

            Collections.sort(circuitScheduleViewList, Comparator.comparing(CircuitScheduleResponseView::getOrder));
            workoutResponse.setCircuitSchedules(circuitScheduleViewList);
            workoutResponse.setDuration(duration);
            workoutResponse.setVideoProcessingPending(isWorkoutVideoProcessingPending);
            workoutResponse.setCircuitCount(circuitCount);
        }
        profilingStart = new Date().getTime();
        workoutResponse.setStatus(getWorkoutStatus(workouts));
        log.info("Get workout status : Time taken in millis : "+(new Date().getTime() - profilingStart));

        profilingStart = new Date().getTime();
        workoutResponse.setActiveSubscriptionCount(getActiveSubscriptionCountOfWorkout(workouts.getWorkoutId()));
        log.info("Active subscriptions count : Time taken in millis : "+(new Date().getTime() - profilingStart));

        profilingStart = new Date().getTime();
        String[] statuslist = {InstructorConstant.PLAN, InstructorConstant.UPLOAD, InstructorConstant.SCHEDULE, InstructorConstant.PRICE, InstructorConstant.PRE_PUBLISH};
        List<WorkoutMapping> programWorkoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdAndProgramsStatusIn(workouts.getWorkoutId(), Arrays.asList(statuslist));
        workoutResponse.setAssociatedInProgressProgramCount(programWorkoutMappingList.size());
        log.info("Associated program query : Time taken in millis : "+(new Date().getTime() - profilingStart));

        return workoutResponse;
    }

    //Workout status set from associated program's status
    private String getWorkoutStatus(Workouts workouts) {
        boolean isUpdated = false;
        String status = InstructorConstant.AVAILABLE;
        String[] publishStatuslist = {InstructorConstant.PUBLISH};
        List<WorkoutMapping> programWorkoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdAndProgramsStatusIn(workouts.getWorkoutId(), Arrays.asList(publishStatuslist));
        if (!programWorkoutMappingList.isEmpty()) {
            status = InstructorConstant.PUBLISH;
            isUpdated = true;
        }
        if (!isUpdated) {
            String[] unpublishBlockStatuslist = {InstructorConstant.UNPUBLISH, InstructorConstant.BLOCK};
            programWorkoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdAndProgramsStatusIn(workouts.getWorkoutId(), Arrays.asList(unpublishBlockStatuslist));
            if (!programWorkoutMappingList.isEmpty()) {
                status = InstructorConstant.UNPUBLISH;
            }
        }
        return status;
    }

    private int getActiveSubscriptionCountOfWorkout(Long workoutId) {
        int activeSubscriptionCount = 0;
        String[] statuslist = {InstructorConstant.PUBLISH, InstructorConstant.UNPUBLISH, InstructorConstant.BLOCK};
        List<WorkoutMapping> programWorkoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdAndProgramsStatusIn(workoutId, Arrays.asList(statuslist));
        if (programWorkoutMappingList.size() > 0) {
            List<Long> programIdList = programWorkoutMappingList.stream().map(mapping -> mapping.getPrograms().getProgramId()).collect(Collectors.toList());
            activeSubscriptionCount = (int) subscriptionService.getOverallActiveSubscriptionCountForProgramsList(programIdList);
        }
        return activeSubscriptionCount;
    }

    public List<WorkoutResponseView> getWorkouts(final Long workoutId, Optional<String> searchName) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        List<Workouts> workouts = null;
        User user = userComponents.getUser();
        if (searchName.isPresent() && !searchName.get().isEmpty() && searchName.get().length() != 0) {
            workouts = workoutRepository.findByOwnerUserIdAndTitleIgnoreCaseContainingOrOwnerUserIdAndDescriptionIgnoreCaseContaining(user.getUserId(), searchName.get(), user.getUserId(), searchName.get());
        } else {
            if (workoutId != null) {
                workouts = workoutRepository.findByWorkoutIdAndOwnerUserId(workoutId, user.getUserId());
            } else {
                workouts = workoutImpl.findByOwnerUserId(user);
            }
        }
        List<WorkoutResponseView> workoutResponseViews = new ArrayList<>();
        if (workouts == null) {
            return Collections.emptyList();
        }
        for (Workouts workout : workouts) {
            workoutResponseViews.add(constructWorkoutResponseView(workout));
        }
        return workoutResponseViews;
    }

    /**
     * Get the workout for the given workout id
     *
     * @param workoutId
     * @return
     */
    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public WorkoutResponseView getWorkout(final Long workoutId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.info("Get workout starts");
        long start = new Date().getTime();
        long profilingStart;
        User user = userComponents.getUser();
        List<Workouts> workouts = new ArrayList<>();
        profilingStart = new Date().getTime();
        List<SamplePrograms> samplePrograms = sampleProgramsRepository.findAll();
        List<Long> sampleProgramIds = new ArrayList<>();
        for (SamplePrograms sampleProgram : samplePrograms) {
            sampleProgramIds.add(sampleProgram.getPrograms().getProgramId());
        }
        log.info("Get Sample programIds : Time taken in millis : "+(new Date().getTime()-profilingStart));

        profilingStart = new Date().getTime();
        List<WorkoutMapping> workoutMappings = workoutMappingRepository.findByWorkoutWorkoutIdAndProgramsProgramIdIn(workoutId, sampleProgramIds);
        if (!workoutMappings.isEmpty()) {
            Workouts workout = workoutRepository.findByWorkoutId(workoutId);
            if (workout != null)
                workouts.add(workout);
        } else {
            workouts = workoutRepository.findByWorkoutIdAndOwnerUserId(workoutId, user.getUserId());
        }
        log.info("Workout query : Time taken in millis : "+(new Date().getTime()-profilingStart));

        if (workouts.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        profilingStart = new Date().getTime();
        WorkoutResponseView workoutResponseView = constructWorkoutResponseView(workouts.get(0));
        log.info("Response construction : Time taken in millis : "+(new Date().getTime()-profilingStart));
        log.info("Get workout : Total time taken in millis : "+(new Date().getTime() - start));
        log.info("Get workout ends");
        return workoutResponseView;

    }

    /**
     * To remove workout from the program
     *
     * @param programId
     * @param workoutId
     * @return
     */
    public ResponseModel removeWorkout(long programId, long workoutId) {
        ResponseModel responseModel = new ResponseModel();
        User user = userComponents.getUser();
        Programs programs = programRepository.findByProgramIdAndOwnerUserId(programId, user.getUserId());
        List<String> restrictedStatusList = Arrays.asList(new String[]{InstructorConstant.UNPUBLISH, DBConstants.UNPUBLISH_EDIT, InstructorConstant.BLOCK, DBConstants.BLOCK_EDIT});
        boolean isProgramRestricted = restrictedStatusList.stream().anyMatch(programs.getStatus()::equalsIgnoreCase);
        if (isProgramRestricted) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_PGM_WKT_REMOVE_RESTRICTED, MessageConstants.ERROR);
        }
        validationService.validateRemoveWorkout(user.getUserId(), programId, workoutId);
        WorkoutMapping workoutMapping = workoutMappingRepository.findByProgramsProgramIdAndWorkoutWorkoutId(programId, workoutId);
        if (workoutMapping == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_WRONG_PROGRAM_WORKOUT_ID, MessageConstants.ERROR);
        }
        workoutMappingRepository.delete(workoutMapping);
        if (InstructorConstant.PUBLISH.equals(programs.getStatus())) {
            programs.setStatus(DBConstants.PUBLISH);
        } else {
        	programs.setStatus(InstructorConstant.SCHEDULE);
        }
        programRepository.save(programs);
        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_WORKOUT_REMOVED);
        return responseModel;
    }


    /**
     * To remove workout from the Workout Schedule
     *
     * @param workoutScheduleId
     * @return
     */
    public ResponseModel removeWorkoutFromSchedule(long workoutScheduleId) {
        ResponseModel responseModel = new ResponseModel();
        User user = userComponents.getUser();
        WorkoutSchedule workoutSchedules = workoutScheduleRepository.findByWorkoutScheduleId(workoutScheduleId);
        if (workoutSchedules == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
        }
        Workouts workouts = workoutRepository.findByWorkoutId(workoutSchedules.getWorkout().getWorkoutId());
        validationService.validateIfWorkoutBelongsToInstructor(user.getUserId(), workouts.getWorkoutId());

        workoutScheduleRepository.delete(workoutSchedules);

        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_WORKOUT_REMOVED_FROM_SCHEDULE);

        return responseModel;
    }

    public ResponseModel deleteThumbnail(Long imageId, Long workoutId) throws ApplicationException {
        ResponseModel responseModel = new ResponseModel();

        if (imageId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_IMAGE_ID_NULL, MessageConstants.ERROR);
        }

        if (workoutId == null || workoutId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_ID_NULL, MessageConstants.ERROR);
        }


        Images images = imgRepo.findByImageId(imageId);
        if (images == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_IMAGE_NOT_FOUND, MessageConstants.ERROR);
        }


        User user = userComponents.getUser();
        List<Workouts> workouts = workoutRepository.findByWorkoutIdAndOwnerUserId(workoutId, user.getUserId());
        if (workouts.isEmpty()) {
            throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_WORKOUT_NOT_FOUND, null);
        }
        Workouts workout = workouts.get(0);

        List<Workouts> workoutsList = workoutRepository.findByImageImageId(imageId);
        boolean isPresent=false;

        for(Workouts workout1:workoutsList){
            if(workout.getWorkoutId().equals(workout1.getWorkoutId())){
                isPresent=true;
                break;
            }
        }

        if (!isPresent) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_ID_INCORRECT, MessageConstants.ERROR);
        }

        boolean isRelatedToOneWorkout = false;
        if (workoutsList.size() == 1) {
            isRelatedToOneWorkout = true;
        }

        workout.setImage(null);
        workoutRepository.save(workout);

        if (isRelatedToOneWorkout) {
            imgRepo.delete(images);
        }

        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_THUMBNAIL_DELETED);

        return responseModel;
    }

    /**
     *
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param searchName
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    public ResponseModel getAllInstructorWorkouts(int pageNo, int pageSize, String sortOrder, String sortBy, Optional<String> searchName) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        long startTime = new Date().getTime();
        log.info("Get all instrcutor workouts started");
        long temp = new Date().getTime();
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        if (!(sortBy.equalsIgnoreCase(SearchConstants.TITLE) || sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
        }
        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }
        log.info("Field validation " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        User user = userComponents.getUser();
        Sort sort = getWorkoutLibrarySortCriteria(sortBy);
        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            sort = sort.descending();
        }
        log.info("Sort criteria generation " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<Workouts> workouts;
        if (searchName.isPresent() && !searchName.get().isEmpty()) {
            workouts = workoutRepository.findByOwnerUserIdAndTitleIgnoreCaseContaining(user.getUserId(), searchName.get(), pageRequest);
        } else {
            workouts = workoutRepository.findByOwnerUserId(user.getUserId(), pageRequest);
        }
        log.info("Get all workouts for instructor " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        Map<String, Object> map = new HashMap<>();
        if (workouts.isEmpty()) {
            map.put(KeyConstants.KEY_WORKOUTS, Collections.EMPTY_LIST);
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, map);
        }
        List<WorkoutLibraryView> workoutResponseViews = new ArrayList<>();
        for (Workouts workout : workouts) {
            workoutResponseViews.add(constructWorkoutLibraryResponse(workout));
        }
        log.info("Construct response " + (new Date().getTime() - temp));
        map.put(KeyConstants.KEY_TOTAL_COUNT, workouts.getTotalElements());
        map.put(KeyConstants.KEY_WORKOUTS, workoutResponseViews);
        log.info("Get all instrcutor workouts completed " + (new Date().getTime() - startTime));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, map);
    }

    private Sort getWorkoutLibrarySortCriteria(String sortBy) throws ApplicationException {
        Sort sort;
        if (sortBy.equalsIgnoreCase(SearchConstants.TITLE)) {
            sort = Sort.by(SearchConstants.TITLE);
        } else {
            sort = Sort.by("workoutId");
        }
        return sort;
    }

    private WorkoutLibraryView constructWorkoutLibraryResponse(Workouts workout) {
        WorkoutLibraryView workoutLibraryView = new WorkoutLibraryView();
        workoutLibraryView.setWorkoutId(workout.getWorkoutId());
        workoutLibraryView.setTitle(workout.getTitle());
        if (workout.getImage() != null) {
            workoutLibraryView.setThumbnailUrl(workout.getImage().getImagePath());
        }
        List<Long> exerciseIdList = new ArrayList<>();
        long duration = 0;
        int circuitCount = 0;
        boolean isVideoProcessingPending = false;
        for (CircuitSchedule circuitSchedule : workout.getCircuitSchedules()) {
            long circuitDuration = 0;
            boolean isRestCircuit = circuitSchedule.isRestCircuit();
            if (isRestCircuit) {
                circuitDuration = circuitSchedule.getRestDuration();
            } else if(circuitSchedule.getCircuit() != null && (circuitSchedule.getIsAudio() == null || !circuitSchedule.getIsAudio())){
                circuitCount++;
                for (ExerciseSchedulers schedule : circuitSchedule.getCircuit().getExerciseSchedules()) {
                    long exerciseDuration = 0;
                    if (schedule.getExercise() != null) {
                        if (schedule.getExercise().getVideoManagement() != null) {
                            exerciseDuration = schedule.getExercise().getVideoManagement().getDuration();
                            if (schedule.getLoopCount() != null && schedule.getLoopCount() > 0) {
                                //Repeat Count Change : Since repeat count is changes as no of times video should play
                                exerciseDuration = exerciseDuration * schedule.getLoopCount();
                            }
                            if (fitwiseUtils.isVideoProcessingPending(schedule.getExercise().getVideoManagement())) {
                                isVideoProcessingPending = true;
                            }
                        }
                        exerciseIdList.add(schedule.getExercise().getExerciseId());
                    } else if(schedule.getWorkoutRestVideo() != null){
                        exerciseDuration = schedule.getWorkoutRestVideo().getRestTime();
                    }else if(schedule.getVoiceOver() != null){
                        exerciseDuration = schedule.getVoiceOver().getAudios().getDuration();
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
            }else if(circuitSchedule.getIsAudio() != null && circuitSchedule.getIsAudio()) {
                for(CircuitAndVoiceOverMapping circuitAndVoiceOverMapping : circuitSchedule.getCircuit().getCircuitAndVoiceOverMappings()){
                    circuitDuration += circuitAndVoiceOverMapping.getVoiceOver().getAudios().getDuration();
                }
            }
            duration += circuitDuration;
        }
        workoutLibraryView.setCircuitCount(circuitCount);
        workoutLibraryView.setDuration(duration);
        workoutLibraryView.setStatus(getWorkoutStatus(workout));
        workoutLibraryView.setActiveSubscriptionCount(getActiveSubscriptionCountOfWorkout(workout.getWorkoutId()));
        workoutLibraryView.setVideoProcessingPending(isVideoProcessingPending);
        String[] statuslist = {InstructorConstant.PLAN, InstructorConstant.UPLOAD, InstructorConstant.SCHEDULE, InstructorConstant.PRICE, InstructorConstant.PRE_PUBLISH};
        int programWorkoutMappingCount = workoutMappingRepository.countByWorkoutWorkoutIdAndProgramsStatusIn(workout.getWorkoutId(), Arrays.asList(statuslist));
        workoutLibraryView.setAssociatedInProgressProgramCount(programWorkoutMappingCount);
        boolean containsBlockedExercise = false;
        if (!exerciseIdList.isEmpty()) {
            containsBlockedExercise = flaggedExercisesSummaryRepository.existsByExerciseExerciseIdInAndFlagStatus(exerciseIdList, KeyConstants.KEY_BLOCK);
        }
        workoutLibraryView.setContainsBlockedExercise(containsBlockedExercise);
        return workoutLibraryView;
    }

    /**
     * Delete a workout and make it anonymous.
     * @param workoutId
     */
    public void deleteWorkout(Long workoutId) {
        User user = userComponents.getUser();
        deleteWorkout(workoutId, user);
    }

    public void deleteWorkout(Long workoutId, User user) {

        if (workoutId == null || workoutId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_ID_NULL, MessageConstants.ERROR);
        }

        List<Workouts> workouts = workoutRepository.findByWorkoutIdAndOwnerUserId(workoutId, user.getUserId());
        if (workouts.isEmpty()) {
            throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_WORKOUT_NOT_FOUND, null);
        }
        Workouts workout = workouts.get(0);

        List<WorkoutMapping> programWorkoutMappingList = workoutMappingRepository.findByWorkoutWorkoutId(workoutId);

        if (!programWorkoutMappingList.isEmpty()) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_WORKOUT_DELETION_FAILED, MessageConstants.ERROR);
        }

        List<CircuitSchedule> circuitScheduleList = circuitScheduleRepository.findByWorkoutWorkoutId(workout.getWorkoutId());
        if (!circuitScheduleList.isEmpty()) {
            //Deleting circuit schedule completions
            List<Long> circuitScheduleIdList = circuitScheduleList.stream().map(circuitSchedule -> circuitSchedule.getCircuitScheduleId()).collect(Collectors.toList());
            List<CircuitCompletion> circuitCompletionList = circuitCompletionRepository.findByCircuitScheduleIdIn(circuitScheduleIdList);
            circuitCompletionRepository.deleteInBatch(circuitCompletionList);
            List<ExerciseCompletion> exerciseCompletionList = exerciseCompletionRepository.findByCircuitScheduleIdIn(circuitScheduleIdList);
            exerciseCompletionRepository.deleteInBatch(exerciseCompletionList);
            circuitScheduleRepository.deleteInBatch(circuitScheduleList);
            //Deleting circuits within a workout
            for(CircuitSchedule circuitSchedule : circuitScheduleList){
                Circuit circuit = circuitRepository.findByCircuitId(circuitSchedule.getCircuit().getCircuitId());
                List<ExerciseSchedulers> exerciseSchedulerList = exerciseScheduleRepository.findByCircuitCircuitId(circuit.getCircuitId());
                if (!exerciseSchedulerList.isEmpty()) {
                    exerciseCompletionRepository.deleteInBatch(exerciseCompletionList);
                    exerciseScheduleRepository.deleteInBatch(exerciseSchedulerList);
                }
                List<CircuitAndVoiceOverMapping> circuitAndVoiceOverMappingList = circuitAndVoiceOverMappingRepository.findByCircuitCircuitId(circuit.getCircuitId());
                if(!circuitAndVoiceOverMappingList.isEmpty()) {
                    circuitAndVoiceOverMappingRepository.deleteInBatch(circuitAndVoiceOverMappingList);
                }
                fitwiseUtils.makeCircuitAnonymous(circuit);
                circuitRepository.save(circuit);
            }
        }
        fitwiseUtils.makeWorkoutAnonymous(workout);
        workoutRepository.save(workout);
    }

    /**
     * @param programId
     * @param workoutId
     * @return
     */
    public ResponseModel removeWorkoutForRestrictedProgram(Long programId, Long workoutId) {
        User user = userComponents.getUser();
        doValidateRoleAsInstructor(user);
        if (programId == null || programId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_ID_NULL, MessageConstants.ERROR);
        }
        Programs program = programRepository.findByProgramIdAndOwnerUserId(programId, user.getUserId());
        ValidationUtils.throwException(program == null, ValidationMessageConstants.MSG_PROGRAM_NOT_FOUND, Constants.BAD_REQUEST);
        validationService.validateRemoveWorkout(user.getUserId(), programId, workoutId);
        //Validation to allow only programs in unpublish_edit or block_edit state
        List<String> restrictedStatusList = Arrays.asList(new String[]{DBConstants.UNPUBLISH_EDIT, DBConstants.BLOCK_EDIT});
        boolean isProgramRestricted = restrictedStatusList.stream().anyMatch(program.getStatus()::equalsIgnoreCase);
        if (!isProgramRestricted) {
            throw new ApplicationException(Constants.CONFLICT, ValidationMessageConstants.MSG_PROGRAMS_NOT_RESTRICTED_EDIT, MessageConstants.ERROR);
        }
        WorkoutMapping workoutMapping = workoutMappingRepository.findByProgramsProgramIdAndWorkoutWorkoutId(programId, workoutId);
        if (workoutMapping == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_WRONG_PROGRAM_WORKOUT_ID, MessageConstants.ERROR);
        }
        workoutMappingRepository.delete(workoutMapping);
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_WORKOUT_REMOVED);
        return responseModel;
    }

}