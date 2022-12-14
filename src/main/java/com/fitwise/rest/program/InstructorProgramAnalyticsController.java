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
 * Created by Vignesh G on 08/04/20
 */
@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/instructor")
public class InstructorProgramAnalyticsController {

    @Autowired
    ProgramAnalyticsService programAnalyticsService;

    /**
     * Returns the program's revenue statistics of an Instructor
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
     * Returns the program subscription statistics of an Instructor
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

    @GetMapping("/getProgramStatsOverviewOfAnInstructor")
    public ResponseModel getProgramAnalytics(@RequestParam long programId) throws ApplicationException {
        return programAnalyticsService.getProgramAnalytics(programId);
    }

    //TODO : Do not user this Controller anymore. It will be removed later. User ProgramAnalyticsController.

}
