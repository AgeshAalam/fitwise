package com.fitwise.rest.admin;

import com.fitwise.admin.model.TaxPercentageModel;
import com.fitwise.admin.service.AdminService;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.AdminExerciseModel;
import com.fitwise.service.admin.AdminDashBoardService;
import com.fitwise.service.admin.AdminExerciseService;
import com.fitwise.service.admin.FitwiseShareService;
import com.fitwise.service.program.ProgramAnalyticsService;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping(value = "/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    ProgramAnalyticsService programAnalyticsService;

    @Autowired
    private AdminExerciseService adminExerciseService;

    private final FitwiseShareService fitwiseShareService;
    private final AdminDashBoardService adminDashBoardService;

    @PutMapping(value = "/update/tax")
    public ResponseModel createExercise(@RequestBody TaxPercentageModel taxPercentageModwel) {
        ResponseModel response;
        try {
            response = adminService.storeTaxPercentageDetails(taxPercentageModwel);
        } catch (ApplicationException aex) {
            response = new ResponseModel();
            response.setStatus(aex.getStatus());
            response.setError(aex.getMessage());
        }
        return response;
    }

    @GetMapping(value = "/checkWorkoutCompletion")
    public ResponseModel checkWorkoutCompletion(@RequestParam Long userId, @RequestParam Long programId, @RequestParam Long workoutScheduleId) {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, adminService.checkIfWorkoutCompleted(userId, programId, workoutScheduleId));
    }

    @GetMapping(value = "/getProgramDetails")
    public ResponseModel getProgram(@RequestParam final Long programId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, adminService.getProgram(programId));
    }

    /**
     * Get revenue statistics for specific program
     *
     * @param date
     * @param programId
     * @param isRenewDataNeeded
     * @return
     */
    @GetMapping("/getProgramRevenueStatsOfAnInstructor")
    public ResponseModel getProgramRevenueStatsOfAnInstructor(@RequestParam String date, @RequestParam long programId, @RequestParam boolean isRenewDataNeeded) throws ParseException {
        return programAnalyticsService.getProgramRevenueStatsOfAnInstructor(date, programId, isRenewDataNeeded);
    }

    /**
     * Get subscription statistics for specific program
     *
     * @param date
     * @param programId
     * @param isRenewDataNeeded
     * @return
     */
    @GetMapping("/getProgramSubscriptionStatsOfAnInstructor")
    public ResponseModel getProgramSubscriptionStatsOfAnInstructor(@RequestParam String date, @RequestParam long programId, @RequestParam boolean isRenewDataNeeded) throws ParseException {
        return programAnalyticsService.getProgramSubscriptionStatsOfAnInstructor(date, programId, isRenewDataNeeded);

    }

    /**
     * To get overview of program analytics
     *
     * @param programId
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/getProgramAnalyticsOverview")
    public ResponseModel getProgramAnalytics(@RequestParam long programId) {
        return programAnalyticsService.getProgramAnalytics(programId);
    }

    /**
     * API to populate dummy subscription
     * @param programId
     * @param memberId
     * @return
     */
    @PutMapping(value = "/populateTestSubscription")
    public ResponseModel populateTestSubscription(@RequestParam Long programId, @RequestParam Long memberId) {

        adminService.populateTestSubscription(programId, memberId);
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(null);
        responseModel.setMessage("Successfully added Test Subscription");
        responseModel.setStatus(Constants.CREATED_STATUS);

        return responseModel;
    }

    /**
     * Get Exercise categories with count
     * @return
     */
    @GetMapping(value = "/exercise/categoriesWithCount")
    public ResponseModel getExerciseCategoriesWithCount(){
        return adminExerciseService.getExerciseCategoriesWithCount();
    }

    /**
     * Get All stock exercises
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param categoryId
     * @param searchName
     * @return
     */
    @GetMapping(value = "/exercises")
    public ResponseModel getAllStockExercises(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy,
                                              @RequestParam Optional<Long> categoryId, @RequestParam Optional<String> searchName){
        return adminExerciseService.getAllStockExercises(pageNo, pageSize, sortOrder, sortBy, categoryId,  searchName);
    }

    /**
     * Delete Exercise
     * @param exerciseId
     * @return
     */
    @DeleteMapping(value = "/exercise")
    public ResponseModel deleteExercise(@RequestParam Long exerciseId){
        return adminExerciseService.deleteExercise(exerciseId);
    }

    /**
     * Create Exercise
     * @param adminExerciseModel
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/exercise/create")
    public ResponseModel createExercise(@RequestBody AdminExerciseModel adminExerciseModel) throws IOException {
        return adminExerciseService.createExercise(adminExerciseModel);
    }

    /**
     * Edit exercise
     * @param adminExerciseModel
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    @PostMapping(value = "/exercise/edit")
    public ResponseModel editExercise(@RequestBody AdminExerciseModel adminExerciseModel) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return adminExerciseService.editExercise(adminExerciseModel);
    }

    /**
     * Validate exercise title
     * @param exerciseName
     * @return
     */
    @GetMapping(value = "/exercise/validate/name")
    public ResponseModel validateProgramName(@RequestParam String exerciseName){
        adminExerciseService.validateName(exerciseName);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SCS_EX_NAME_VALID, null);
    }

    /**
     * Get exercise
     * @param exerciseId
     * @return
     */
    @GetMapping(value = "/exercise")
    public ResponseModel getExercise(@RequestParam Long exerciseId){
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, adminExerciseService.getExercise(exerciseId));
    }

    /**
     * Update default share
     * @param subscriptionTypeId Type of subscription
     * @param share Share in percentage
     * @return Success response
     */
    @PutMapping(value = "/update/fitwise/share")
    public ResponseModel updateFitwiseShare(@RequestParam Long subscriptionTypeId, @RequestParam Long share){
        fitwiseShareService.updateFitwiseShare(subscriptionTypeId, share);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.SUCCESS_SUBSCRIPTION_SHARE_UPDATE, null);
    }

    /**
     * Update instructor level share
     * @param subscriptionTypeId Type of subscription
     * @param email Instructor email
     * @param share Share in percentage
     * @return Success response
     */
    @PutMapping(value = "/update/fitwise/share/instructor")
    public ResponseModel updateFitwiseShare(@RequestParam Long subscriptionTypeId, @RequestParam String email, @RequestParam Long share){
        fitwiseShareService.updateFitwiseShare(subscriptionTypeId, email, share);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.SUCCESS_SUBSCRIPTION_SHARE_UPDATE, null);
    }

    /**
     * Get tiertypes and counts by range
     * @param startDate starting date
     * @param frequency whether the frequency weekly, monthly , yearly
     * @return Success response
     */
    @GetMapping(value = "/getTierTypesAndCounts")
    public ResponseModel getTierTypesAndCounts(@RequestParam @DateTimeFormat(pattern= StringConstants.PATTERN_DATE) @Valid Date startDate,
                                                     @RequestParam String frequency) throws ParseException {
        return adminDashBoardService.getTierTypesAndCounts(startDate,frequency);
    }
}
