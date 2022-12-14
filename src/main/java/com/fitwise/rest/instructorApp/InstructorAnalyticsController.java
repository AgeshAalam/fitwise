package com.fitwise.rest.instructorApp;

import com.fitwise.exception.ApplicationException;
import com.fitwise.service.instructor.InstructorAnalyticsService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

/**
 * The Class InstructorAnalyticsController.
 */
@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/instructor")
public class InstructorAnalyticsController {

    @Autowired
    InstructorAnalyticsService instructorAnalyticsService;

/*    @GetMapping("/getInstructorOverviewStats")
    public ResponseModel getInstructorOverviewStats() {
        return instructorAnalyticsService.getInstructorStatsOverview();
    }

    @GetMapping("/getProgramStatsOverviewOfAnInstructor")
    public ResponseModel getProgramStatsOverviewOfAnInstructor() {
        return instructorAnalyticsService.getProgramStatsOverviewOfAnInstructor();
    }*/

    /**
     * Method returns data for NEW OR RENEW Revenue Graph. New or Renew option is accepted as param.
     * Used only on Mobile App.
     *
     * For Yearly data, send date as only year - 2o2o
     * For Monthly data, send date  as month/year - 10/2020
     *
     * @param date
     * @param isRenewDataNeeded
     * @return
     */
    @GetMapping("/getInstructorRevenueData")
    public ResponseModel getInstructorRevenue(@RequestParam String date, @RequestParam boolean isRenewDataNeeded) throws ParseException {
        return instructorAnalyticsService.getInstructorRevenueData(date, isRenewDataNeeded, null);
    }

    /**
     * Returns the instructor subscription statistics count for Year or Month/Year
     *
     * @param date
     * @param isRenewDataNeeded
     * @return
     */
    @GetMapping("/getInstructorSubscriptionStatsCount")
    public ResponseModel getInstructorSubscriptionStatsCount(@RequestParam String date, @RequestParam boolean isRenewDataNeeded) throws ParseException {
        return instructorAnalyticsService.getInstructorSubscriptionStatsCount(date, isRenewDataNeeded, null);

    }

    /**
     * Returns the instructor subscription statistics count for Year or Month/Year
     *
     * @param date
     * @return
     */
    @GetMapping("/getInstructorMonthAndYearSubscriptionCount")
    public ResponseModel getInstructorMonthAndYearSubscriptionCount(@RequestParam String date) throws ParseException {
        return instructorAnalyticsService.getMonthAndYearSubscriptionCount(date, null);
    }

    @GetMapping("/getInstructorOverviewStats")
    public ResponseModel getInstructorAnalytics() throws ApplicationException {
        return instructorAnalyticsService.getInstructorAnalytics(null);
    }

    @GetMapping("/getClientDemographics")
    public ResponseModel getClientDemographics(@RequestParam String startDate, @RequestParam String endDate, @RequestParam String graphBasis) throws ApplicationException {
        return instructorAnalyticsService.getClientDemographics(startDate, endDate, graphBasis, null);
    }

    @GetMapping("/getSubscriptionByPlatform")
    public ResponseModel getSubscriptionByPlatform(@RequestParam String startDate, @RequestParam String endDate) throws ApplicationException {
        return instructorAnalyticsService.getSubscriptionByPlatform(startDate, endDate, null);
    }

    @GetMapping(value = "/getInstructorProgramsPerformanceStats")
    public ResponseModel getInstructorProgramsPerformanceStats(@RequestParam int pageNo, @RequestParam int pageSize ){
        return instructorAnalyticsService.getInstructorProgramsPerformanceStats(pageNo, pageSize, null);
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
    public ResponseModel getRevenueByPlatform(@RequestParam String startDate, @RequestParam String endDate) throws ApplicationException {
        return instructorAnalyticsService.getRevenueByPlatform(startDate, endDate, null);
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
    public ResponseModel getRevenueData(@RequestParam String startDate, @RequestParam String endDate, @RequestParam boolean isForYear) throws ApplicationException, ParseException {

        ResponseModel responseModel = null;
        if (isForYear) {
            responseModel = instructorAnalyticsService.getRevenueDataForYear(startDate, endDate, null);
        } else {
            responseModel = instructorAnalyticsService.getRevenueDataForMonth(startDate, endDate, null);
        }
        return responseModel;

    }

}
