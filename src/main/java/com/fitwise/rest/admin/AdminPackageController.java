package com.fitwise.rest.admin;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.service.admin.AdminPackageService;
import com.fitwise.view.ResponseModel;

@RestController
@RequestMapping(value = "/v1/admin/subscriptionPackage")
public class AdminPackageController {

    @Autowired
    private AdminPackageService adminPackageService;

    /**
     * Get subscription package details
     * @param subscriptionPackageId
     * @return
     */
    @GetMapping(value = "/packageDetails")
    public ResponseModel getPackageDetails(@RequestParam Long subscriptionPackageId){
        return adminPackageService.getSubscriptionPackage(subscriptionPackageId);
    }

    /**
     * Get All subscription Packages
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @param sortOrder
     * @param blockStatus
     * @param search
     * @return
     */
    @GetMapping(value = "packagesList")
    public ResponseModel getAllPackages(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String sortBy, @RequestParam String sortOrder, @RequestParam String blockStatus, @RequestParam Optional<String> search){
        return adminPackageService.getPackages(pageNo,pageSize,sortBy,sortOrder,blockStatus,search);
    }

    /**
     * Get Instructor packages
     * @param pageNo
     * @param pageSize
     * @param instructorId
     * @return
     */
    @GetMapping(value = "/instructorPackages")
    public ResponseModel getInstructorPackages(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam Long instructorId){
        return adminPackageService.getInstructorPackages(pageNo,pageSize,instructorId);
    }

    /**
     * API to get the package subscription history of a member from admin
     * @param userId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping(value = "/memberPackageHistory")
    public ResponseModel getMemberPackageHistory(@RequestParam Long userId, @RequestParam int pageNo, @RequestParam int pageSize) {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(adminPackageService.getMemberPackageHistory(userId, pageNo, pageSize));

        return response;
    }
    
    
	/**
	 * Gets the all packages min details.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @param search the search
	 * @return the all packages min details
	 */
	@GetMapping("/all/mindetails")
	public ResponseModel getAllPackagesMinDetails(@RequestParam final int pageNo, @RequestParam final int pageSize,
			@RequestParam String search) {
		return adminPackageService.minimumPackageDetails(pageNo, pageSize, search);
	}

}
