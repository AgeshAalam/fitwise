package com.fitwise.rest.member;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.member.WorkoutDiscardFeedbackModel;
import com.fitwise.service.FeedbackService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@RequestMapping("/v1/member/feedback")
public class FeedbackController {

    @Autowired
    FeedbackService feedbackService;


    @GetMapping("/getFeedBackTypes")
    public ResponseModel getFeedBackTypes() {
        return feedbackService.getFeedBackTypes();
    }

    /**
     * @param programId
     * @param workoutScheduleId
     * @return
     * @throws ApplicationException
     */
    @PostMapping("/saveWorkoutFeedback")
    public ResponseModel saveWorkoutFeedBack(@RequestParam Long programId, @RequestParam Long workoutScheduleId, @RequestParam Long feedbackTypeId) throws ApplicationException {
        feedbackService.saveWorkoutFeedback(programId, workoutScheduleId, feedbackTypeId);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_WORKOUT_FEEDBACK_SAVED);
        response.setPayload(null);
        return response;
    }

    /**
     * @param programId
     * @param feedbackTypeId
     * @return
     */
    @PostMapping(value = "/saveProgramFeedback")
    public ResponseModel saveProgramFeedback(@RequestParam Long programId, @RequestParam Long feedbackTypeId) {
        feedbackService.saveProgramFeedback(programId, feedbackTypeId);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_PROGRAM_FEEDBACK_SAVED);
        response.setPayload(null);
        return response;
    }

    /**
     * @param programId
     * @param rating
     * @return
     */
    @PutMapping(value = "/saveProgramRating")
    public ResponseModel saveProgramRating(@RequestParam Long programId, @RequestParam Float rating) {
        String responseMsg = feedbackService.saveProgramRating(programId, rating);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(responseMsg);
        response.setPayload(null);
        return response;
    }

    /**
     * @return
     */
    @GetMapping("/getDiscardReasons")
    public ResponseModel getDiscardFeedbackOptions() {
        return feedbackService.getDiscardReasons();
    }

    /**
     * @param discardFeedbackModel
     * @return
     * @throws ApplicationException
     */
    @PostMapping("/saveWorkoutDiscardReason")
    public ResponseModel saveWorkoutDiscardReason(@RequestBody WorkoutDiscardFeedbackModel discardFeedbackModel) throws ApplicationException {
        feedbackService.saveWorkoutDiscardReason(discardFeedbackModel);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_WORKOUT_DISCARD_SAVED);
        response.setPayload(null);
        return response;
    }

}
