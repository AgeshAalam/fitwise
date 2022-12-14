package com.fitwise.rest.dynamiclink;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.service.dynamiclink.DynamicLinkService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.dynamiclink.DynamicLinkRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/*
 * Created by Vignesh G on 25/08/20
 */
@RestController
@RequestMapping(value = "/v1/dynamiclink")
public class DynamicLinkController {

    @Autowired
    DynamicLinkService dynamicLinkService;

    @PutMapping("/shortlink")
    public ResponseModel getShortLink(@RequestBody DynamicLinkRequest dynamicLinkRequest) {
        String shortLink = dynamicLinkService.getShortLinkForParams(dynamicLinkRequest);

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("shortLink", shortLink);

        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DYNAMIC_LINK_CREATED);
        responseModel.setPayload(responseMap);
        return responseModel;
    }

    /**
     * API to get short link for program - to be shared with members
     * @param programId
     * @return
     */
    @GetMapping("/member/program")
    public ResponseModel getProgramLinkForMember(@RequestParam Long programId) {
        String shortLink = dynamicLinkService.constructProgramLinkForMember(programId);

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("shortLink", shortLink);

        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DYNAMIC_LINK_CREATED);
        responseModel.setPayload(responseMap);
        return responseModel;
    }

    /**
     * API to get short link for current instructor profile - to be shared with members
     * @return
     */
    @GetMapping("member/instructorProfile")
    public ResponseModel getInstructorProfileLinkForMember() {
        String shortLink = dynamicLinkService.constructInstructorProfileLinkForMember();

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("shortLink", shortLink);

        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DYNAMIC_LINK_CREATED);
        responseModel.setPayload(responseMap);
        return responseModel;
    }

    /**
     * API to get short link for SubscriptionPackage - to be shared with members
     * @param subscriptionPackageId
     * @return
     */
    @GetMapping("/member/package")
    public ResponseModel getPackageLinkForMember(@RequestParam Long subscriptionPackageId) {
        String shortLink = dynamicLinkService.constructPackageLinkForMember(subscriptionPackageId);

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("shortLink", shortLink);

        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DYNAMIC_LINK_CREATED);
        responseModel.setPayload(responseMap);
        return responseModel;
    }


}
