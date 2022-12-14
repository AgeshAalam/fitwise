package com.fitwise.rest.admin;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.admin.AdminDashBoardPlatformService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/*
 * Created by Vignesh Gunasekar on 06/03/2020
 */
@RestController
@RequestMapping(value = "/v1/admin/dashboard/platform")
public class AdminDashBoardPlatformController {

    @Autowired
    private AdminDashBoardPlatformService adminDashBoardPlatformService;

    /**
     * API end point for Admin analytics overview
     * @return
     * @throws ApplicationException
     * @throws ParseException
     */
    @GetMapping(value = "/getAdminDashboardOverview")
    public ResponseModel getAdminDashboardOverviewData() {
        return adminDashBoardPlatformService.getAdminDashboardOverviewData();
    }

    /**
     * Get SubscriptionByProgramType for Admin dashboard graph on Platform tab.
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/getSubscriptionByProgramType")
    public ResponseModel getSubscriptionByProgramType(@RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardPlatformService.getSubscriptionByProgramType(startDate, endDate);
    }

    /**
     * Get All subscriptions of new and renewal
     *
     * @param date
     * @return responseModel
     * @throws ApplicationException
     */
    @GetMapping(value = "/getAllSubscriptionsOfNewAndRenewal")
    public ResponseModel getAllSubscriptionsOfNewAndRenewal(@RequestParam String date) throws ParseException {
        return adminDashBoardPlatformService.getAllSubscriptionsOfNewAndRenewal(date);
    }

    @GetMapping("/getSubscriptionByPlatform")
    public ResponseModel getSubscriptionByPlatform(@RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardPlatformService.getSubscriptionByPlatform(startDate, endDate);
    }

    @GetMapping(value = "/getFlaggedVideoAnalytics")
    public ResponseModel getFlaggedVideoAnalytics(@RequestParam String startDate, @RequestParam String endDate, @RequestParam boolean isForYear) {
        Map<String, Object> responseMap = new HashMap<>();
        Object trend = null;
        if (isForYear) {
            trend = adminDashBoardPlatformService.getFlaggedVideoTrendForYear(startDate, endDate);
        } else {
            trend = adminDashBoardPlatformService.getFlaggedVideoTrendForMonth(startDate, endDate);
        }
        if (trend != null) {
            responseMap.put(KeyConstants.KEY_TREND, trend);
        }
        Object flaggingReasons = adminDashBoardPlatformService.getFlaggingReasonDistribution(startDate, endDate);
        if (flaggingReasons != null) {
            responseMap.put(KeyConstants.KEY_REASONS, flaggingReasons);
        }
        if (responseMap.size() == 0) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(responseMap);
        return responseModel;
    }

    @GetMapping("/getPostWorkoutFeedbackAnalytics")
    public ResponseModel getPostWorkoutFeedbackAnalytics(@RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardPlatformService.getPostWorkoutFeedbackAnalytics(startDate, endDate);
    }

    @GetMapping("/getWorkoutDiscardFeedbackAnalytics")
    public ResponseModel getWorkoutDiscardFeedbackAnalytics(@RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardPlatformService.getWorkoutDiscardFeedbackAnalytics(startDate, endDate);
    }

    /**
     * API to get list of ppl who reported workout discard feedback as Others.
     * @param pageNo
     * @param pageSize
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping(value = "/getAllOtherDiscardWorkoutFeedBack")
    public ResponseModel getAllOtherDiscardWorkoutFeedBack(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardPlatformService.getAllOtherDiscardWorkoutFeedBack(pageNo, pageSize, startDate, endDate);
    }

    @GetMapping("/getRevenueSplitByPlatform")
    public ResponseModel getRevenueSplitByPlatform(@RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardPlatformService.getRevenueSplitByPlatform(startDate, endDate);
    }

    @GetMapping("/getRevenueSplitByPayout")
    public ResponseModel getRevenueSplitByPayout(@RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardPlatformService.getRevenueSplitByPayout(startDate, endDate);
    }

    @GetMapping("/revenueEarned")
    public ResponseModel getRevenueEarned(@RequestParam String startDate, @RequestParam String endDate, @RequestParam boolean isForYear) throws ParseException {
        ResponseModel response;
        if (isForYear) {
            response = adminDashBoardPlatformService.getRevenueEarnedForYear(startDate, endDate);
        } else {
            response = adminDashBoardPlatformService.getRevenueEarnedForMonth(startDate, endDate);
        }
        return response;
    }

}