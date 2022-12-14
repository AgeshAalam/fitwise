package com.fitwise.rest.program;


import com.fitwise.exception.ApplicationException;
import com.fitwise.service.program.ProgramAnalyticsService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

/*
 * Created by Vignesh G on 17/03/20
 */

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/program/analytics")
public class ProgramAnalyticsController {

    @Autowired
    ProgramAnalyticsService programAnalyticsService;

    @GetMapping("/getProgramClientDemographics")
    public ResponseModel getProgramClientDemographics(@RequestParam long programId, @RequestParam String graphBasis, @RequestParam String startDate, @RequestParam String endDate) throws ApplicationException {
        return programAnalyticsService.getProgramClientDemographics(programId, graphBasis, startDate, endDate);
    }

    @GetMapping("/getSubscriptionByPlatform")
    public ResponseModel getSubscriptionByPlatform(@RequestParam String startDate, @RequestParam String endDate, @RequestParam long programId) throws ApplicationException {
        return programAnalyticsService.getSubscriptionByPlatform(startDate, endDate, programId);
    }

    @GetMapping(value = "/getDiscardWorkoutFeedBackStats")
    public ResponseModel getDiscardWorkoutFeedBackStats(@RequestParam Long programId,@RequestParam String startDate, @RequestParam String endDate) {
        return programAnalyticsService.getDiscardWorkoutFeedBackStats(programId,startDate,endDate);
    }

    @GetMapping(value = "/getAllOtherDiscardWorkoutFeedBack")
    public ResponseModel getAllOtherDiscardWorkoutFeedBack(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam Long programId) {
        return programAnalyticsService.getAllOtherDiscardWorkoutFeedBack(pageNo, pageSize, programId);
    }

    @GetMapping(value = "/getWorkoutNormalFeedback")
    public ResponseModel getWorkoutNormalFeedback(@RequestParam Long programId,@RequestParam String startDate, @RequestParam String endDate){
        return programAnalyticsService.getWorkoutNormalFeedback(programId,startDate,endDate);
    }

    @GetMapping(value = "/getInstructorProgramSubscriptionStats")
    public ResponseModel getInstructorProgramSubscriptionStats(@RequestParam String date , @RequestParam Long programId) throws ParseException {
        return  programAnalyticsService.getInstructorProgramSubscriptionStats(date , programId);
    }

    @GetMapping(value = "/getClientAcquisition")
    public ResponseModel getClientAcquisition(@RequestParam Long programId,@RequestParam String startDate, @RequestParam String endDate){
        return programAnalyticsService.getClientAcquisition(programId,startDate,endDate);
    }

    @GetMapping(value = "/getClientAcquisitionUsers")
    public ResponseModel getClientAcquisitionUsers(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam Long programId, @RequestParam String clientAcquisitionKey,@RequestParam String startDate ,@RequestParam String endDate){
        return programAnalyticsService.getClientAcquisitionUsers(pageNo, pageSize , programId, clientAcquisitionKey,startDate, endDate);
    }

    @GetMapping("/getRevenueData")
    public ResponseModel getRevenueData(@RequestParam String startDate, @RequestParam String endDate, @RequestParam boolean isForYear, @RequestParam long programId) throws ApplicationException, ParseException {

        ResponseModel responseModel = null;
        if (isForYear) {
            responseModel = programAnalyticsService.getRevenueDataForYear(startDate, endDate, programId);
        } else {
            responseModel = programAnalyticsService.getRevenueDataForMonth(startDate, endDate, programId);
        }
        return responseModel;

    }

    @GetMapping("/getProgramRevenueByPlatform")
    public ResponseModel getRevenueByPlatform(@RequestParam long programId, @RequestParam String startDate, @RequestParam String endDate) throws ApplicationException {
        return programAnalyticsService.getProgramRevenueByPlatform(programId,startDate, endDate);
    }

    @GetMapping("/populateHistoryDataForSubscriptionAudit")
    public ResponseModel populateHistoryDataForSubscriptionAudit() {
        return programAnalyticsService.populateHistoryDataForSubscriptionAudit();
    }
}
