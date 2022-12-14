package com.fitwise.rest.admin;

import com.fitwise.service.admin.AdminDashBoardInstructorService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

/*
 * Created by Vignesh G on 23/03/20
 */
@RestController
@RequestMapping(value = "/v1/admin/dashboard/instructor")
public class AdminDashBoardInstructorController {

    @Autowired
    private AdminDashBoardInstructorService adminDashBoardInstructorService;

    @GetMapping("/getInstructorActivity")
    public ResponseModel getInstructorActivity(@RequestParam String startDate, @RequestParam String endDate, @RequestParam boolean isForYear) {
        ResponseModel responseModel;
        if (isForYear) {
            responseModel = adminDashBoardInstructorService.getInstructorActivityForYear(startDate, endDate);
        } else {
            responseModel = adminDashBoardInstructorService.getInstructorActivityForMonth(startDate, endDate);
        }
        return responseModel;
    }

    @GetMapping(value = "/getTopRatedInstructors")
    public ResponseModel getTopRatedInstructors(@RequestParam String startDate, @RequestParam  String endDate, @RequestParam boolean isForYear) throws ParseException {
        return adminDashBoardInstructorService.getTopRatedInstructors(startDate, endDate , isForYear );
    }

    @GetMapping("/revenuePayout")
    public ResponseModel getRevenuePayout(@RequestParam String startDate, @RequestParam String endDate, @RequestParam boolean isForYear) throws ParseException {
        ResponseModel response;
        if (isForYear) {
            response = adminDashBoardInstructorService.getRevenuePayoutForYear(startDate, endDate);
        } else {
            response = adminDashBoardInstructorService.getRevenuePayoutForMonth(startDate, endDate);
        }
        return response;
    }

    @GetMapping("/revenuePayoutByPlatform")
    public ResponseModel getRevenuePayoutByPlatform(@RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardInstructorService.getRevenuePayoutByPlatform(startDate, endDate);
    }

    @GetMapping("/topEarned")
    public ResponseModel getTopEarned(@RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardInstructorService.getTopEarned(startDate, endDate);
    }

}