package com.fitwise.rest.member;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.FlaggedVideoReason;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.member.AllProgramsFilterModel;
import com.fitwise.model.member.MemberProgramsFilterModel;
import com.fitwise.model.member.RecommendedAndTrendingFilterModel;
import com.fitwise.model.videoCaching.VideoCachingRequestModel;
import com.fitwise.service.member.MemberProgramService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.member.ExerciseCompletionResponse;
import com.fitwise.view.member.MemberFilterView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The Class MemberProgramController.
 */
@RestController
@RequestMapping(value = "/v1/member/program")
@RequiredArgsConstructor
public class MemberProgramController {

    private final MemberProgramService memberProgramService;

    /**
     * Filter options for recommended programs
     * @return Response model
     */
    @GetMapping(value = "/recommended/filter")
    public ResponseModel getRecommendedFilter() {
        Map<String, List<MemberFilterView>> filterRespMap = new HashMap<>();
        filterRespMap.put(KeyConstants.KEY_FILTER_DATA, memberProgramService.getRecommendedAndTrendingFilter());
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(filterRespMap);
        return responseModel;
    }

    /**
     * Gets the recommended programs.
     * @param pageSize the page size
     * @param pageNo   the page count
     * @return the recommended programs
     */
    @PutMapping(value = "/recommended")
    public ResponseModel getRecommendedPrograms(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestBody RecommendedAndTrendingFilterModel filterModel, @RequestParam Optional<String> search) {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(memberProgramService.getRecommendedPrograms(pageNo, pageSize, filterModel, search));
        return responseModel;
    }

    /**
     * Filter options for trending programs
     * @return Recommended and trending filter
     */
    @GetMapping(value = "/trending/filter")
    public ResponseModel getTrendingFilter() {
        Map<String, List<MemberFilterView>> filterRespMap = new HashMap<>();
        filterRespMap.put(KeyConstants.KEY_FILTER_DATA, memberProgramService.getRecommendedAndTrendingFilter());
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(filterRespMap);
        return responseModel;
    }

    /**
     * Gets the trending programs.
     * @param pageSize the page size
     * @param pageNo   the page count
     * @return the trending programs
     */
    @PutMapping(value = "/trending")
    public ResponseModel getTrendingPrograms(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestBody RecommendedAndTrendingFilterModel filterModel, @RequestParam Optional<String> search) {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(memberProgramService.getUserTrendingPrograms(pageNo, pageSize, filterModel, search));
        return responseModel;
    }

    /**
     * Get filter for programs
     * @return Programs applied with filter
     */
    @GetMapping(value = "/programsbyTypeFilter")
    public ResponseModel getProgramsbyTypeFilter() {
        Map<String, List<MemberFilterView>> filterRespMap = new HashMap<>();
        filterRespMap.put(KeyConstants.KEY_FILTER_DATA, memberProgramService.getProgramsbyTypeFilter());
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(filterRespMap);
        return responseModel;
    }

    /**
     * To get programns By type
     * @param memberProgramsFilterModel
     * @return Programs by given type
     */
    @PutMapping(value = "/getProgramsByType")
    public ResponseModel getProgramsByType(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam Long programTypeId, @RequestBody MemberProgramsFilterModel memberProgramsFilterModel, @RequestParam Optional<String> search) {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(memberProgramService.getProgramsByType(pageNo, pageSize, programTypeId, memberProgramsFilterModel, search));
        return responseModel;
    }

    /**
     * Method to get the program details based on userId and ProgramId
     * Getting userId inorder to fetch the user subscription data of the user.
     * @param programId
     * @return Program details
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws ParseException
     */
    @GetMapping(value = "/getProgramDetails")
    public ResponseModel getProgramDetails(@RequestParam Long programId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(memberProgramService.getProgramDetails(programId));
        return responseModel;
    }

