package com.fitwise.rest.member;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.member.PackageFilterModel;
import com.fitwise.service.member.MemberPackageService;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Optional;

/**
 * The Class MemberPackageController.
 */
@RestController
@RequestMapping(value = "/v1/member/package")
@RequiredArgsConstructor
public class MemberPackageController {

    private final MemberPackageService memberPackageService;

    /**
     * Get subscription packages for member
     * @param pageNo
     * @param pageSize
     * @param filterModel
     * @param search
     * @return
     * @throws ApplicationException
     */
    @PutMapping(value = "/getSubscriptionPackages")
    public ResponseModel getSubscriptionPackages(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestBody PackageFilterModel filterModel, @RequestParam Optional<String> search) {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, memberPackageService.getSubscriptionPackages(pageNo, pageSize, filterModel, search));
    }

    @GetMapping(value = "/getPackageFilters")
    public ResponseModel getSubscriptionPackages() {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(memberPackageService.getSubscriptionPackageFilter());
        return responseModel;
    }

    @GetMapping(value = "/getSubscriptionPackage")
    public ResponseModel getPackageDetails(@RequestParam Long packageId,@RequestParam Optional<String> token) throws ParseException, UnsupportedEncodingException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, memberPackageService.getSubscriptionPackageDetails(packageId,token));
    }

    @GetMapping(value = "/myPackages")
    public ResponseModel getMyPackages(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String subscriptionStatus, @RequestParam Optional<String> searchName) {
        return memberPackageService.getMyPackages(subscriptionStatus,pageNo,pageSize,searchName);
    }

    @GetMapping(value = "/subscribedPackages")
    public ResponseModel getSubscribedPackages(@RequestParam final int pageNo, @RequestParam final int pageSize) {
        return memberPackageService.getSubscribedPackagesList(pageNo, pageSize);
    }

    /**
     * Get Instructor's packages
     * @param userId Instructor user id
     * @param pageNo Page number
     * @param pageSize Page size
     * @return Response with packages
     */
    @GetMapping(value = "/getInstructorPackages")
    public ResponseModel getInstructorPackages(@RequestParam final Long userId, @RequestParam final int pageNo, @RequestParam final int pageSize, Optional<String> search) {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(memberPackageService.getInstructorPackages(userId, pageNo, pageSize, search));
        return responseModel;
    }

    @GetMapping(value = "/mySchedulesInPackage")
    public ResponseModel getSchedules(@RequestParam Long subscriptionPackageId) throws ParseException {
        return memberPackageService.getPackageSchedules(subscriptionPackageId);
    }

    @GetMapping(value = "/subscribedPackagesAndSessions")
    public ResponseModel getSubscribedPackagesAndSessions() {
        return memberPackageService.getSubscribedPackagesAndSessions();
    }

}
