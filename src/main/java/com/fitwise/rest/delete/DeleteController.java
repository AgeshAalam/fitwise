package com.fitwise.rest.delete;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.delete.service.DeleteService;
import com.fitwise.exception.ApplicationException;
import com.fitwise.request.DeleteReasonsRequest;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping(value="/v1/delete")
public class DeleteController {

    /**
     * The delete service.
     */
    @Autowired
    private DeleteService deleteService;

    /**
     *  deletes the user.
     *
     * @param userId
     * @param role of the user
     * @return the response model
     * @throws ApplicationException the application exception
     */
    @DeleteMapping(value = "/admin/deleteUserAccount")
    public ResponseModel deleteUserAccount(@RequestParam Long userId, @RequestParam String role) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return deleteService.deleteUserAccount(userId , role);
    }

    /**
     * Delete Instructor.
     *
     * @param deleteReasonsRequest of the instructor
     * @return ResponseModel
     */
    @DeleteMapping(value = "/deleteInstructorAccount")
    public  ResponseModel deleteInstructorAccount(@RequestBody DeleteReasonsRequest deleteReasonsRequest) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return deleteService.deleteInstructorAccount(deleteReasonsRequest);
    }

    /**
     * Delete Member.
     *
     * @return ResponseModel
     */
    @DeleteMapping(value = "/deleteMemberAccount")
    public  ResponseModel deleteMemberAccount(@RequestBody  DeleteReasonsRequest deleteReasonsRequest) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return deleteService.deleteMemberAccount(deleteReasonsRequest);
    }

    /**
     *  delete the program via admin.
     *
     * @param programId
     * @return the response model
     * @throws ApplicationException the application exception
     */
    @DeleteMapping(value = "/admin/deleteProgramViaAdmin")
    public ResponseModel deleteProgramViaAdmin(@RequestParam Long programId){
        return deleteService.deleteProgramViaAdmin(programId);
    }

    /**
     *  delete the program via instructor
     *
     * @param programId
     * @return the response model
     * @throws ApplicationException the application exception
     */
    @DeleteMapping(value = "/deleteProgramViaInstructor")
    public ResponseModel deleteProgramViaInstructor(@RequestParam Long programId){
        return deleteService.deleteProgramViaInstructor(programId);
    }

    /**
     * get all the delete reasons
     */
    @GetMapping(value = "/getDeleteReasons")
    public ResponseModel getDeleteReasons(){
        return  new ResponseModel(Constants.SUCCESS_STATUS , MessageConstants.MSG_DELETE_REASONS_RETRIEVED, deleteService.getDeleteReasons());
    }

    /**
     * API to delete SubscriptionPackage
     * @param subscriptionPackageId
     * @return
     */
    @DeleteMapping(value = "/instructor/deleteSubscriptionPackage")
    public ResponseModel deleteSubscriptionPackageViaInstructor(@RequestParam Long subscriptionPackageId){
        deleteService.deleteSubscriptionPackageViaInstructor(subscriptionPackageId);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_PACKAGE_DELETED, null);
    }

    /**
     * API to delete SubscriptionPackage
     * @param subscriptionPackageId
     * @return
     */
    @DeleteMapping(value = "/admin/deleteSubscriptionPackage")
    public ResponseModel deleteSubscriptionPackageViaAdmin(@RequestParam Long subscriptionPackageId){
        deleteService.deleteSubscriptionPackageViaAdmin(subscriptionPackageId);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_PACKAGE_DELETED, null);
    }

}
