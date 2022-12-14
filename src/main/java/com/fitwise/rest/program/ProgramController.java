package com.fitwise.rest.program;

import com.fitwise.constants.Constants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.program.model.ProgramModel;
import com.fitwise.program.model.PromoUploadModel;
import com.fitwise.program.service.PriceService;
import com.fitwise.program.service.ProgramService;
import com.fitwise.view.ResponseModel;
import com.fitwise.workout.service.WorkoutService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Map;
import java.util.Optional;

/**
 * The Class ProgramController.
 */
@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/program")
public class ProgramController {

    /**
     * The program service.
     */
    @Autowired
    private ProgramService programService;

    @Autowired
    private PriceService priceService;

    @Autowired
    private WorkoutService workoutService;

    /**
     * Creates the program.
     *
     * @param model the model
     * @return the response model
     * @throws ParseException 
     * @throws ApplicationException 
     */
    @PutMapping("/create")
    public ResponseModel createProgram(@RequestBody ProgramModel model) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ApplicationException, ParseException {
        return programService.createProgram(model);
    }

    /**
     * API to edit Unpublish or block programs
     *
     * @param model
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws ParseException 
     * @throws ApplicationException 
     */
    @PutMapping("/restricted/edit")
    public ResponseModel restrictedProgramEdit(@RequestBody ProgramModel model) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ApplicationException, ParseException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage("Successfully saved program");
        response.setPayload(programService.restrictedProgramEdit(model));
        return response;
    }

    /**
     * Validate the program name for duplicate
     *
     * @param programName
     * @return
     */
    @GetMapping(value = "/validate/name")
    public ResponseModel validateProgramName(@RequestParam String programName) {
        programService.validateName(programName);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SCS_PGM_NAME_VALID, null);
    }

    /**
     * Publish program.
     *
     * @param programId the program id
     * @return the response model
     */
    @GetMapping("/publish/{programId}")
    public ResponseModel publishProgram(@PathVariable("programId") long programId) throws StripeException {
        return programService.publishProgram(programId);
    }

    /**
     * Reset all workout schedule from the program
     *
     * @param programId
     * @return
     */
    @DeleteMapping("/resetWorkoutFromSchedule")
    public ResponseModel resetWorkoutFromSchedule(@RequestParam Long programId) {
        return programService.resetWorkoutFromSchedule(programId);
    }

    /**
     * Reset all workout schedule from unpublish/block program
     *
     * @param programId
     * @return
     */
    @DeleteMapping("/restricted/resetWorkoutFromSchedule")
    public ResponseModel resetWorkoutScheduleForRestrictedProgram(@RequestParam Long programId) {
        return programService.resetWorkoutScheduleForRestrictedProgram(programId);
    }

    /**
     * Gets the program.
     *
     * @param programId the program id
     * @return the program
     * @throws ApplicationException the application exception
     * @throws ParseException 
     */
    @GetMapping
    public ResponseModel getProgram(@RequestParam final Long programId) throws ApplicationException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, programService.getProgram(programId));
    }

    @GetMapping(value = "/sample")
    public ResponseModel getSampleProgram(@RequestParam Long programId) throws ApplicationException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, programService.getSampleProgram(programId));
    }

    /**
     * Gets the programs.
     *
     * @param pageNo   the page no
     * @param pageSize the page size
     * @return the programs
     * @throws ApplicationException the application exception
     */
    @GetMapping(value = "/all")
    public ResponseModel getPrograms(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam Optional<String> searchname, @RequestParam Optional<String> sortOrder, @RequestParam Optional<String> sortBy) throws ApplicationException {
        Map<String, Object> response = programService.getAllPrograms("", pageNo, pageSize, searchname, sortOrder, sortBy);
        response.put(KeyConstants.KEY_PROGRAM_COUNT, response.get(KeyConstants.KEY_TOTAL_COUNT));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
    }

    /**
     * Gets the published programs.
     *
     * @param pageNo   the page no
     * @param pageSize the page size
     * @return the published programs
     * @throws ApplicationException the application exception
     */
    @GetMapping(value = "/all/published")
    public ResponseModel getPublishedPrograms(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam Optional<String> searchname, @RequestParam Optional<String> sortOrder, @RequestParam Optional<String> sortBy) throws ApplicationException {
        Map<String, Object> response = programService.getAllPrograms(InstructorConstant.PUBLISH, pageNo, pageSize, searchname, sortOrder, sortBy);
        response.put(KeyConstants.KEY_PUBLISHED_PROGRAM_COUNT, response.get(KeyConstants.KEY_TOTAL_COUNT));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
    }

    /**
     * Gets the un published programs.
     *
     * @param pageNo   the page no
     * @param pageSize the page size
     * @return the un published programs
     * @throws ApplicationException the application exception
     */
    @GetMapping(value = "/all/unpublished")
    public ResponseModel getUnPublishedPrograms(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam Optional<String> searchname, @RequestParam Optional<String> sortOrder, @RequestParam Optional<String> sortBy) throws ApplicationException {
        Map<String, Object> response = programService.getAllPrograms(InstructorConstant.UNPUBLISH, pageNo, pageSize, searchname, sortOrder, sortBy);
        response.put(KeyConstants.KEY_UNPUBLISHED_PROGRAM_COUNT, response.get(KeyConstants.KEY_TOTAL_COUNT));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
    }

    /**
     * Gets the progress programs.
     *
     * @param pageNo   the page no
     * @param pageSize the page size
     * @return the progress programs
     * @throws ApplicationException the application exception
     */
    @GetMapping(value = "/all/inprogress")
    public ResponseModel getProgressPrograms(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam Optional<String> searchname, @RequestParam Optional<String> sortOrder, @RequestParam Optional<String> sortBy) throws ApplicationException {
        Map<String, Object> response = programService.getAllPrograms(InstructorConstant.INPROGRESS, pageNo, pageSize, searchname, sortOrder, sortBy);
        response.put(KeyConstants.KEY_IN_PROGRESS_PROGRAM_COUNT, response.get(KeyConstants.KEY_TOTAL_COUNT));

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
    }

    /**
     * Gets the blocked programs.
     *
     * @param pageNo   the page no
     * @param pageSize the page size
     * @return the blocked programs
     * @throws ApplicationException the application exception
     */
    @GetMapping(value = "/all/blocked")
    public ResponseModel getBlockedPrograms(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam Optional<String> searchname, @RequestParam Optional<String> sortOrder, @RequestParam Optional<String> sortBy) throws ApplicationException {
        Map<String, Object> response = programService.getAllPrograms(InstructorConstant.BLOCK, pageNo, pageSize, searchname, sortOrder, sortBy);
        response.put(KeyConstants.KEY_BLOCKED_PROGRAM_COUNT, response.get(KeyConstants.KEY_TOTAL_COUNT));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
    }

    /**
     * Gets the programs.
     *
     * @param promoUploadModel the promotion data to upload
     * @return the programs
     * @throws IOException          Signals that an I/O exception has occurred.
     */
    @PostMapping(value = "/uploadPromo")
    public ResponseModel getPrograms(@RequestBody final PromoUploadModel promoUploadModel) throws IOException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, programService.uploadPromotion(promoUploadModel, KeyConstants.KEY_PROGRAM));
    }

    /**
     * Delete program.
     *
     * @param programId the program id
     * @return the response model
     */
    @DeleteMapping
    public ResponseModel deleteProgram(@RequestParam final Long programId) {
        programService.deleteProgram(programId);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROGRAM_DELETED, null);
    }

    /**
     * Fetch promotion details
     *
     * @param promotionId
     * @return
     * @throws ApplicationException
     */
    @GetMapping(value = "/getPromotion")
    public ResponseModel getPromotion(@RequestParam final Long promotionId) throws ApplicationException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return programService.getPromotions(promotionId);
    }

    /**
     * Delete promotion
     *
     * @param promotionId
     * @return
     * @throws ApplicationException
     */
    @DeleteMapping(value = "/deletePromotion")
    public ResponseModel deletePromotion(@RequestParam final Long promotionId, @RequestParam final Long programId) throws ApplicationException {
        return programService.deletePromotion(promotionId, programId);
    }

    @DeleteMapping(value = "/deleteThumbnail")
    public ResponseModel deleteThumbnail(@RequestParam final Long imageId, @RequestParam final Long programId) throws ApplicationException {
        return programService.deleteThumbnail(imageId, programId);
    }

    /**
     * Get the sample program and tour video
     *
     * @return
     */
    @GetMapping(value = "/sampleAndTourDetails")
    public ResponseModel getSampleAndTourDetails() {
        return programService.getSampleAndTourDetails();
    }

    /**
     * Gets the tax details by platform.
     *
     * @return the tax details by platform
     */
    @GetMapping(value = "/getTaxDetailsByPlatform")
    public ResponseModel getTaxDetailsByPlatform() {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, priceService.getTaxDetailsByPlatform());
    }

    /**
     * Gets the Program revenue by platform.
     *
     * @return the Program revenue by platform
     */
    @GetMapping(value = "/getProgramRevenueByPlatform")
    public ResponseModel getProgramRevenueByPlatform(@RequestParam Double price) {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, priceService.getProgramRevenueByPlatform(price));
    }

    /**
     * Delete the workout from goven program
     *
     * @param programId
     * @param workoutId
     * @return
     */
    @DeleteMapping(value = "/removeWorkout")
    public ResponseModel removeWorkout(@RequestParam Long programId, @RequestParam Long workoutId) {
        return workoutService.removeWorkout(programId, workoutId);
    }

    /**
     * Remove a workout from a unpublish_edit or block_edit program
     *
     * @param programId
     * @param workoutId
     * @return
     */
    @DeleteMapping(value = "/restricted/removeWorkout")
    public ResponseModel removeWorkoutForRestrictedProgram(@RequestParam Long programId, @RequestParam Long workoutId) {
        return workoutService.removeWorkoutForRestrictedProgram(programId, workoutId);
    }

    @DeleteMapping("/removeWorkoutFromSchedule")
    public ResponseModel removeWorkoutFromSchedule(@RequestParam Long workoutScheduleId) {
        return workoutService.removeWorkoutFromSchedule(workoutScheduleId);
    }

    @PostMapping(value = "/programPromoCompletionStatus")
    public ResponseModel programPromoCompletionStatus(@RequestParam long programId) {
        return programService.programPromoCompletionStatus(programId);
    }


    /**
     * Member's program visit/usage audit. Need to be called in L2 page of member app
     *
     * @param programId
     * @return
     */
    @PostMapping(value = "/postUserProgramAudit")
    public ResponseModel postUserProgramAudit(@RequestParam long programId) {
        return programService.postUserProgramAudit(programId);
    }

    @GetMapping(value = "/getProgramPrices")
    public ResponseModel getProgramPrices() {
        return programService.getProgramPrices();
    }

    /**
     * Get sample programs list
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping(value = "/getSampleProgramsList")
    public ResponseModel getSamplePrograms(@RequestParam int pageNo, @RequestParam int pageSize) {
        return programService.getSamplePrograms(pageNo, pageSize);
    }

    /**
     * Get sTour videos List
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping(value = "/getTourVideosList")
    public ResponseModel getTourVideosList(@RequestParam int pageNo, @RequestParam int pageSize) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return programService.getQuickTourVideos(pageNo, pageSize);
    }

    /**
     * API to return RestActivityTypes for program workout schedule
     *
     * @return
     */
    @GetMapping(value = "/restActivityTypes")
    public ResponseModel restActivityTypes() {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(programService.restActivityTypes());
        return response;
    }

    @GetMapping(value = "/getAdditionalResources")
    public ResponseModel getAdditionalResources(@RequestParam int pageNo, @RequestParam int pageSize)  {
        return programService.getAdditionalResources(pageNo, pageSize);
    }

    @GetMapping("/getQuickTourVideo/{tourVideoId}")
    public ResponseModel getTourVideo(@PathVariable("tourVideoId") long tourVideoId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return programService.getTourVideo(tourVideoId);
    }

    /**
     * API to get Instructor program count by type
     * @return
     */
    @GetMapping(value = "/programCountByType")
    public ResponseModel getProgramCountByType(){
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(programService.getProgramCountByType());
        return response;
    }

    /**
     * API for program library
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param programTypeId
     * @param searchName
     * @return
     */
    @GetMapping(value = "/allInstructorPrograms")
    public ResponseModel getAllInstructorPrograms(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<Long> programTypeId, @RequestParam Optional<String> searchName){
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(programService.getAllInstructorPrograms(pageNo, pageSize, sortOrder, sortBy,programTypeId, searchName));
        return response;
    }

}
