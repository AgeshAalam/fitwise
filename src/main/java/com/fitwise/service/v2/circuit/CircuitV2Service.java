package com.fitwise.service.v2.circuit;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.VideoUploadStatus;
import com.fitwise.entity.Circuit;
import com.fitwise.entity.CircuitAndVoiceOverMapping;
import com.fitwise.entity.ExerciseSchedulers;
import com.fitwise.entity.User;
import com.fitwise.entity.VideoManagement;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.ExerciseScheduleRepository;
import com.fitwise.repository.FlaggedExercisesSummaryRepository;
import com.fitwise.repository.circuit.CircuitRepository;
import com.fitwise.service.circuit.CircuitService;
import com.fitwise.view.AudioResponseView;
import com.fitwise.view.ExerciseScheduleViewWithSetsAndReps;
import com.fitwise.view.RestResponseView;
import com.fitwise.view.circuit.CircuitResponseViewWithSetsAndReps;
import com.fitwise.workout.model.SupportingVideoMappingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Processing the circuit logic version 2.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CircuitV2Service {

    final private UserComponents userComponents;
    final private CircuitService circuitService;
    final private FlaggedExercisesSummaryRepository flaggedExercisesSummaryRepository;
    final private ExerciseScheduleRepository exerciseScheduleRepository;
    final private CircuitRepository circuitRepository;

    /**
     * Creating response for circuit details
     *
     * @param circuit
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    private CircuitResponseViewWithSetsAndReps constructCircuitResponse(Circuit circuit) {
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
            if (circuit.getExerciseSchedules() != null && circuit.getExerciseSchedules().size() > 0) {
                List<ExerciseScheduleViewWithSetsAndReps> schedules = new ArrayList<>();
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
                        if (videoManagement != null) {
                            vimeoUrl = videoManagement.getUrl();
                        }
                        exerciseScheduleView.setUrl(vimeoUrl);
                        if (scheduler.getExercise().getVideoManagement() != null) {
                            int exerciseDuration = (int) (scheduler.getExercise().getVideoManagement().getDuration());
                            if (scheduler.getLoopCount() != null && scheduler.getLoopCount() > 0) {
                                exerciseScheduleView.setLoopCount(scheduler.getLoopCount());
                                exerciseDuration = (int) (exerciseDuration * scheduler.getLoopCount());
                            }
							// Calculated Workout time for Set & Rep Based Workouts
							if (scheduler.getSetsCount() > 0 && scheduler.getRepsCount() > 0) {
								int setsDuration = scheduler.getSetsCount() * Constants.SET_REST_DURATION_IN_SEC; // # of Sets * 60 sec
								int repsDuration = scheduler.getSetsCount() * scheduler.getRepsCount()
										* Constants.REPS_REST_DURATION_IN_SEC; // # of Sets * # of Reps * 2 sec
								exerciseDuration = setsDuration + repsDuration;
							}
                            exerciseScheduleView.setDuration(exerciseDuration);
                            duration += exerciseDuration;
                            if (scheduler.getExercise().getVideoManagement().getThumbnail() != null) {
                                exerciseScheduleView.setImageId(scheduler.getExercise().getVideoManagement().getThumbnail().getImageId());
                                exerciseScheduleView.setThumbnailUrl(scheduler.getExercise().getVideoManagement().getThumbnail().getImagePath());
                            }
                            exerciseScheduleView.setVideoUploadStatus(scheduler.getExercise().getVideoManagement().getUploadStatus());
                            if (scheduler.getExercise().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_PROCESSING_FAILED)) {
                                exerciseScheduleView.setVideoUploadStatus(VideoUploadStatus.UPLOAD);
                            } else if (scheduler.getExercise().getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED)) {
                                exerciseScheduleView.setVideoUploadStatus(VideoUploadStatus.REUPLOAD);
                            }
                        }
                        VideoManagement supportVideoManagement = scheduler.getExercise().getSupportVideoManagement();
                        if (supportVideoManagement != null) {
                            SupportingVideoMappingModel supportingVideoModel = new SupportingVideoMappingModel();
                            supportingVideoModel.setTitle(supportVideoManagement.getTitle());
                            supportingVideoModel.setDescription(supportVideoManagement.getDescription());
                            String supportingVimeoUrl = supportVideoManagement.getUrl();
                            supportingVideoModel.setUrl(supportingVimeoUrl);
                            supportingVideoModel.setDuration(supportVideoManagement.getDuration());
                            if (supportVideoManagement.getThumbnail() != null) {
                                supportingVideoModel.setImageId(supportVideoManagement.getThumbnail().getImageId());
                                supportingVideoModel.setThumbnailUrl(supportVideoManagement.getThumbnail().getImagePath());
                            }
                            supportingVideoModel.setVideoUploadStatus(supportVideoManagement.getUploadStatus());
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
                circuitView.setStatus(circuitService.getCircuitStatus(circuit));
                profilingEndTimeInMillis = new Date().getTime();
                log.info("Circuit status check: Time taken in millis : " + (profilingEndTimeInMillis - startTimeInMillis));
                log.info("circuit response construction ends");
                log.info("circuit response construction : Time taken in millis : " + (profilingEndTimeInMillis - profilingStartTimeInMillis));
            }
        }
        return circuitView;
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
    public CircuitResponseViewWithSetsAndReps getCircuit(Long circuitId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        long startTime = new Date().getTime();
        log.info("Get circuit start : " + startTime);
        userComponents.getUser();
        if (circuitId == null || circuitId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_ID_NULL, MessageConstants.ERROR);
        }
        Circuit circuit = circuitRepository.findByCircuitId(circuitId);
        if (circuit == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_CIRCUIT_NOT_FOUND, null);
        }
        CircuitResponseViewWithSetsAndReps responseViewWithSetsAndReps = constructCircuitResponse(circuit);
        log.info("Get circuit end : " + (new Date().getTime() - startTime));
        return responseViewWithSetsAndReps;
    }

}
