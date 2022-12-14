package com.fitwise.rest.admin;

import com.fitwise.service.admin.AdminDashBoardMemberService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;


/*
 * Created by Vignesh G on 25/03/20
 */

@RestController
@RequestMapping(value = "/v1/admin/dashboard/member")
public class AdminDashBoardMemberController {

    @Autowired
    private AdminDashBoardMemberService adminDashBoardMemberService;

    @GetMapping("/getMemberActivity")
    public ResponseModel getMemberActivity(@RequestParam String startDate, @RequestParam String endDate, @RequestParam boolean isForYear) {
        ResponseModel responseModel;
        if (isForYear) {
            responseModel = adminDashBoardMemberService.getMemberActivityForYear(startDate, endDate);
        } else {
            responseModel = adminDashBoardMemberService.getMemberActivityForMonth(startDate, endDate);
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

    @GetMapping("/spent")
    public ResponseModel getSpent(@RequestParam String startDate, @RequestParam String endDate, @RequestParam boolean isForYear) throws ParseException {
        ResponseModel response;
        if (isForYear) {
            response = adminDashBoardMemberService.getSpentForYear(startDate, endDate);
        } else {
            response = adminDashBoardMemberService.getSpentForMonth(startDate, endDate);
        }
        return response;
    }

    @GetMapping("/spentByPlatform")
    public ResponseModel getSpentByPlatform(@RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardMemberService.spentByPlatform(startDate, endDate);
    }

    @GetMapping("/clientAcquisition")
    public ResponseModel getClientAcquisition(@RequestParam String startDate, @RequestParam String endDate) {
        return adminDashBoardMemberService.getMemberClientAcquisition(startDate, endDate);
    }

}