    /**
     * Method to get the workout details based on userId and workoutId
     *
     * @param workoutId
     * @return
     * @throws ApplicationException
     */
    @GetMapping(value = "/getWorkoutDetails")
    public ResponseModel getWorkoutDetails(@RequestParam Long workoutId, @RequestParam Long workoutScheduleId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_WORKOUT_DETAIL_FETCHED);
        responseModel.setPayload(memberProgramService.getWorkoutDetails(workoutId, workoutScheduleId));
        return responseModel;
    }


    /**
     * Method to get the workout details based on userId and workoutId
     *
     * @param workoutId
     * @return
     */
    @GetMapping(value = "/getWorkoutDetailsForDemo")
    public ResponseModel getWorkoutDetailsForDemo(@RequestParam Long workoutId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_WORKOUT_DETAIL_FETCHED);
        responseModel.setPayload(memberProgramService.getWorkoutDetails(workoutId));
        return responseModel;
    }

    /**
     * Mark exercise completion
     * @param programId Program
     * @param workoutScheduleId Workout
     * @param circuitScheduleId Circuit
     * @param exerciseScheduleId Exercise to be marked
     * @return success response
     */
    @PostMapping(value = "/postExerciseCompletedStatus")
    public ResponseModel postExerciseCompletedStatus(@RequestParam Long programId, @RequestParam Long workoutScheduleId, @RequestParam Long circuitScheduleId, @RequestParam Long exerciseScheduleId) {
        return memberProgramService.postExerciseCompletedStatus(programId, workoutScheduleId, circuitScheduleId, exerciseScheduleId, null, false);
    }

    /**
     * Mark voiceover completion
     * @param programId Program
     * @param workoutScheduleId Workout
     * @param circuitScheduleId Circuit
     * @param circuitAndVoiceOverMappingId Completed voiceover
     * @return success response
     */
    @PostMapping(value = "/postVoiceOverInAudioCircuitCompletedStatus")
    public ResponseModel postVoiceOverInAudioCircuitCompletedStatus(@RequestParam Long programId, @RequestParam Long workoutScheduleId, @RequestParam Long circuitScheduleId, @RequestParam Long circuitAndVoiceOverMappingId) {
        return memberProgramService.postExerciseCompletedStatus(programId, workoutScheduleId, circuitScheduleId, null, circuitAndVoiceOverMappingId, true);
    }

    /**
     * API to post rest circuit completion
     * @param programId program involves for the completion
     * @param workoutScheduleId completed schedule
     * @param circuitScheduleId completed circuit
     * @return Success response
     */
    @PostMapping(value = "/postRestCircuitCompletedStatus")
    public ResponseModel postRestCircuitCompletedStatus(@RequestParam Long programId, @RequestParam Long workoutScheduleId, @RequestParam Long circuitScheduleId) {
        return memberProgramService.postRestCircuitCompletedStatus(programId, workoutScheduleId, circuitScheduleId);
    }

    /**
     * API to post rest activity workout completion
     * @param programId program which involves for the completion
     * @param workoutScheduleId completed schedule
     * @return Success response
     */
    @PostMapping(value = "/postRestActivityCompletedStatus")
    public ResponseModel postRestActivityCompletedStatus(@RequestParam Long programId, @RequestParam Long workoutScheduleId, @RequestParam Long completionDate) {
        ExerciseCompletionResponse exerciseCompletionResponse = memberProgramService.postRestActivityCompletedStatus(programId, workoutScheduleId, completionDate);
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_REST_ACTIVITY_WORKOUT_COMPLETED_STATUS_UPDATED);
        responseModel.setPayload(exerciseCompletionResponse);
        return responseModel;
    }

    /**
     * Reset the current progress of the program completion
     * @param programId Program to reset
     * @return Success response
     */
    @PutMapping(value = "/resetProgramCompletion")
    public ResponseModel resetProgramCompletion(@RequestParam Long programId) {
        memberProgramService.resetProgramCompletion(programId);
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_PROGRAM_COMPLETION_RESET);
        responseModel.setPayload(null);
        return responseModel;
    }

    /**
     * Get member programs
     * @param subscriptionStatus state of the program
     * @param pageNo Page numbers to find
     * @param pageSize Records per page
     * @param searchName Name to search programs
     * @return Program list
     */
    @GetMapping(value = "/myPrograms")
    public ResponseModel getMyPrograms(@RequestParam String subscriptionStatus, @RequestParam int pageNo, @RequestParam int pageSize, @RequestParam Optional<String> searchName) {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(memberProgramService.getMyPrograms(subscriptionStatus, pageNo, pageSize, searchName));
        return response;
    }

    /**
     * Get today program activities
     * @return Today programs
     * @throws ParseException
     */
    @GetMapping(value = "/todaysPrograms")
    public ResponseModel getTodaysPrograms() throws ParseException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(memberProgramService.getTodaysPrograms());
        return response;
    }

    /**
     * Filter for Overall list of programs.
     * @return Filter for program
     */
    @GetMapping(value = "/all/filter")
    public ResponseModel getAllProgramsFilter() {
        Map<String, List<MemberFilterView>> filterRespMap = new HashMap<>();
        filterRespMap.put(KeyConstants.KEY_FILTER_DATA, memberProgramService.getAllProgramsFilter());
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(filterRespMap);
        return responseModel;
    }

    /**
     * Gets the overall list of programs.
     * @param pageNo
     * @param pageSize
     * @param filterModel
     * @param search
     * @return All programs
     */
    @PutMapping(value = "/all")
    public ResponseModel getAllPrograms(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestBody AllProgramsFilterModel filterModel, @RequestParam Optional<String> search) {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(memberProgramService.getAllPrograms(pageNo, pageSize, filterModel, search));
        return responseModel;
    }

    /**
     * API to get list of exercise flagging reasons
     * @return Flag reasons
     */
    @GetMapping(value = "/exerciseFlaggingReasons")
    public ResponseModel getExerciseFlaggingReasons() {
        Map<String, List<FlaggedVideoReason>> respMap = new HashMap<>();
        respMap.put(KeyConstants.KEY_REASONS, memberProgramService.getExerciseFlaggingReasons());
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(respMap);
        return responseModel;
    }

    /**
     * API to flag a video
     * @param exerciseId
     * @param reasonId
     * @return Success response
     */
    @PostMapping(value = "/flagExercise")
    public ResponseModel flagExercise(@RequestParam Long exerciseId, @RequestParam Long reasonId) {
        return memberProgramService.flagExercise(exerciseId, reasonId);
    }

    /**
     * API to video cache sync
     * @param programs
     * @return Response model
     */
    @PostMapping(value = "/videoCacheSync")
    public ResponseModel videoCacheSync(@RequestBody VideoCachingRequestModel programs) {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(memberProgramService.videoCacheSync(programs));
        responseModel.setPayload(null);
        return responseModel;
    }
}