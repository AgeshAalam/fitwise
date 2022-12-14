package com.fitwise.rest.block;

import com.fitwise.block.service.AdminBlockedService;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/admin/block")
public class AdminBlockedController {

    /**
     * The admin blocked service.
     */
    @Autowired
    private AdminBlockedService adminBlockedService;

    /**
     *  blocks the user.
     *
     * @param userId
     * @param role of the user
     * @return the response model
     * @throws ApplicationException the application exception
     */
    @GetMapping(value = "/blockUser")
    public ResponseModel blockUser(@RequestParam Long userId , @RequestParam String role){
        return adminBlockedService.blockUser(userId, role);
    }


    /**
     *  un block the user.
     *
     * @param userId
     * @param role of the user
     * @return the response model
     * @throws ApplicationException the application exception
     */
    @GetMapping(value = "/unBlockUser")
    public ResponseModel unBlockUser(@RequestParam Long userId, @RequestParam String role ){
        return adminBlockedService.unBlockUser(userId, role);
    }

    /**
     *  blocks the program.
     *
     * @param programId
     * @return the response model
     * @throws ApplicationException the application exception
     */
    @GetMapping(value = "/blockProgram")
    public ResponseModel blockProgram(@RequestParam Long programId){
        return adminBlockedService.blockProgram(programId, KeyConstants.KEY_PROGRAM_BLOCK);
    }


    /**
     *  un block the program.
     *
     * @param programId
     * @return the response model
     * @throws ApplicationException the application exception
     */
    @GetMapping(value = "/unBlockProgram")
    public ResponseModel unBlockProgram(@RequestParam Long programId){
        return adminBlockedService.unBlockProgram(programId, KeyConstants.KEY_PROGRAM_BLOCK);
    }

    /**
     * API to block a subscriptionPackage
     * @param subscriptionPackageId
     * @return
     */
    @PutMapping(value = "/blockPackage")
    public ResponseModel blockPackage(@RequestParam Long subscriptionPackageId){
        adminBlockedService.blockPackage(subscriptionPackageId, KeyConstants.KEY_PACKAGE_BLOCK);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PACKAGE_BLOCKED, null);
    }

    /**
     * API to unblock a Subscription Package
     * @param subscriptionPackageId
     * @return
     */
    @PutMapping(value = "/unblockPackage")
    public ResponseModel unblockPackage(@RequestParam Long subscriptionPackageId){
        adminBlockedService.unblockPackage(subscriptionPackageId, KeyConstants.KEY_PACKAGE_BLOCK);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PACKAGE_UNBLOCKED, null);
    }

}
