package com.fitwise.service.circuit;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.VideoUploadStatus;
import com.fitwise.entity.Circuit;
import com.fitwise.entity.CircuitAndVoiceOverMapping;
import com.fitwise.entity.CircuitCompletion;
import com.fitwise.entity.CircuitRepeatCount;
import com.fitwise.entity.CircuitSchedule;
import com.fitwise.entity.Equipments;
import com.fitwise.entity.ExerciseCompletion;
import com.fitwise.entity.ExerciseSchedulers;
import com.fitwise.entity.Exercises;
import com.fitwise.entity.SetsCount;
import com.fitwise.entity.User;
import com.fitwise.entity.VideoManagement;
import com.fitwise.entity.VoiceOver;
import com.fitwise.entity.WorkoutMapping;
import com.fitwise.entity.WorkoutRestVideos;
import com.fitwise.entity.Workouts;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.service.VimeoService;
import com.fitwise.model.circuit.CircuitModel;
import com.fitwise.model.circuit.CircuitModelWithSetsAndReps;
import com.fitwise.repository.CircuitAndVoiceOverMappingRepository;
import com.fitwise.repository.EquipmentsRepository;
import com.fitwise.repository.ExerciseRepository;
import com.fitwise.repository.ExerciseScheduleRepository;
import com.fitwise.repository.FlaggedExercisesSummaryRepository;
import com.fitwise.repository.VoiceOverRepository;
import com.fitwise.repository.WorkoutMappingRepository;
import com.fitwise.repository.WorkoutRepository;
import com.fitwise.repository.WorkoutRestRepository;
import com.fitwise.repository.challenge.SetsCountRepository;
import com.fitwise.repository.circuit.CircuitRepeatCountRepository;
import com.fitwise.repository.circuit.CircuitRepository;
import com.fitwise.repository.circuit.CircuitScheduleRepository;
import com.fitwise.repository.member.CircuitCompletionRepository;
import com.fitwise.repository.member.ExerciseCompletionRepository;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.AudioResponseView;
import com.fitwise.view.ExerciseScheduleViewWithSetsAndReps;
import com.fitwise.view.RestResponseView;
import com.fitwise.view.circuit.CircuitLibraryView;
import com.fitwise.view.circuit.CircuitResponseView;
import com.fitwise.view.circuit.CircuitResponseViewWithSetsAndReps;
import com.fitwise.view.circuit.ExerciseScheduleView;
import com.fitwise.workout.model.ExerciseMappingModel;
import com.fitwise.workout.model.ExerciseMappingModelWithSetsAndReps;
import com.fitwise.workout.model.SupportingVideoMappingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Created by Vignesh G on 11/05/20
 */
@Service
@Slf4j
public class CircuitService {

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private CircuitRepository circuitRepository;

    @Autowired
    private CircuitScheduleRepository circuitScheduleRepository;

    @Autowired
    private WorkoutRestRepository workoutRestRepository;

    @Autowired
    private ExerciseScheduleRepository exerciseScheduleRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    WorkoutMappingRepository workoutMappingRepository;

    @Autowired
    VimeoService vimeoService;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    private ValidationService validationService;

    @Autowired
    CircuitRepeatCountRepository circuitRepeatCountRepository;

    @Autowired
    private EquipmentsRepository equipmentsRepository;

    @Autowired
    private CircuitCompletionRepository circuitCompletionRepository;

    @Autowired
    private ExerciseCompletionRepository exerciseCompletionRepository;

    @Autowired
    FlaggedExercisesSummaryRepository flaggedExercisesSummaryRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private SetsCountRepository setsCountRepository;

    @Autowired
    private VoiceOverRepository voiceOverRepository;

    @Autowired
    private CircuitAndVoiceOverMappingRepository circuitAndVoiceOverMappingRepository;

    /**
     * Circuit name validation
     *
     * @param circuitName
     */
    public void validateName(String circuitName, Long workoutId) {
        if (circuitName == null || circuitName.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_TITLE_NULL, null);
        }

