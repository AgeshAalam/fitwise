package com.fitwise.service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.DiscardWorkoutReasons;
import com.fitwise.entity.FeedbackTypes;
import com.fitwise.entity.ProgramFeedback;
import com.fitwise.entity.ProgramRating;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.WorkoutDiscardFeedback;
import com.fitwise.entity.WorkoutDiscardFeedbackMapping;
import com.fitwise.entity.WorkoutFeedback;
import com.fitwise.entity.WorkoutSchedule;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.member.DiscardReasonModel;
import com.fitwise.model.member.WorkoutDiscardFeedbackModel;
import com.fitwise.repository.ProgramRatingRepository;
import com.fitwise.repository.WorkoutScheduleRepository;
import com.fitwise.repository.feedback.DiscardWorkoutReasonsRepository;
import com.fitwise.repository.feedback.FeedbackTypesRepository;
import com.fitwise.repository.feedback.ProgramFeedbackRepository;
import com.fitwise.repository.feedback.WorkoutDiscardFeedbackRepository;
import com.fitwise.repository.feedback.WorkoutFeedbackRepository;
import com.fitwise.repository.member.WorkoutCompletionRepository;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.view.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FeedbackService {

    @Autowired
    FeedbackTypesRepository feedbackTypesRepository;
    @Autowired
    DiscardWorkoutReasonsRepository discardWorkoutReasonsRepository;
    @Autowired
    ProgramFeedbackRepository programFeedbackRepository;
    @Autowired
    WorkoutFeedbackRepository workoutFeedbackRepository;
    @Autowired
    private UserComponents userComponents;
    @Autowired
    ValidationService validationService;
    @Autowired
    private ProgramRatingRepository programRatingRepository;
    @Autowired
    WorkoutScheduleRepository workoutScheduleRepository;
    @Autowired
    WorkoutDiscardFeedbackRepository workoutDiscardFeedbackRepository;
    @Autowired
    WorkoutCompletionRepository workoutCompletionRepository;

    /**
     * Method to get the Feedback types
     *
     * @return Response model with all Feed back types
     */
    public ResponseModel getFeedBackTypes() {

        List<FeedbackTypes> feedbackTypes = feedbackTypesRepository.findAll();

        ResponseModel res = new ResponseModel();
        if (feedbackTypes.isEmpty()) {
            res.setStatus(Constants.EMPTY_RESPONSE_STATUS);
            res.setMessage(MessageConstants.ERROR);
        } else {
            res.setStatus(Constants.SUCCESS_STATUS);
            res.setMessage(MessageConstants.MSG_FEEDBACK_TYPES_FETCHED);
        }
        Map<String, Object> resMap = new HashMap<String, Object>();
        resMap.put(KeyConstants.KEY_FEEDBACKS, feedbackTypes);
        res.setPayload(resMap);

        return res;
    }

    /**
     * Method to get Discard reasons list
     */
    public ResponseModel getDiscardReasons() {
        List<DiscardWorkoutReasons> discardReasons = discardWorkoutReasonsRepository.findAll();
        ResponseModel res = new ResponseModel();

        if (discardReasons.isEmpty()) {
            res.setStatus(Constants.EMPTY_RESPONSE_STATUS);
            res.setMessage(MessageConstants.MSG_DISCARD_REASONS_EMPTY);
            res.setPayload(null);
        } else {
            res.setStatus(Constants.SUCCESS_STATUS);
            Map<String, Object> reasonsMap = new HashMap<String, Object>();
            reasonsMap.put(KeyConstants.KEY_DISCARD_REASONS_LIST, discardReasons);
            res.setPayload(reasonsMap);
            res.setError(null);
        }

        return res;
    }

    /**
     * Method used to save work-out feedback
     *
     * @return
     * @throws ApplicationException
     */
    public void saveWorkoutFeedback(Long programId, Long workoutScheduleId, Long feedbackTypeId) throws ApplicationException {
        log.info("saveWorkoutFeedback starts.");
        long apiStartTimeMillis = new Date().getTime();

        User user = userComponents.getUser();

        long profilingStartTimeMillis = new Date().getTime();
        //Validation
        Programs program = validationService.validateProgramId(programId);

        if (feedbackTypeId == null || feedbackTypeId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FEEDBACK_TYPE_ID_NULL, MessageConstants.ERROR);
        }
        FeedbackTypes feedbackType = feedbackTypesRepository.findByfeedbackTypeId(feedbackTypeId);
        if (feedbackType == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FEEDBACK_TYPE_NOT_FOUND, MessageConstants.ERROR);
        }
        if (workoutScheduleId == null || workoutScheduleId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_ID_NULL, MessageConstants.ERROR);
        }
        WorkoutSchedule workoutSchedule = workoutScheduleRepository.findByWorkoutScheduleId(workoutScheduleId);
        if (workoutSchedule == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
        }
        List<Long> workoutScheduleIdList = program.getWorkoutSchedules().stream().map(schedule -> schedule.getWorkoutScheduleId()).collect(Collectors.toList());
        if (!workoutScheduleIdList.contains(workoutScheduleId)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_INCORRECT, MessageConstants.ERROR);
        }
        if (workoutSchedule.isRestDay()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_REST, MessageConstants.ERROR);
        }
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Validation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        WorkoutFeedback workoutFeedback = new WorkoutFeedback();
        workoutFeedback.setProgram(program);
        workoutFeedback.setWorkoutSchedule(workoutSchedule);
        workoutFeedback.setUser(user);
        workoutFeedback.setFeedbackType(feedbackType);
        workoutFeedbackRepository.save(workoutFeedback);
        profilingEndTimeMillis = new Date().getTime();
        log.info("DB update row : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("saveWorkoutFeedback ends.");
    }

    /**
     * @param programId
     * @param feedbackTypeId
     * @return
     */
    public void saveProgramFeedback(Long programId, Long feedbackTypeId) {
        User user = userComponents.getUser();

        Programs program = validationService.validateProgramId(programId);

        if (feedbackTypeId == null || feedbackTypeId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FEEDBACK_TYPE_ID_NULL, MessageConstants.ERROR);
        }
        FeedbackTypes feedbackType = feedbackTypesRepository.findByfeedbackTypeId(feedbackTypeId);
        if (feedbackType == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FEEDBACK_TYPE_NOT_FOUND, MessageConstants.ERROR);
        }

        ProgramFeedback programFeedback = new ProgramFeedback();
        programFeedback.setProgram(program);
        programFeedback.setUser(user);
        programFeedback.setFeedbackType(feedbackType);
        programFeedback.setModifiedDate(new Date());
        programFeedbackRepository.save(programFeedback);

    }

    /**
     * @param programId
     * @param rating
     * @return
     */
    @Transactional
    public String saveProgramRating(Long programId, Float rating) {
        log.info("Save program ratings starts.");
        long apiStartTimeMillis = new Date().getTime();
        User user = userComponents.getUser();

        Programs program = validationService.validateProgramId(programId);
        if (rating == null || rating <= 0 || rating > 5) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_RATING_INPUT_INCORRECT, null);
        }
        log.info("Get program and user : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        String responseMsg = MessageConstants.MSG_PROGRAM_RATING_SAVED;

        int completedWorkouts = workoutCompletionRepository.countByMemberUserIdAndProgramProgramId(user.getUserId(), programId);
        boolean isProgramCompleted = false;
        if (completedWorkouts == program.getDuration().getDuration().intValue()) {
            isProgramCompleted = true;
        }
        if (!isProgramCompleted) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_RATING_CANT_SUBMIT, null);
        }
        log.info("Query to completed workout count : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        ProgramRating programRating = programRatingRepository.findByProgramAndUser(program, user);
        if (programRating != null) {
            if (!programRating.getIsSubmissionAllowed().booleanValue()) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROGRAM_RATING_CANT_RESUBMIT, null);
            }
            responseMsg = MessageConstants.MSG_PROGRAM_RATING_UPDATED;
        } else {
            programRating = new ProgramRating();
            programRating.setProgram(program);
            programRating.setUser(user);
        }
        log.info("Query to get program rating : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        programRating.setProgramRating(rating);
        programRating.setModifiedDate(new Date());
        programRating.setIsSubmissionAllowed(false);
        programRatingRepository.save(programRating);
        log.info("Query to save program rating : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Save program rating ends.");
        return responseMsg;
    }

    /**
     * @param discardFeedbackModel
     */
    public void saveWorkoutDiscardReason(WorkoutDiscardFeedbackModel discardFeedbackModel) {
        log.info("saveWorkoutDiscardReason starts.");
        long apiStartTimeMillis = new Date().getTime();

        User user = userComponents.getUser();

        long profilingStartTimeMillis = new Date().getTime();
        //Validation
        Programs program = validationService.validateProgramId(discardFeedbackModel.getProgramId());
        if (discardFeedbackModel.getWorkoutScheduleId() == null || discardFeedbackModel.getWorkoutScheduleId() == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_ID_NULL, MessageConstants.ERROR);
        }
        WorkoutSchedule workoutSchedule = workoutScheduleRepository.findByWorkoutScheduleId(discardFeedbackModel.getWorkoutScheduleId());
        if (workoutSchedule == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_NOT_FOUND, MessageConstants.ERROR);
        }
        List<Long> workoutScheduleIdList = program.getWorkoutSchedules().stream().map(schedule -> schedule.getWorkoutScheduleId()).collect(Collectors.toList());
        if (!workoutScheduleIdList.contains(discardFeedbackModel.getWorkoutScheduleId())) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_INCORRECT, MessageConstants.ERROR);
        }
        if (workoutSchedule.isRestDay()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_SCHEDULE_REST, MessageConstants.ERROR);
        }

        WorkoutDiscardFeedback workoutDiscardFeedback = new WorkoutDiscardFeedback();

        List<WorkoutDiscardFeedbackMapping> discardFeedbackMappingList = new ArrayList<>();
        for (DiscardReasonModel discardReasonModel : discardFeedbackModel.getDiscardReasons()) {
            if (discardReasonModel.getDiscardFeedbackId() == null || discardReasonModel.getDiscardFeedbackId() == 0) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_DISCARD_FEEDBACK_ID_NULL, MessageConstants.ERROR);
            }
            DiscardWorkoutReasons discardReason = discardWorkoutReasonsRepository.findByDiscardFeedbackId(discardReasonModel.getDiscardFeedbackId());
            if (discardReason == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_DISCARD_FEEDBACK_NOT_FOUND, MessageConstants.ERROR);
            }

            boolean isOtherReason = false;
            if (DBConstants.OTHERS.equals(discardReason.getDiscardReason())) {
                isOtherReason = true;
            }
            if (isOtherReason && (discardReasonModel.getCustomReason() == null || discardReasonModel.getCustomReason().isEmpty())) {
                throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_DISCARD_CUSTOM_FEEDBACK_NOT_FOUND, MessageConstants.ERROR);
            }
            WorkoutDiscardFeedbackMapping discardFeedbackMapping = new WorkoutDiscardFeedbackMapping();
            discardFeedbackMapping.setDiscardWorkoutReason(discardReason);
            discardFeedbackMapping.setWorkoutDiscardFeedback(workoutDiscardFeedback);
            if (isOtherReason) {
                discardFeedbackMapping.setCustomReason(discardReasonModel.getCustomReason());
            }

            discardFeedbackMappingList.add(discardFeedbackMapping);
        }
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Query and Validation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = new Date().getTime();
        workoutDiscardFeedback.setProgram(program);
        workoutDiscardFeedback.setWorkoutSchedule(workoutSchedule);
        workoutDiscardFeedback.setUser(user);
        workoutDiscardFeedback.setWorkoutDiscardFeedbackMapping(discardFeedbackMappingList);
        workoutDiscardFeedbackRepository.save(workoutDiscardFeedback);
        profilingEndTimeMillis = new Date().getTime();
        log.info("DB update : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("saveWorkoutDiscardReason ends.");
    }

}
