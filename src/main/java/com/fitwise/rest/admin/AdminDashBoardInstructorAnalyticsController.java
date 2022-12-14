package com.fitwise.rest.admin;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.admin.AdminDashBoardMemberService;
import com.fitwise.service.instructor.InstructorAnalyticsService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping(value = "/v1/admin/dashboard/instructor/analytics")
public class AdminDashBoardInstructorAnalyticsController {

    @Autowired
    InstructorAnalyticsService instructorAnalyticsService;

    @Autowired
    private AdminDashBoardMemberService adminDashBoardMemberService;

    @GetMapping("/getInstructorRevenueData")
    public ResponseModel getInstructorRevenue(@RequestParam Long userId, @RequestParam String date, @RequestParam boolean isRenewDataNeeded) throws ParseException {
        if (userId == null || userId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }
        return instructorAnalyticsService.getInstructorRevenueData(date, isRenewDataNeeded, userId);
    }

    /**
     * Returns the instructor subscription statistics count for Year or Month/Year
     *
     * @param date
     * @param isRenewDataNeeded
     * @return
     */
    @GetMapping("/getInstructorSubscriptionStatsCount")
    public ResponseModel getInstructorSubscriptionStatsCount(@RequestParam String date, @RequestParam boolean isRenewDataNeeded, @RequestParam Long userId) throws ParseException {
        if (userId == null || userId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }
        return instructorAnalyticsService.getInstructorSubscriptionStatsCount(date, isRenewDataNeeded, userId);
    }

    /**
     * Returns the instructor subscription statistics count for Year or Month/Year
     *
     * @param date
     * @return
     */
    @GetMapping("/getInstructorMonthAndYearSubscriptionCount")
    public ResponseModel getInstructorMonthAndYearSubscriptionCount(@RequestParam String date, @RequestParam Long userId) throws ParseException {
        if (userId == null || userId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }
        return instructorAnalyticsService.getMonthAndYearSubscriptionCount(date, userId);
    }

    @GetMapping("/getInstructorOverviewStats")
    public ResponseModel getInstructorAnalytics(@RequestParam Long userId) {
        if (userId == null || userId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }
        return instructorAnalyticsService.getInstructorAnalytics(userId);
    }

    @GetMapping("/getClientDemographics")
    public ResponseModel getClientDemographics(@RequestParam String startDate, @RequestParam String endDate, @RequestParam String graphBasis, @RequestParam Long userId) {
        if (userId == null || userId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }
        return instructorAnalyticsService.getClientDemographics(startDate, endDate, graphBasis, userId);
    }

    @GetMapping("/getSubscriptionByPlatform")
    public ResponseModel getSubscriptionByPlatform(@RequestParam String startDate, @RequestParam String endDate, @RequestParam Long userId) {
        if (userId == null || userId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }
        return instructorAnalyticsService.getSubscriptionByPlatform(startDate, endDate, userId);
    }

    @GetMapping(value = "/getInstructorProgramsPerformanceStats")
    public ResponseModel getInstructorProgramsPerformanceStats(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam Long userId){
        if (userId == null || userId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }
        return instructorAnalyticsService.getInstructorProgramsPerformanceStats(pageNo, pageSize, userId);
    }

    /**
     * Method returns data for Revenue by Platform Chart.
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/getRevenueByPlatform")
    public ResponseModel getRevenueByPlatform(@RequestParam String startDate, @RequestParam String endDate, @RequestParam Long userId) {
        if (userId == null || userId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }
        return instructorAnalyticsService.getRevenueByPlatform(startDate, endDate, userId);
    }

    /**
     * Method returns data for NEW & RENEW Revenue Stacked Graph. Used only on Web App.
     *
     * @param startDate
     * @param endDate
     * @param isForYear
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/getRevenueData")
    public ResponseModel getRevenueData(@RequestParam String startDate, @RequestParam String endDate, @RequestParam boolean isForYear, @RequestParam Long userId) throws ParseException {
        if (userId == null || userId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_ID_NULL, MessageConstants.ERROR);
        }
        ResponseModel responseModel;
        if (isForYear) {
            responseModel = instructorAnalyticsService.getRevenueDataForYear(startDate, endDate, userId);
        } else {
            responseModel = instructorAnalyticsService.getRevenueDataForMonth(startDate, endDate, userId);
        }
        return responseModel;
    }

    @GetMapping("/getPreferredPrograms")
    public ResponseModel getPreferredPrograms(@RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardMemberService.getPreferredPrograms(startDate, endDate);
    }

    @GetMapping("/getPreferredDuration")
    public ResponseModel getPreferredDuration(@RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardMemberService.getPreferredDuration(startDate, endDate);
    }

    @GetMapping("/getUserDemographics")
    public ResponseModel getUserDemographics(@RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardMemberService.getUserDemographics(startDate, endDate);
    }
}