        User user = userComponents.getUser();
        List<Workouts> workouts = workoutRepository.findByWorkoutIdAndOwnerUserId(workoutId, user.getUserId());
        if (workouts.isEmpty()) {
            throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_WORKOUT_NOT_FOUND, null);
        }
        Workouts workout = workouts.get(0);

        Set<CircuitSchedule> circuitSchedules = workout.getCircuitSchedules();
        for (CircuitSchedule circuitSchedule : circuitSchedules) {
            if (!circuitSchedule.isRestCircuit() && circuitSchedule.getCircuit() != null) {
                if (circuitName.equalsIgnoreCase(circuitSchedule.getCircuit().getTitle())) {
                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_CIRCUIT_DUPLICATE_TITLE_IN_WORKOUT, null);
                }
            }
        }
    }

    /**
     * Create/edit a circuit
     *
     * @param circuitModel
     * @return
     * @throws ApplicationException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    @Transactional
    public CircuitResponseView createCircuit(CircuitModel circuitModel) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        validateCircuitModel(circuitModel);
        User user = userComponents.getUser();

        Circuit circuit = new Circuit();
        //Check if circuit already exists
        if (circuitModel.getCircuitId() != null && circuitModel.getCircuitId() != 0) {
            circuit = circuitRepository.findByCircuitIdAndOwnerUserId(circuitModel.getCircuitId(), user.getUserId());
            if (circuit == null) {
                throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_CIRCUIT_NOT_FOUND, null);
            }

            allowOrRestrictCircuitEdit(circuit.getCircuitId());

            //allowOrRestrictChangesAcrossWorkouts(circuit.getCircuitId(), circuitModel.getReflectChangesAcrossWorkouts());

        }

        if(circuitModel.isAudio()){
            if(circuitModel.getVoiceOverIds() == null && circuitModel.getVoiceOverIds().isEmpty()){
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_ID_INVALID, null);
            }
            /*List<VoiceOver> voiceOvers = voiceOverRepository.findByVoiceOverIdIn(circuitModel.getVoiceOverIds());
            if (voiceOvers == null & voiceOvers.isEmpty()){
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_NOT_FOUND, null);
            }*/

            Set<CircuitAndVoiceOverMapping> circuitAndVoiceOverMappings = new HashSet<>();
            for (Long voiceOverId :circuitModel.getVoiceOverIds()) {
                VoiceOver voiceOver = voiceOverRepository.findByVoiceOverId(voiceOverId);
                if (voiceOver == null){
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_NOT_FOUND, null);
                }
                CircuitAndVoiceOverMapping circuitAndVoiceOverMapping = new CircuitAndVoiceOverMapping();
                circuitAndVoiceOverMapping.setVoiceOver(voiceOver);
                circuitAndVoiceOverMappings.add(circuitAndVoiceOverMapping);
                circuitAndVoiceOverMapping.setCircuit(circuit);
            }
            circuit.setOwner(user);
            circuit.setIsAudio(circuitModel.isAudio());
            circuit.setCircuitAndVoiceOverMappings(circuitAndVoiceOverMappings);
        }else {
            constructCircuit(circuit, circuitModel);
        }
        circuitRepository.save(circuit);
        return constructCircuitResponseView(circuit);
    }

    private void validateCircuitModel(CircuitModel circuitModel) {
        ValidationUtils.throwException(circuitModel == null, ValidationMessageConstants.MSG_CIRCUIT_MODEL_EMPTY, Constants.BAD_REQUEST);
        if(!circuitModel.isAudio()) {
            ValidationUtils.throwException(ValidationUtils.isEmptyString(circuitModel.getTitle()), ValidationMessageConstants.MSG_TITLE_NULL, Constants.BAD_REQUEST);
            ValidationUtils.throwException(circuitModel.getExerciseSchedules() == null || circuitModel.getExerciseSchedules().isEmpty(), ValidationMessageConstants.MSG_EXERCISE_EMPTY, Constants.BAD_REQUEST);
        }
    }

    //Circuit edit validation
    private void allowOrRestrictCircuitEdit(Long circuitId) {
        List<CircuitSchedule> circuitScheduleList = circuitScheduleRepository.findByCircuitCircuitId(circuitId);
        if (!circuitScheduleList.isEmpty()) {
            List<Long> workoutIdList = circuitScheduleList.stream().map(circuitSchedule -> circuitSchedule.getWorkout().getWorkoutId()).collect(Collectors.toList());
            List<String> statuslist = Arrays.asList(new String[]{InstructorConstant.PUBLISH, InstructorConstant.UNPUBLISH, InstructorConstant.BLOCK});

            List<WorkoutMapping> programWorkoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdInAndProgramsStatusIn(workoutIdList, statuslist);
            if (programWorkoutMappingList.size() > 0) {
                //throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_CIRCUIT_IN_RESTRICT_EDIT_PROGRAM, MessageConstants.ERROR);
                List<Long> programIdList = programWorkoutMappingList.stream().map(mapping -> mapping.getPrograms().getProgramId()).collect(Collectors.toList());

                long subscriptionCount = subscriptionService.getOverallActiveSubscriptionCountForProgramsList(programIdList);
//                if (subscriptionCount > 0) {
//                    throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_CIRCUIT_IN_ACTIVE_SUBCRIPTION_PROGRAM, MessageConstants.ERROR);
//                }
            }
        }
    }

    //Reflect Circuit changes across Workout
    private void allowOrRestrictChangesAcrossWorkouts(Long circuitId, Boolean reflectChangesAcrossPrograms) {
        List<CircuitSchedule> circuitScheduleList = circuitScheduleRepository.findByCircuitCircuitId(circuitId);
        if (!circuitScheduleList.isEmpty()) {
            List<Long> workoutIdList = circuitScheduleList.stream().map(circuitSchedule -> circuitSchedule.getWorkout().getWorkoutId()).collect(Collectors.toList());

            List<String> statuslist = Arrays.asList(new String[]{InstructorConstant.PLAN, InstructorConstant.UPLOAD, InstructorConstant.SCHEDULE, InstructorConstant.PRICE, InstructorConstant.PRE_PUBLISH});

            List<WorkoutMapping> programWorkoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdInAndProgramsStatusIn(workoutIdList, statuslist);

            if (programWorkoutMappingList.size() > 1 && reflectChangesAcrossPrograms == null) {
                throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.MSG_CIRCUIT_IN_INPROGRESS_PROGRAM, MessageConstants.ERROR);
            } else if (programWorkoutMappingList.size() > 1 && reflectChangesAcrossPrograms == Boolean.FALSE) {
                throw new ApplicationException(Constants.CAN_EDIT, MessageConstants.MSG_CIRCUIT_CREATE_NEW, MessageConstants.ERROR);
            }
        }
    }

    private void constructCircuit(Circuit circuit, CircuitModel circuitModel) {
        User user = userComponents.getUser();
        String circuitTitle = circuitModel.getTitle();

        //Duplicate Circuit title validation
        boolean isNewCircuit = false;
        if (circuit.getCircuitId() == null || circuit.getCircuitId() == 0) {
            isNewCircuit = true;
        }

        /*
        //Circuit duplicate title validation
        if (isNewCircuit || (!isNewCircuit && !circuit.getTitle().equalsIgnoreCase(circuitTitle))) {
            Circuit circuitWithSameTitle = circuitRepository.findByOwnerUserIdAndTitle(user.getUserId(), circuitTitle);
            if (circuitWithSameTitle != null) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CIRCUIT_DUPLICATE_TITLE, MessageConstants.ERROR);
            }
        }*/

       /* List<Circuit> circuits = circuitRepository.findByOwnerUserIdAndTitle(user.getUserId(), circuitModel.getTitle());
        if(!circuits.isEmpty()){
            throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_CIRCUIT_DUPLICATE_TITLE, null);
        }*/

        circuit.setTitle(circuitModel.getTitle());
        circuit.setOwner(user);

        /*Images images = imageRepository.findByImageId(circuitModel.getImageId());
        if(images == null){
            throw new ApplicationException(Constants.BAD_REQUEST,ValidationMessageConstants.MSG_IMAGE_NOT_FOUND,MessageConstants.ERROR);
        }

        circuit.setThumbnail(images);*/

        if (circuitModel.getExerciseSchedules().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_EXERCISE_SCHEDULE_EMPTY, MessageConstants.ERROR);
        }
        regulateRestVideoInExerciseSchedule(circuitModel.getExerciseSchedules());

        //Saving exercise schedulers
        Set<Long> newScheduleOrderSet = new HashSet<Long>();
        Set<ExerciseSchedulers> exerciseSchedulerList = new HashSet<ExerciseSchedulers>();
        for (ExerciseMappingModel exerciseModel : circuitModel.getExerciseSchedules()) {

            //Schedule Order validation
            if (exerciseModel.getOrder() == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SCHEDULE_ORDER_MISSING, MessageConstants.ERROR);
            }
            if (exerciseModel.getOrder().intValue() <= 0) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SCHEDULE_ORDER_CANT_BE_ZERO, MessageConstants.ERROR);
            }
            int oldUniqueScheduleOrderSize = newScheduleOrderSet.size();
            Long newScheduleOrder = exerciseModel.getOrder();
            newScheduleOrderSet.add(newScheduleOrder);
            int newUniqueScheduleOrderSize = newScheduleOrderSet.size();
            if (oldUniqueScheduleOrderSize == newUniqueScheduleOrderSize || newScheduleOrder > circuitModel.getExerciseSchedules().size()) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SCHEDULE_ORDER_INCORRECT, MessageConstants.ERROR);
            }

            ExerciseSchedulers scheduler = new ExerciseSchedulers();
            if (exerciseModel.getScheduleId() != null && exerciseModel.getScheduleId() != 0) {
                scheduler = exerciseScheduleRepository.findByExerciseScheduleId(exerciseModel.getScheduleId());
            }
            if (exerciseModel.isRestVideo()) {
                WorkoutRestVideos workoutRestVideos = workoutRestRepository.findByWorkoutRestVideoId(exerciseModel.getWorkoutRestVideo().getWorkoutRestVideoId());
                if (workoutRestVideos == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_REST_ID_INVALID, MessageConstants.ERROR);
                }
                scheduler.setWorkoutRestVideo(workoutRestVideos);
            } else if(exerciseModel.isAudio()){
                if(exerciseModel.getVoiceOverId() == null && exerciseModel.getVoiceOverId() == 0){
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_ID_INVALID, null);
                }
                VoiceOver voiceOver = voiceOverRepository.findByVoiceOverId(exerciseModel.getVoiceOverId());
                if (voiceOver == null){
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_NOT_FOUND, null);
                }
                scheduler.setVoiceOver(voiceOver);
            }else {
                Optional<Exercises> exercise = exerciseRepository.findById(exerciseModel.getExerciseId());
                ValidationUtils.throwException(!exercise.isPresent(), ValidationMessageConstants.MSG_EXERCISE_NOT_FOUND, Constants.BAD_REQUEST);
                scheduler.setDescription(exerciseModel.getDescription());
                scheduler.setExercise(exercise.get());
                scheduler.setLoopCount(exerciseModel.getLoopCount());
                scheduler.setTitle(exerciseModel.getTitle());
            }
            scheduler.setIsAudio(exerciseModel.isAudio());
            scheduler.setCircuit(circuit);
            scheduler.setOrder(exerciseModel.getOrder());
            exerciseSchedulerList.add(scheduler);
        }
        circuit.setIsAudio(circuitModel.isAudio());
        circuit.setExerciseSchedules(exerciseSchedulerList);
        validateFlaggedVideosInCircuit(circuitModel.getExerciseSchedules());
    }

    //Regulating rest distribution in a circuit
    private void regulateRestVideoInExerciseSchedule(List<ExerciseMappingModel> schedules) {
        Collections.sort(schedules);
        if (schedules.size() > 0) {
            //First exercise in a circuit can not be rest
            if (schedules.get(0).isRestVideo()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FIRST_EXERCISE_NO_REST, MessageConstants.ERROR);
            }
            //A circuit can not have 2 consecutive rest
            for (int i = 0; i < schedules.size() - 1; i++) {
                if (schedules.get(i).isRestVideo() && schedules.get(i + 1).isRestVideo()) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONSECUTIVE_EXERCISE_NO_REST, MessageConstants.ERROR);
                }
            }
        }
    }

    /**
     * Validation to check if a flagged video in block state is added to the circuit
     *
     * @param exerciseSchedules
     */
    private void validateFlaggedVideosInCircuit(List<ExerciseMappingModel> exerciseSchedules) {
        List<Long> exerciseIdList = new ArrayList<>();
        for (ExerciseMappingModel schedule : exerciseSchedules) {
            if (!schedule.isRestVideo()) {
                exerciseIdList.add(schedule.getExerciseId());
            }
        }
        exerciseIdList = exerciseIdList.stream().distinct().collect(Collectors.toList());

        boolean containsFlaggedVideo = false;
        if (!exerciseIdList.isEmpty()) {
            containsFlaggedVideo = flaggedExercisesSummaryRepository.existsByExerciseExerciseIdInAndFlagStatus(exerciseIdList, KeyConstants.KEY_BLOCK);
        }
        if (containsFlaggedVideo) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CIRCUIT_CANT_ADD_FLAGGED_VIDEO, MessageConstants.ERROR);
        }
    }

    /**
     * Creating response for circuit details
     *
     * @param circuit
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    private CircuitResponseView constructCircuitResponseView(Circuit circuit) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        User user = userComponents.getUser();
        CircuitResponseView circuitView = new CircuitResponseView();
        long duration = 0;

        if(circuit.getIsAudio()!= null && circuit.getIsAudio()){
            boolean isAudio = (circuit.getIsAudio() != null && circuit.getIsAudio().booleanValue()) ? true : false;
            circuitView.setAudio(isAudio);
            circuitView.setCircuitId(circuit.getCircuitId());
            circuitView.setInstructorId(user.getUserId());
            List<AudioResponseView> audioResponseViews = new ArrayList<>();
            for (CircuitAndVoiceOverMapping circuitAndVoiceOverMapping : circuit.getCircuitAndVoiceOverMappings()) {
                AudioResponseView audioResponseView = new AudioResponseView();
                audioResponseView.setAudioId(circuitAndVoiceOverMapping.getVoiceOver().getAudios().getAudioId());
                audioResponseView.setFilePath(circuitAndVoiceOverMapping.getVoiceOver().getAudios().getFilePath());
                audioResponseView.setDuration(circuitAndVoiceOverMapping.getVoiceOver().getAudios().getDuration());
                audioResponseView.setTitle(circuitAndVoiceOverMapping.getVoiceOver().getTitle());
                audioResponseView.setVoiceOverId(circuitAndVoiceOverMapping.getVoiceOver().getVoiceOverId());
                audioResponseView.setCircuitAndVoiceOverMappingId(circuitAndVoiceOverMapping.getCircuitAndVoiceOverMappingId());
                audioResponseViews.add(audioResponseView);
                duration += circuitAndVoiceOverMapping.getVoiceOver().getAudios().getDuration();
            }
            circuitView.setAudioResponseViews(audioResponseViews);
        }else {
            circuitView.setCircuitId(circuit.getCircuitId());
            circuitView.setTitle(circuit.getTitle());
            circuitView.setInstructorId(user.getUserId());
        /*if (circuit.getThumbnail() != null){
            circuitView.setThumbnailUrl(circuit.getThumbnail().getImagePath());
        }*/

            if (circuit.getExerciseSchedules() != null && circuit.getExerciseSchedules().size() > 0) {
                List<ExerciseScheduleView> schedules = new ArrayList<ExerciseScheduleView>();
                for (ExerciseSchedulers scheduler : circuit.getExerciseSchedules()) {
                    ExerciseScheduleView exerciseScheduleView = new ExerciseScheduleView();
                    exerciseScheduleView.setScheduleId(scheduler.getExerciseScheduleId());
                    exerciseScheduleView.setTitle(scheduler.getTitle());
                    exerciseScheduleView.setOrder(scheduler.getOrder());
                    exerciseScheduleView.setDescription(scheduler.getDescription());
                    if (scheduler.getExercise() != null) {
                        exerciseScheduleView.setExerciseId(scheduler.getExercise().getExerciseId());
                        exerciseScheduleView.setRestVideo(false);

                        VideoManagement videoManagement = scheduler.getExercise().getVideoManagement();

                        String vimeoUrl = null;
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
                        exerciseScheduleView.setUrl(vimeoUrl);

                        if (!vimeoId.isEmpty()) {
                            exerciseScheduleView.setParsedUrl(vimeoService.getVimeoUrlFromId(Long.parseLong(vimeoId)));
                        }
                        if (scheduler.getExercise().getVideoManagement() != null) {
                            int exerciseDuration = (int) (scheduler.getExercise().getVideoManagement().getDuration());

                            if (scheduler.getLoopCount() != null && scheduler.getLoopCount() > 0) {
                                exerciseScheduleView.setLoopCount(scheduler.getLoopCount());
                                //Repeat count Change :  no of times video is played
                                exerciseDuration =  (int) (exerciseDuration * scheduler.getLoopCount());
                            }
                            exerciseScheduleView.setDuration(exerciseDuration);

                            duration += exerciseDuration;

                            if (scheduler.getExercise().getVideoManagement().getThumbnail() != null) {
                                exerciseScheduleView.setImageId(scheduler.getExercise().getVideoManagement().getThumbnail().getImageId());
                                exerciseScheduleView.setThumbnailUrl(scheduler.getExercise().getVideoManagement().getThumbnail().getImagePath());
                            }
                            exerciseScheduleView.setVideoUploadStatus(scheduler.getExercise().getVideoManagement().getUploadStatus());
                            /**
                             * If the video processing was failed first time, marking it as upload status
                             * If the video processing was failed more than one time, marking it as re-upload status
                             */
                            if (scheduler.getExercise().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_PROCESSING_FAILED)) {
                                exerciseScheduleView.setVideoUploadStatus(VideoUploadStatus.UPLOAD);
                            } else if (scheduler.getExercise().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED)) {
                                exerciseScheduleView.setVideoUploadStatus(VideoUploadStatus.REUPLOAD);
                            }
                        }

                        //Support Video mapping construction from Circuit's exercise schedule
                        VideoManagement supportVideoManagement = scheduler.getExercise().getSupportVideoManagement();
                        if (supportVideoManagement != null) {
                            SupportingVideoMappingModel supportingVideoModel = new SupportingVideoMappingModel();
                            supportingVideoModel.setTitle(supportVideoManagement.getTitle());
                            supportingVideoModel.setDescription(supportVideoManagement.getDescription());

                            String supportingVimeoUrl = supportVideoManagement.getUrl();
                            supportingVideoModel.setUrl(supportingVimeoUrl);

                            String supportingVimeoId = "";

                            if (supportingVimeoUrl != null) {
                                try {
                                    if (supportingVimeoUrl.contains("/")) {
                                        String[] videoIds = supportingVimeoUrl.split("/");
                                        supportingVimeoId = videoIds[2];
                                    }
                                } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                                    log.error("The index you have entered is invalid", arrayIndexOutOfBoundsException);
                                } catch (Exception exception) {
                                    log.error("Unexpected error", exception);
                                }
                            }

                            if (!supportingVimeoId.isEmpty()) {
                                supportingVideoModel.setParsedUrl(vimeoService.getVimeoUrlFromId(Long.parseLong(supportingVimeoId)));
                            }

                            supportingVideoModel.setDuration(supportVideoManagement.getDuration());
                            if (supportVideoManagement.getThumbnail() != null) {
                                supportingVideoModel.setImageId(supportVideoManagement.getThumbnail().getImageId());
                                supportingVideoModel.setThumbnailUrl(supportVideoManagement.getThumbnail().getImagePath());
                            }
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
                            exerciseScheduleView.setSupportingVideoMappingModel(supportingVideoModel);
                        }

                        if (scheduler.getExercise().getEquipments() != null) {
                            exerciseScheduleView.setEquipments(scheduler.getExercise().getEquipments());
                        }
                        boolean isExerciseBlocked = flaggedExercisesSummaryRepository.existsByExerciseExerciseIdAndFlagStatus(scheduler.getExercise().getExerciseId(), KeyConstants.KEY_BLOCK);
                        exerciseScheduleView.setExerciseBlocked(isExerciseBlocked);
                    } else if (scheduler.getIsAudio() != null && scheduler.getIsAudio()) {
                        boolean isAudio = (scheduler.getIsAudio() != null && scheduler.getIsAudio().booleanValue()) ? true : false;
                        exerciseScheduleView.setAudio(isAudio);
                        AudioResponseView audioResponseView = new AudioResponseView();
                        audioResponseView.setAudioId(scheduler.getVoiceOver().getAudios().getAudioId());
                        audioResponseView.setFilePath(scheduler.getVoiceOver().getAudios().getFilePath());
                        audioResponseView.setDuration(scheduler.getVoiceOver().getAudios().getDuration());
                        audioResponseView.setTitle(scheduler.getVoiceOver().getTitle());
                        audioResponseView.setVoiceOverId(scheduler.getVoiceOver().getVoiceOverId());
                        exerciseScheduleView.setAudioResponseView(audioResponseView);
                        duration += scheduler.getVoiceOver().getAudios().getDuration();
                    } else {
                        exerciseScheduleView.setRestVideo(true);
                        RestResponseView restVideo = new RestResponseView();
                        restVideo.setWorkoutRestVideoId(scheduler.getWorkoutRestVideo().getWorkoutRestVideoId());
                        restVideo.setRestTime(scheduler.getWorkoutRestVideo().getRestTime());

                        VideoManagement videoManagement = scheduler.getWorkoutRestVideo().getVideoManagement();

                        String vimeoUrl = null;
                        String vimeoId = "";

                        if (videoManagement != null) {
                            vimeoUrl = videoManagement.getUrl();
                            if (vimeoUrl != null) {
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
                        restVideo.setUrl(vimeoUrl);

                        if (!vimeoId.isEmpty()) {
                            restVideo.setParsedUrl(vimeoService.getVimeoUrlFromId(Long.parseLong(vimeoId)));
                        }
                        restVideo.setThumbnail(scheduler.getWorkoutRestVideo().getVideoManagement().getThumbnail());
                        exerciseScheduleView.setWorkoutRestVideo(restVideo);
                        duration += scheduler.getWorkoutRestVideo().getRestTime();
                    }
                    schedules.add(exerciseScheduleView);
                }
                Collections.sort(schedules);
                circuitView.setExerciseSchedules(schedules);
                if (circuit.getExerciseSchedules() != null && circuit.getExerciseSchedules().size() > 0) {
                    circuitView.setExerciseCount(exerciseScheduleRepository.countByCircuitAndWorkoutRestVideo(circuit, null));
                }
                circuitView.setStatus(getCircuitStatus(circuit));
            }
        }
        circuitView.setDuration(duration);
        return circuitView;
    }

    /**
     * Get circuit availability
     * @param circuit
     * @return
     */
    public String getCircuitStatus(Circuit circuit) {
        String status = InstructorConstant.AVAILABLE;
        boolean isUpdated = false;
        List<CircuitSchedule> circuitScheduleList = circuitScheduleRepository.findByCircuitCircuitId(circuit.getCircuitId());
        if (!circuitScheduleList.isEmpty()) {
            List<Long> workoutIdList = circuitScheduleList.stream().map(circuitSchedule -> circuitSchedule.getWorkout().getWorkoutId()).collect(Collectors.toList());
            List<String> statuslist = Arrays.asList(new String[]{InstructorConstant.PUBLISH, InstructorConstant.UNPUBLISH, InstructorConstant.BLOCK});
            List<WorkoutMapping> programWorkoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdInAndProgramsStatusIn(workoutIdList, statuslist);
            if (programWorkoutMappingList.size() > 0) {
                status = InstructorConstant.PUBLISH;
                isUpdated = true;
            }
            if (!isUpdated) {
                List<String> inProgressStatuslist = Arrays.asList(new String[]{InstructorConstant.PLAN, InstructorConstant.UPLOAD, InstructorConstant.SCHEDULE, InstructorConstant.PRICE, InstructorConstant.PRE_PUBLISH});
                programWorkoutMappingList = workoutMappingRepository.findByWorkoutWorkoutIdInAndProgramsStatusIn(workoutIdList, inProgressStatuslist);
                if (programWorkoutMappingList.size() > 1) {
                    status = InstructorConstant.INPROGRESS;
                }
            }
        }
        return status;
    }

    /**
     * Get details of a circuit
     *
     * @param circuitId
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    public CircuitResponseView getCircuit(Long circuitId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.info("Get circuit starts.");
        long apiStartTimeMillis = new Date().getTime();

        User user = userComponents.getUser();
        if (circuitId == null || circuitId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_ID_NULL, MessageConstants.ERROR);
        }
        Circuit circuit = circuitRepository.findByCircuitId(circuitId);
        if (circuit == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_NOT_FOUND, null);
        }
        log.info("query to get circuit : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        CircuitResponseView circuitResponseView = constructCircuitResponseView(circuit);
        log.info("Circuit response view construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get circuit ends.");

        return circuitResponseView;
    }

    /**
     * Get all circuits of instructor. Used in circuit library
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
    public Map<String, Object> getAllInstructorCircuits(int pageNo, int pageSize, String sortOrder, String sortBy, Optional<String> searchName) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        if (!(sortBy.equalsIgnoreCase(SearchConstants.TITLE) || sortBy.equalsIgnoreCase(SearchConstants.CREATED_DATE))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_ERROR_SORT_BY, null);
        }

        if (!(sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC) || sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }

        Page<Circuit> circuitPage = null;
        User user = userComponents.getUser();

        Sort sort = getCircuitLibrarySortCriteria(sortBy);
        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            sort = sort.descending();
        }
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, sort);

        if (searchName.isPresent() && !searchName.get().isEmpty()) {
            circuitPage = circuitRepository.findByOwnerUserIdAndTitleIgnoreCaseContaining(user.getUserId(), searchName.get(), pageRequest);
        } else {
            circuitPage = circuitRepository.findByOwnerUserId(user.getUserId(), pageRequest);
        }

        int fromIndex = (pageNo - 1) * pageSize;

        if (circuitPage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        List<CircuitLibraryView> circuitLibraryViewList = new ArrayList<CircuitLibraryView>();
        for (Circuit circuit : circuitPage) {
            circuitLibraryViewList.add(constructCircuitLibraryResponse(circuit));
        }

        Map<String, Object> map = new HashMap<>();
        map.put(KeyConstants.KEY_TOTAL_COUNT, circuitPage.getTotalElements());
        map.put(KeyConstants.KEY_CIRCUITS, circuitLibraryViewList);

        return map;
    }

    private CircuitLibraryView constructCircuitLibraryResponse(Circuit circuit) {
        CircuitLibraryView circuitLibraryView = new CircuitLibraryView();

        circuitLibraryView.setCircuitId(circuit.getCircuitId());
        circuitLibraryView.setTitle(circuit.getTitle());
        /*if(circuit.getThumbnail() != null){
            circuitLibraryView.setCircuitThumbnailUrl(circuit.getThumbnail().getImagePath());
        }*/

        long circuitDuration = 0;
        int exerciseCount = 0;
        List<String> exerciseThumbnails = new ArrayList<>();
        List<Equipments> equipmentsList = new ArrayList<>();
        Set<ExerciseSchedulers> exerciseSchedules = circuit.getExerciseSchedules();

        boolean isVideoProcessingPending = false;

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
                        isVideoProcessingPending = true;
                    }
                }

                if (schedule.getExercise().getEquipments() != null) {
                    equipmentsList.addAll(schedule.getExercise().getEquipments());
                }

                exerciseCount++;
            } else {
                exerciseDuration = schedule.getWorkoutRestVideo().getRestTime();
            }
            circuitDuration += exerciseDuration;
        }

        circuitLibraryView.setDuration(circuitDuration);
        circuitLibraryView.setExerciseCount(exerciseCount);
        circuitLibraryView.setExerciseThumbnails(exerciseThumbnails);

        List<Long> equipmentsIdList = equipmentsList.stream().map(equipment -> equipment.getEquipmentId()).distinct().collect(Collectors.toList());
        List<Equipments> equipments = equipmentsRepository.findByEquipmentIdIn(equipmentsIdList);
        circuitLibraryView.setEquipments(equipments);

        circuitLibraryView.setVideoProcessingPending(isVideoProcessingPending);

        return circuitLibraryView;
    }

    private Sort getCircuitLibrarySortCriteria(String sortBy) throws ApplicationException {
        Sort sort;
        if (sortBy.equalsIgnoreCase(SearchConstants.TITLE)) {
            sort = Sort.by(SearchConstants.TITLE);
        } else {
            sort = Sort.by("circuitId");
        }
        return sort;
    }

    /**
     * Delete a circuit and make it anonymous.
     *
     * @param circuitId
     * @return
     * @throws ApplicationException
     */
    public void deleteCircuit(Long circuitId) {
        User user = userComponents.getUser();
        deleteCircuit(circuitId, user);
    }

    public void deleteCircuit(Long circuitId, User user) {

        if (circuitId == null || circuitId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_ID_NULL, MessageConstants.ERROR);
        }
        Circuit circuit = circuitRepository.findByCircuitIdAndOwnerUserId(circuitId, user.getUserId());
        if (circuit == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_NOT_FOUND, null);
        }

        List<CircuitSchedule> circuitScheduleList = circuitScheduleRepository.findByCircuitCircuitId(circuitId);
        if (!circuitScheduleList.isEmpty()) {
            throw new ApplicationException(Constants.FORBIDDEN, MessageConstants.MSG_CIRCUIT_DELETION_FAILED, MessageConstants.ERROR);
        }

        List<ExerciseSchedulers> exerciseSchedulerList = exerciseScheduleRepository.findByCircuitCircuitId(circuitId);
        if (!exerciseSchedulerList.isEmpty()) {
            List<Long> exerciseSchedulerIdList = exerciseSchedulerList.stream().map(exerciseScheduler -> exerciseScheduler.getExerciseScheduleId()).collect(Collectors.toList());

            List<ExerciseCompletion> exerciseCompletionList = exerciseCompletionRepository.findByExerciseScheduleIdIn(exerciseSchedulerIdList);
            exerciseCompletionRepository.deleteInBatch(exerciseCompletionList);

            exerciseScheduleRepository.deleteInBatch(exerciseSchedulerList);
        }

        fitwiseUtils.makeCircuitAnonymous(circuit);

        circuitRepository.save(circuit);

    }

    /**
     * Remove an exercise from a circuit. Exercise will not be deleted. Exercise will not be related to the circuit.
     *
     * @param exerciseScheduleId
     * @return
     */
    @Transactional
    public void removeExercise(Long exerciseScheduleId) {

        User user = userComponents.getUser();
        ExerciseSchedulers exerciseSchedulers = exerciseScheduleRepository.findByExerciseScheduleId(exerciseScheduleId);
        if (exerciseSchedulers == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
        }
        regulateExerciseRestDuringDelete(exerciseSchedulers);
        
        List<ExerciseCompletion> exerciseCompletion = exerciseCompletionRepository.findByExerciseScheduleId(exerciseSchedulers.getExerciseScheduleId());
        exerciseCompletionRepository.deleteInBatch(exerciseCompletion);

        exerciseScheduleRepository.deleteByExerciseScheduleId(exerciseScheduleId);
    }

    //Exercise rest regulation during exercise removal from a circuit
    private void regulateExerciseRestDuringDelete(ExerciseSchedulers exerciseSchedulers) {
        Long deleteOrder = exerciseSchedulers.getOrder();

        Set<ExerciseSchedulers> exerciseSchedulerSet = exerciseSchedulers.getCircuit().getExerciseSchedules();
        List<ExerciseSchedulers> exerciseSchedulerList = new ArrayList<>();

        for (ExerciseSchedulers exerciseScheduler : exerciseSchedulerSet) {
            if (exerciseScheduler.getOrder() != deleteOrder) {
                exerciseSchedulerList.add(exerciseScheduler);
            }
        }
        exerciseSchedulerList.sort((schedule1, schedule2) -> (int) (schedule1.getOrder() - schedule2.getOrder()));

        if (exerciseSchedulerList.size() > 0) {
            //First exercise in a circuit can not be rest
            if (exerciseSchedulerList.get(0).getWorkoutRestVideo() != null) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_EX_SCH_REST_RESTRICTION, null);
            }
            //A circuit can not have 2 consecutive rest
            for (int i = 0; i < exerciseSchedulerList.size() - 1; i++) {
                if (exerciseSchedulerList.get(i).getWorkoutRestVideo() != null && exerciseSchedulerList.get(i + 1).getWorkoutRestVideo() != null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONSECUTIVE_EXERCISE_NO_REST, MessageConstants.ERROR);
                }
            }
        }
    }

    /**
     * Remove an circuit from a workout. circuit will not be deleted. circuit will not be related to the workout.
     *
     * @param circuitScheduleId
     * @return
     */
    @Transactional
    public void removeCircuitFromWorkout(Long circuitScheduleId) {
        User user = userComponents.getUser();
        Optional<CircuitSchedule> circuitScheduleOptional = circuitScheduleRepository.findById(circuitScheduleId);
        if (!circuitScheduleOptional.isPresent()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
        }
        CircuitSchedule circuitSchedule = circuitScheduleOptional.get();

        Workouts workout = circuitSchedule.getWorkout();
        validationService.validateIfWorkoutBelongsToInstructor(user.getUserId(), workout.getWorkoutId());

        regulateCircuitRestDuringDelete(circuitSchedule);

        if (!circuitSchedule.isRestCircuit()) {
            Long circuitId = circuitSchedule.getCircuit().getCircuitId();
            Circuit circuit = circuitRepository.findByCircuitIdAndOwnerUserId(circuitId, user.getUserId());
            if (circuit == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_NOT_FOUND, null);
            }
        }

        List<ExerciseCompletion> exerciseCompletion = exerciseCompletionRepository.findByCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
        exerciseCompletionRepository.deleteInBatch(exerciseCompletion);

        List<CircuitCompletion> circuitCompletions = circuitCompletionRepository.findByCircuitScheduleId(circuitSchedule.getCircuitScheduleId());
        circuitCompletionRepository.deleteInBatch(circuitCompletions);

        circuitScheduleRepository.deleteByCircuitScheduleId(circuitSchedule.getCircuitScheduleId());

    }

    //Circuit rest regulation during Circuit schedule removal from a workout
    private void regulateCircuitRestDuringDelete(CircuitSchedule circuitSchedule) {
        Long deleteOrder = circuitSchedule.getOrder();

        Set<CircuitSchedule> circuitSchedulerSet = circuitSchedule.getWorkout().getCircuitSchedules();
        List<CircuitSchedule> circuitSchedulerList = new ArrayList<>();

        for (CircuitSchedule schedule : circuitSchedulerSet) {
            if (schedule.getOrder() != deleteOrder) {
                circuitSchedulerList.add(schedule);
            }
        }
        circuitSchedulerList.sort((schedule1, schedule2) -> (int) (schedule1.getOrder() - schedule2.getOrder()));

        if (circuitSchedulerList.size() > 0) {
            //First circuit in a workout can not be a rest circuit
            if (circuitSchedulerList.get(0).isRestCircuit()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FIRST_CIRCUIT_NO_REST, MessageConstants.ERROR);
            }
            //A workout can not have 2 consecutive rest circuit
            for (int i = 0; i < circuitSchedulerList.size() - 1; i++) {
                if (circuitSchedulerList.get(i).isRestCircuit() && circuitSchedulerList.get(i + 1).isRestCircuit()) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONSECUTIVE_CIRCUIT_NO_REST, MessageConstants.ERROR);
                }
            }
            //Last circuit in a workout can not be a rest circuit
            if (circuitSchedulerList.get(circuitSchedulerList.size() - 1).isRestCircuit()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_LAST_CIRCUIT_NO_REST, MessageConstants.ERROR);
            }
        }

    }

    /**
     * Get allowed values of Circuit repeat counts
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    public List<Long> getCircuitRepeatCounts() {
        List<CircuitRepeatCount> repeatCounts = circuitRepeatCountRepository.findAll();
        if(repeatCounts.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, "", "");
        }
        List<Long> circuitRepeatList = repeatCounts.stream().map(repeatCount -> repeatCount.getRepeatCount()).collect(Collectors.toList());
        return circuitRepeatList;
    }

    /**
     * Create/edit a circuit
     *
     * @param circuitModel
     * @return
     * @throws ApplicationException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    @Transactional
    public CircuitResponseViewWithSetsAndReps createCircuitWithSetsAndReps(CircuitModelWithSetsAndReps circuitModel) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.info("Create circuit with sets and reps starts");
        long start = new Date().getTime();
        long profilingStartTimeInMillis = new Date().getTime();
        long profilingEndTimeInMillis;
        validateCircuitModelWithSetsAndReps(circuitModel);
        User user = userComponents.getUser();

        Circuit circuit = new Circuit();
        //Check if circuit already exists
        if (circuitModel.getCircuitId() != null && circuitModel.getCircuitId() != 0) {
            circuit = circuitRepository.findByCircuitIdAndOwnerUserId(circuitModel.getCircuitId(), user.getUserId());
            if (circuit == null) {
                throw new ApplicationException(Constants.NOT_FOUND, ValidationMessageConstants.MSG_CIRCUIT_NOT_FOUND, null);
            }

            allowOrRestrictCircuitEdit(circuit.getCircuitId());

            //allowOrRestrictChangesAcrossWorkouts(circuit.getCircuitId(), circuitModel.getReflectChangesAcrossWorkouts());

        }
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Validations : time taken in millis :"+(profilingEndTimeInMillis-profilingStartTimeInMillis));

        profilingStartTimeInMillis = new Date().getTime();
        if(circuitModel.isAudio()){
            if(circuitModel.getVoiceOverIds() == null && circuitModel.getVoiceOverIds().isEmpty()){
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_ID_INVALID, null);
            }

            /*List<VoiceOver> voiceOvers = voiceOverRepository.findByVoiceOverIdIn(circuitModel.getVoiceOverIds());
            if (voiceOvers == null & voiceOvers.isEmpty()){
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_NOT_FOUND, null);
            }*/

            Set<CircuitAndVoiceOverMapping> circuitAndVoiceOverMappings = new HashSet<>();
            for (Long voiceOverId :circuitModel.getVoiceOverIds()) {
                VoiceOver voiceOver = voiceOverRepository.findByVoiceOverId(voiceOverId);
                if (voiceOver == null){
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_NOT_FOUND, null);
                }
                CircuitAndVoiceOverMapping circuitAndVoiceOverMapping = new CircuitAndVoiceOverMapping();
                circuitAndVoiceOverMapping.setVoiceOver(voiceOver);
                circuitAndVoiceOverMappings.add(circuitAndVoiceOverMapping);
                circuitAndVoiceOverMapping.setCircuit(circuit);
            }
            circuit.setOwner(user);
            circuit.setIsAudio(circuitModel.isAudio());
            circuit.setCircuitAndVoiceOverMappings(circuitAndVoiceOverMappings);
        }else {
            constructCircuitWithSetsAndReps(circuit, circuitModel);
        }
        circuitRepository.save(circuit);
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Constructing circuit : Time taken in millis : "+(profilingEndTimeInMillis-profilingStartTimeInMillis));
        CircuitResponseViewWithSetsAndReps circuitResponseViewWithSetsAndReps = constructCircuitResponseViewWithSetsReps(circuit);
        profilingEndTimeInMillis = new Date().getTime();
        log.info("Create circuit with sets and reps : Total time taken in millis : "+(profilingEndTimeInMillis-start));
        log.info("Create circuit with sets and reps ends");
        return circuitResponseViewWithSetsAndReps;
    }

    private void constructCircuitWithSetsAndReps(Circuit circuit, CircuitModelWithSetsAndReps circuitModel) {
        User user = userComponents.getUser();

        String circuitTitle = circuitModel.getTitle();

        //Duplicate Circuit title validation
        boolean isNewCircuit = false;
        if (circuit.getCircuitId() == null || circuit.getCircuitId() == 0) {
            isNewCircuit = true;
        }

        circuit.setTitle(circuitModel.getTitle());
        circuit.setOwner(user);

        if (circuitModel.getExerciseMappingModelWithSetsAndReps().isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_EXERCISE_SCHEDULE_EMPTY, MessageConstants.ERROR);
        }
        regulateRestVideoInExerciseScheduleWithSetsAndReps(circuitModel.getExerciseMappingModelWithSetsAndReps());

        //Saving exercise schedulers
        Set<Long> newScheduleOrderSet = new HashSet<Long>();
        Set<ExerciseSchedulers> exerciseSchedulerList = new HashSet<ExerciseSchedulers>();
        for (ExerciseMappingModelWithSetsAndReps exerciseModel : circuitModel.getExerciseMappingModelWithSetsAndReps()) {

            //Schedule Order validation
            if (exerciseModel.getOrder() == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SCHEDULE_ORDER_MISSING, MessageConstants.ERROR);
            }
            if (exerciseModel.getOrder().intValue() <= 0) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SCHEDULE_ORDER_CANT_BE_ZERO, MessageConstants.ERROR);
            }
            int oldUniqueScheduleOrderSize = newScheduleOrderSet.size();
            Long newScheduleOrder = exerciseModel.getOrder();
            newScheduleOrderSet.add(newScheduleOrder);
            int newUniqueScheduleOrderSize = newScheduleOrderSet.size();
            if (oldUniqueScheduleOrderSize == newUniqueScheduleOrderSize || newScheduleOrder > circuitModel.getExerciseMappingModelWithSetsAndReps().size()) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_SCHEDULE_ORDER_INCORRECT, MessageConstants.ERROR);
            }

            ExerciseSchedulers scheduler = new ExerciseSchedulers();
            if (exerciseModel.getScheduleId() != null && exerciseModel.getScheduleId() != 0) {
                scheduler = exerciseScheduleRepository.findByExerciseScheduleId(exerciseModel.getScheduleId());
            }
            if (exerciseModel.isRestVideo()) {
                WorkoutRestVideos workoutRestVideos = workoutRestRepository.findByWorkoutRestVideoId(exerciseModel.getWorkoutRestVideo().getWorkoutRestVideoId());
                if (workoutRestVideos == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_REST_ID_INVALID, MessageConstants.ERROR);
                }
                scheduler.setWorkoutRestVideo(workoutRestVideos);
            }else if(exerciseModel.isAudio()) {
                if (exerciseModel.getVoiceOverId() == null && exerciseModel.getVoiceOverId() == 0) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_ID_INVALID, null);
                }
                VoiceOver voiceOver = voiceOverRepository.findByVoiceOverId(exerciseModel.getVoiceOverId());
                if (voiceOver == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_NOT_FOUND, null);
                }
                scheduler.setVoiceOver(voiceOver);
            } else {

//                if (!(SearchConstants.TIMED.equalsIgnoreCase(exerciseModel.getPlayType()) || SearchConstants.REPS.equalsIgnoreCase(exerciseModel.getPlayType()))) {
//                    throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_REPS_TIMED_ERROR, null);
//                }
            	if (!(SearchConstants.TIMED.equalsIgnoreCase(exerciseModel.getPlayType()) || SearchConstants.REPS.equalsIgnoreCase(exerciseModel.getPlayType()) || SearchConstants.SECONDS.equalsIgnoreCase(exerciseModel.getPlayType()) || SearchConstants.YARDS.equalsIgnoreCase(exerciseModel.getPlayType()) || SearchConstants.METERS.equalsIgnoreCase(exerciseModel.getPlayType()) ))  
                    
                {
                  throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_REPS_TIMED_ERROR, null);
                }
                Optional<Exercises> exercise = exerciseRepository.findById(exerciseModel.getExerciseId());
                ValidationUtils.throwException(!exercise.isPresent(), ValidationMessageConstants.MSG_EXERCISE_NOT_FOUND, Constants.BAD_REQUEST);
                scheduler.setDescription(exerciseModel.getDescription());
                scheduler.setExercise(exercise.get());
                scheduler.setLoopCount(exerciseModel.getLoopCount());
                scheduler.setTitle(exerciseModel.getTitle());

                scheduler.setPlayType(exerciseModel.getPlayType());
                scheduler.setRepsCount(exerciseModel.getRepsCount());
                scheduler.setSetsCount(exerciseModel.getSetsCount());

            }
            scheduler.setIsAudio(exerciseModel.isAudio());
            scheduler.setCircuit(circuit);
            scheduler.setOrder(exerciseModel.getOrder());
            exerciseSchedulerList.add(scheduler);
        }
        circuit.setIsAudio(circuitModel.isAudio());
        circuit.setExerciseSchedules(exerciseSchedulerList);

        validateFlaggedVideosInCircuitWithSetsAndReps(circuitModel.getExerciseMappingModelWithSetsAndReps());
    }


    /**
     * Creating response for circuit details
     *
     * @param circuit
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    private CircuitResponseViewWithSetsAndReps constructCircuitResponseViewWithSetsReps(Circuit circuit) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        long profilingStartTimeInMillis = new Date().getTime();
        log.info("circuit response construction starts");
        long profilingEndTimeInMillis;
        User user = userComponents.getUser();
        CircuitResponseViewWithSetsAndReps circuitView = new CircuitResponseViewWithSetsAndReps();
        long duration = 0;


        if(circuit.getIsAudio()!= null && circuit.getIsAudio()){
            boolean isAudio = (circuit.getIsAudio() != null && circuit.getIsAudio().booleanValue()) ? true : false;
            circuitView.setAudio(isAudio);
            circuitView.setCircuitId(circuit.getCircuitId());
            circuitView.setInstructorId(user.getUserId());
            List<AudioResponseView> audioResponseViews = new ArrayList<>();
            for (CircuitAndVoiceOverMapping circuitAndVoiceOverMapping : circuit.getCircuitAndVoiceOverMappings()) {
                AudioResponseView audioResponseView = new AudioResponseView();
                audioResponseView.setAudioId(circuitAndVoiceOverMapping.getVoiceOver().getAudios().getAudioId());
                audioResponseView.setFilePath(circuitAndVoiceOverMapping.getVoiceOver().getAudios().getFilePath());
                audioResponseView.setDuration(circuitAndVoiceOverMapping.getVoiceOver().getAudios().getDuration());
                audioResponseView.setTitle(circuitAndVoiceOverMapping.getVoiceOver().getTitle());
                audioResponseView.setVoiceOverId(circuitAndVoiceOverMapping.getVoiceOver().getVoiceOverId());
                audioResponseView.setCircuitAndVoiceOverMappingId(circuitAndVoiceOverMapping.getCircuitAndVoiceOverMappingId());
                audioResponseViews.add(audioResponseView);
                duration += circuitAndVoiceOverMapping.getVoiceOver().getAudios().getDuration();
            }
            circuitView.setAudioResponseViews(audioResponseViews);
            circuitView.setDuration(duration);
        }else {
            circuitView.setCircuitId(circuit.getCircuitId());
            circuitView.setTitle(circuit.getTitle());
            circuitView.setInstructorId(user.getUserId());
        /*if (circuit.getThumbnail() != null){
            circuitView.setThumbnailUrl(circuit.getThumbnail().getImagePath());
        }*/

            if (circuit.getExerciseSchedules() != null && circuit.getExerciseSchedules().size() > 0) {
                List<ExerciseScheduleViewWithSetsAndReps> schedules = new ArrayList<ExerciseScheduleViewWithSetsAndReps>();
                for (ExerciseSchedulers scheduler : circuit.getExerciseSchedules()) {
                    ExerciseScheduleViewWithSetsAndReps exerciseScheduleView = new ExerciseScheduleViewWithSetsAndReps();
                    exerciseScheduleView.setScheduleId(scheduler.getExerciseScheduleId());
                    exerciseScheduleView.setTitle(scheduler.getTitle());
                    exerciseScheduleView.setOrder(scheduler.getOrder());
                    exerciseScheduleView.setDescription(scheduler.getDescription());
                    exerciseScheduleView.setPlayType(scheduler.getPlayType());
                    exerciseScheduleView.setRepsCount(scheduler.getRepsCount());
                    exerciseScheduleView.setSetsCount(scheduler.getSetsCount());

                    if (scheduler.getExercise() != null) {
                        exerciseScheduleView.setExerciseId(scheduler.getExercise().getExerciseId());
                        exerciseScheduleView.setRestVideo(false);

                        VideoManagement videoManagement = scheduler.getExercise().getVideoManagement();

                        String vimeoUrl = null;
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
                        exerciseScheduleView.setUrl(vimeoUrl);

                        long startTimeInMillis = new Date().getTime();

                        if (!vimeoId.isEmpty()) {
                            exerciseScheduleView.setParsedUrl(vimeoService.getVimeoUrlFromId(Long.parseLong(vimeoId)));
                            exerciseScheduleView.setVideoStandards(vimeoService.getVimeoVideos(Long.parseLong(vimeoId)));
                        }
                        profilingEndTimeInMillis = new Date().getTime();
                        log.info("Vimeo url and video standards for exercise video: Time taken in millis : " + (profilingEndTimeInMillis - startTimeInMillis));

                        if (scheduler.getExercise().getVideoManagement() != null) {
                            int exerciseDuration = (int) (scheduler.getExercise().getVideoManagement().getDuration());

                            if (scheduler.getLoopCount() != null && scheduler.getLoopCount() > 0) {
                                exerciseScheduleView.setLoopCount(scheduler.getLoopCount());
                                //Repeat Count Change : Since repeat count is changes as no of times video should play
                                exerciseDuration = (int) (exerciseDuration * scheduler.getLoopCount());
                            }
                            exerciseScheduleView.setDuration(exerciseDuration);

                            duration += exerciseDuration;

                            if (scheduler.getExercise().getVideoManagement().getThumbnail() != null) {
                                exerciseScheduleView.setImageId(scheduler.getExercise().getVideoManagement().getThumbnail().getImageId());
                                exerciseScheduleView.setThumbnailUrl(scheduler.getExercise().getVideoManagement().getThumbnail().getImagePath());
                            }
                            exerciseScheduleView.setVideoUploadStatus(scheduler.getExercise().getVideoManagement().getUploadStatus());
                            /**
                             * If the video processing was failed first time, marking it as upload status
                             * If the video processing was failed more than one time, marking it as re-upload status
                             */
                            if (scheduler.getExercise().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_PROCESSING_FAILED)) {
                                exerciseScheduleView.setVideoUploadStatus(VideoUploadStatus.UPLOAD);
                            } else if (scheduler.getExercise().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED)) {
                                exerciseScheduleView.setVideoUploadStatus(VideoUploadStatus.REUPLOAD);
                            }
                        }

                        //Support Video mapping construction from Circuit's exercise schedule
                        VideoManagement supportVideoManagement = scheduler.getExercise().getSupportVideoManagement();
                        if (supportVideoManagement != null) {
                            SupportingVideoMappingModel supportingVideoModel = new SupportingVideoMappingModel();
                            supportingVideoModel.setTitle(supportVideoManagement.getTitle());
                            supportingVideoModel.setDescription(supportVideoManagement.getDescription());

                            String supportingVimeoUrl = supportVideoManagement.getUrl();
                            supportingVideoModel.setUrl(supportingVimeoUrl);

                            String supportingVimeoId = "";

                            if (supportingVimeoUrl != null) {
                                try {
                                    if (supportingVimeoUrl.contains("/")) {
                                        String[] videoIds = supportingVimeoUrl.split("/");
                                        supportingVimeoId = videoIds[2];
                                    }
                                } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                                    log.error("The index you have entered is invalid", arrayIndexOutOfBoundsException);
                                } catch (Exception exception) {
                                    log.error("Unexpected error", exception);
                                }
                            }

                            startTimeInMillis = new Date().getTime();
                            if (!supportingVimeoId.isEmpty()) {
                                supportingVideoModel.setParsedUrl(vimeoService.getVimeoUrlFromId(Long.parseLong(supportingVimeoId)));
                                supportingVideoModel.setSupportingVideoStandards(vimeoService.getVimeoVideos(Long.parseLong(supportingVimeoId)));
                            }
                            profilingEndTimeInMillis = new Date().getTime();
                            log.info("Vimeo url and video standards for support video: Time taken in millis : " + (profilingEndTimeInMillis - startTimeInMillis));


                            supportingVideoModel.setDuration(supportVideoManagement.getDuration());
                            if (supportVideoManagement.getThumbnail() != null) {
                                supportingVideoModel.setImageId(supportVideoManagement.getThumbnail().getImageId());
                                supportingVideoModel.setThumbnailUrl(supportVideoManagement.getThumbnail().getImagePath());
                            }
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
                            exerciseScheduleView.setSupportingVideoMappingModel(supportingVideoModel);
                        }

                        if (scheduler.getExercise().getEquipments() != null) {
                            exerciseScheduleView.setEquipments(scheduler.getExercise().getEquipments());
                        }
                        boolean isExerciseBlocked = flaggedExercisesSummaryRepository.existsByExerciseExerciseIdAndFlagStatus(scheduler.getExercise().getExerciseId(), KeyConstants.KEY_BLOCK);
                        exerciseScheduleView.setExerciseBlocked(isExerciseBlocked);
                    } else if (scheduler.getIsAudio() != null && scheduler.getIsAudio()) {
                        boolean isAudio = (scheduler.getIsAudio() != null && scheduler.getIsAudio().booleanValue()) ? true : false;
                        exerciseScheduleView.setAudio(isAudio);
                        AudioResponseView audioResponseView = new AudioResponseView();
                        audioResponseView.setAudioId(scheduler.getVoiceOver().getAudios().getAudioId());
                        audioResponseView.setFilePath(scheduler.getVoiceOver().getAudios().getFilePath());
                        audioResponseView.setDuration(scheduler.getVoiceOver().getAudios().getDuration());
                        audioResponseView.setTitle(scheduler.getVoiceOver().getTitle());
                        audioResponseView.setVoiceOverId(scheduler.getVoiceOver().getVoiceOverId());
                        exerciseScheduleView.setAudioResponseView(audioResponseView);
                        duration += scheduler.getVoiceOver().getAudios().getDuration();
                    } else {
                        exerciseScheduleView.setRestVideo(true);
                        RestResponseView restVideo = new RestResponseView();
                        restVideo.setWorkoutRestVideoId(scheduler.getWorkoutRestVideo().getWorkoutRestVideoId());
                        restVideo.setRestTime(scheduler.getWorkoutRestVideo().getRestTime());

                        VideoManagement videoManagement = scheduler.getWorkoutRestVideo().getVideoManagement();

                        String vimeoUrl = null;
                        String vimeoId = "";

                        if (videoManagement != null) {
                            vimeoUrl = videoManagement.getUrl();
                            if (vimeoUrl != null) {
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
                        restVideo.setUrl(vimeoUrl);

                        long startTimeInMillis  = new Date().getTime();
                        if (!vimeoId.isEmpty()) {
                            restVideo.setParsedUrl(vimeoService.getVimeoUrlFromId(Long.parseLong(vimeoId)));
                        }
                        profilingEndTimeInMillis = new Date().getTime();
                        log.info("Vimeo url  for rest video: Time taken in millis : " + (profilingEndTimeInMillis - startTimeInMillis));

                        restVideo.setThumbnail(scheduler.getWorkoutRestVideo().getVideoManagement().getThumbnail());
                        exerciseScheduleView.setWorkoutRestVideo(restVideo);
                        duration += scheduler.getWorkoutRestVideo().getRestTime();
                    }
                    schedules.add(exerciseScheduleView);
                }
                Collections.sort(schedules);
                circuitView.setExerciseSchedules(schedules);
                circuitView.setDuration(duration);
                if (circuit.getExerciseSchedules() != null && circuit.getExerciseSchedules().size() > 0) {
                    circuitView.setExerciseCount(exerciseScheduleRepository.countByCircuitAndWorkoutRestVideo(circuit, null));
                }
                long startTimeInMillis  = new Date().getTime();
                circuitView.setStatus(getCircuitStatus(circuit));
                profilingEndTimeInMillis = new Date().getTime();
                log.info("Circuit status check: Time taken in millis : " + (profilingEndTimeInMillis - startTimeInMillis));
                log.info("circuit response construction ends");
                log.info("circuit response construction : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));
            }
        }
        return circuitView;
    }

    private void validateCircuitModelWithSetsAndReps(CircuitModelWithSetsAndReps circuitModel) {
        ValidationUtils.throwException(circuitModel == null, ValidationMessageConstants.MSG_CIRCUIT_MODEL_EMPTY, Constants.BAD_REQUEST);
        if(!circuitModel.isAudio()) {
            ValidationUtils.throwException(ValidationUtils.isEmptyString(circuitModel.getTitle()), ValidationMessageConstants.MSG_TITLE_NULL, Constants.BAD_REQUEST);
            ValidationUtils.throwException(circuitModel.getExerciseMappingModelWithSetsAndReps() == null || circuitModel.getExerciseMappingModelWithSetsAndReps().isEmpty(), ValidationMessageConstants.MSG_EXERCISE_EMPTY, Constants.BAD_REQUEST);
        }
    }

    private void validateFlaggedVideosInCircuitWithSetsAndReps(List<ExerciseMappingModelWithSetsAndReps> exerciseSchedules) {
        List<Long> exerciseIdList = new ArrayList<>();
        for (ExerciseMappingModelWithSetsAndReps schedule : exerciseSchedules) {
            if (!schedule.isRestVideo()) {
                exerciseIdList.add(schedule.getExerciseId());
            }
        }
        exerciseIdList = exerciseIdList.stream().distinct().collect(Collectors.toList());

        boolean containsFlaggedVideo = false;
        if (!exerciseIdList.isEmpty()) {
            containsFlaggedVideo = flaggedExercisesSummaryRepository.existsByExerciseExerciseIdInAndFlagStatus(exerciseIdList, KeyConstants.KEY_BLOCK);
        }
        if (containsFlaggedVideo) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CIRCUIT_CANT_ADD_FLAGGED_VIDEO, MessageConstants.ERROR);
        }
    }

    //Regulating rest distribution in a circuit
    private void regulateRestVideoInExerciseScheduleWithSetsAndReps(List<ExerciseMappingModelWithSetsAndReps> schedules) {
        Collections.sort(schedules);
        if (schedules.size() > 0) {
            //First exercise in a circuit can not be rest
            if (schedules.get(0).isRestVideo()) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_FIRST_EXERCISE_NO_REST, MessageConstants.ERROR);
            }
            //A circuit can not have 2 consecutive rest
            for (int i = 0; i < schedules.size() - 1; i++) {
                if (schedules.get(i).isRestVideo() && schedules.get(i + 1).isRestVideo()) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_CONSECUTIVE_EXERCISE_NO_REST, MessageConstants.ERROR);
                }
            }
        }
    }

    /**
     * Get details of a circuit
     *
     * @param circuitId
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    public CircuitResponseViewWithSetsAndReps getCircuitWithSetsAndReps(Long circuitId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        log.info("Get circuit with sets and reps starts.");
        long apiStartTimeMillis = new Date().getTime();

        User user = userComponents.getUser();
        if (circuitId == null || circuitId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_ID_NULL, MessageConstants.ERROR);
        }
        Circuit circuit = circuitRepository.findByCircuitId(circuitId);
        if (circuit == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_NOT_FOUND, null);
        }
        log.info("Basic validations : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        CircuitResponseViewWithSetsAndReps circuitResponseViewWithSetsAndReps = constructCircuitResponseViewWithSetsReps(circuit);
        log.info("Construct circuit response view with sets and reps : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get circuit with sets and reps ends.");

        return circuitResponseViewWithSetsAndReps;
    }

    /**
     * Get sets count
     * @return
     */
    public List<Long> getSetsCount() {
        List<SetsCount> setsCount = setsCountRepository.findAll();
        if(setsCount.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, "", "");
        }
        List<Long> setsCountList = setsCount.stream().map(count -> count.getSetsCount()).collect(Collectors.toList());
        return setsCountList;
    }

    /**
     * Remove an voice over from a circuit.
     * Voice over will not be deleted.
     * Voice over will not be related to the circuit.
     * Circuit and Voice Over Mapping or Exercise Schedule will be deleted.
     *
     * @param exerciseScheduleId
     * @return
     */
    public void removeVoiceOver(Long exerciseScheduleId, Long circuitAndVoiceOverMappingId, Boolean isAudio)
    {

        User user = userComponents.getUser(); //Getting current user

        if(isAudio)
        {
            if(circuitAndVoiceOverMappingId == null || circuitAndVoiceOverMappingId == 0)
            {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_AND_VOICE_OVER_MAPPING_ID_NULL, MessageConstants.ERROR);
            }

            Optional<CircuitAndVoiceOverMapping> circuitAndVoiceOverMappingOptional = circuitAndVoiceOverMappingRepository.findById(circuitAndVoiceOverMappingId);

            if(!circuitAndVoiceOverMappingOptional.isPresent())
            {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_AND_VOICE_OVER_MAPPING_NOT_FOUND, MessageConstants.ERROR);
            }
            CircuitAndVoiceOverMapping circuitAndVoiceOverMapping = circuitAndVoiceOverMappingOptional.get();

            VoiceOver voiceOver = circuitAndVoiceOverMapping.getVoiceOver();
            if(voiceOver == null)
            {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_NOT_FOUND, MessageConstants.ERROR);
            }
            validationService.validateIfVoiceOverBelongsToInstructor(user.getUserId(), voiceOver.getVoiceOverId());

            circuitAndVoiceOverMappingRepository.deleteById(circuitAndVoiceOverMappingId);
        }
        else
        {
            //Validating the user input
            if(exerciseScheduleId == null || exerciseScheduleId == 0)
            {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_SCHEDULE_ID_NULL, MessageConstants.ERROR);
            }

            //Getting Exercise Scheduler and validate it
            ExerciseSchedulers exerciseSchedulers = exerciseScheduleRepository.findByExerciseScheduleId(exerciseScheduleId);
            if(exerciseSchedulers == null)
            {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
            }

            regulateExerciseRestDuringDelete(exerciseSchedulers); //Checking rest order is valid if we delete the current schedule

            if(exerciseSchedulers.getIsAudio() == null)
            {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_NOT_FOUND, MessageConstants.ERROR);
            }

            //Getting Voice Over and validate it with current user
            if(exerciseSchedulers.getIsAudio() != null && exerciseSchedulers.getIsAudio().booleanValue())
            {
                VoiceOver voiceOver = exerciseSchedulers.getVoiceOver();
                if(voiceOver == null)
                {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_VOICE_OVER_NOT_FOUND, MessageConstants.ERROR);
                }
                validationService.validateIfVoiceOverBelongsToInstructor(user.getUserId(), voiceOver.getVoiceOverId());
            }

            exerciseScheduleRepository.deleteByExerciseScheduleId(exerciseScheduleId); //Remove the exercise schedule from repository
        }
    }
}
