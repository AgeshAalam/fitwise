package com.fitwise.rest.packaging;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.packaging.PackageSessionCount;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.packaging.OfferSubscriptionPackageModel;
import com.fitwise.model.packaging.SubscriptionPackageModel;
import com.fitwise.service.StaticContentService;
import com.fitwise.service.packaging.SubscriptionPackageService;
import com.fitwise.view.ResponseModel;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

/*
 * Created by Vignesh G on 22/09/20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/subscriptionPackage")
public class SubscriptionPackageController {

    private final SubscriptionPackageService subscriptionPackageService;
    private final StaticContentService staticContentService;

    /**
     * API to create subscription package
     * @param model
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    @PutMapping("/create")
    public ResponseModel createSubscriptionPackage(@RequestBody SubscriptionPackageModel model) {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_SUBSCRIPTION_PACKAGE_SAVED);
        response.setPayload(subscriptionPackageService.createSubscriptionPackage(model));
        return response;
    }

    /**
     * API for Package name duplicate validation
     * @param packageName
     * @return
     */
    @GetMapping(value = "/validate/name")
    public ResponseModel validatePackageName(@RequestParam String packageName) {
        subscriptionPackageService.validatePackageName(packageName);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIPTION_PACKAGE_NAME_VALID, null);
    }

    /**
     * API to get instructor's SubscriptionPackage
     * @param subscriptionPackageId
     * @return
     * @throws ApplicationException
     */
    @GetMapping
    public ResponseModel getSubscriptionPackage(@RequestParam Long subscriptionPackageId) {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(subscriptionPackageService.getSubscriptionPackage(subscriptionPackageId));
        return response;
    }

    /**
     * API to get Session types
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/sessionTypes")
    public ResponseModel getSessionTypes() {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(subscriptionPackageService.getSessionTypes());
        return response;
    }

    /**
     * API to publish SubscriptionPackage
     * @param subscriptionPackageId
     * @return
     * @throws ApplicationException
     */
    @PutMapping("/publish")
    public ResponseModel publishSubscriptionPackage(@RequestParam Long subscriptionPackageId) throws StripeException{
        subscriptionPackageService.publishSubscriptionPackage(subscriptionPackageId);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_SUBSCRIPTION_PACKAGE_PUBLISHED);
        response.setPayload(null);
        return response;
    }

    /**
     * API to unpublish SubscriptionPackage
     * @param subscriptionPackageId
     * @return
     * @throws ApplicationException
     */
    @PutMapping("/unpublish")
    public ResponseModel unpublishSubscriptionPackage(@RequestParam Long subscriptionPackageId) {
        subscriptionPackageService.unpublishSubscriptionPackage(subscriptionPackageId);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_SUBSCRIPTION_PACKAGE_UNPUBLISHED);
        response.setPayload(null);
        return response;
    }

    /**
     * API to delete in progress Subscription package
     * @param subscriptionPackageId
     * @return
     * @throws ApplicationException
     */
    @DeleteMapping
    public ResponseModel deleteInProgressSubscriptionPackage(@RequestParam Long subscriptionPackageId) {
        subscriptionPackageService.deleteInProgressSubscriptionPackage(subscriptionPackageId);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_SUBSCRIPTION_PACKAGE_DELETED);
        response.setPayload(null);
        return response;
    }

    /**
     * API to remove program from SubscriptionPackage
     * @param subscriptionPackageId
     * @param programId
     * @return
     * @throws ApplicationException
     */
    @DeleteMapping("/removeProgram")
    public ResponseModel removeProgram(@RequestParam Long subscriptionPackageId, @RequestParam Long programId) {
        subscriptionPackageService.removeProgram(subscriptionPackageId, programId);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_SUBSCRIPTION_PACKAGE_PROGRAM_REMOVED);
        response.setPayload(null);
        return response;
    }

    /**
     * API to delete session from SubscriptionPackage
     * @param subscriptionPackageId
     * @param meetingId
     * @return
     * @throws ApplicationException
     */
    @DeleteMapping("/deleteSession")
    public ResponseModel deleteSession(@RequestParam Long subscriptionPackageId, @RequestParam Long meetingId) {
        return  subscriptionPackageService.removeSessionFromPackage(subscriptionPackageId, meetingId);
    }

    /**
     * API to get Tax model for packages
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/taxDetails")
    public ResponseModel getPackageTaxDetails(@RequestParam Long packageId) {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(subscriptionPackageService.getPackageTaxDetails(packageId));
        return response;
    }

    /**
     * API to get Price break down model for packages
     * @param price
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/priceBreakdown")
    public ResponseModel getPackagePriceBreakdown(@RequestParam Double price, @RequestParam Long packageId) {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(subscriptionPackageService.getPackagePriceBreakdown(price, packageId));
        return response;
    }

    /**
     * API to get all published Subscription Packages
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param searchname
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/all/published")
    public ResponseModel getPublishedPackages(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> searchname) {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(subscriptionPackageService.getPublishedPackages(pageNo, pageSize, sortOrder, sortBy, searchname));
        return response;
    }

    /**
     * API to get all inprogress Subscription Packages
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param searchname
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/all/inprogress")
    public ResponseModel getInProgressPackages(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> searchname) {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(subscriptionPackageService.getInProgressPackages(pageNo, pageSize, sortOrder, sortBy, searchname));
        return response;
    }

    /**
     * API to get all unpublished Subscription Packages
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param searchname
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/all/unpublished")
    public ResponseModel getUnPublishedPackages(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> searchname) {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(subscriptionPackageService.getUnPublishedPackages(pageNo, pageSize, sortOrder, sortBy, searchname));
        return response;
    }

    /**
     * API to get all blocked Subscription Packages
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param searchname
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/all/blocked")
    public ResponseModel getBlockedPackages(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> searchname) {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(subscriptionPackageService.getBlockedPackages(pageNo, pageSize, sortOrder, sortBy, searchname));
        return response;
    }

    /**
     * API to edit Unpublish or block SubscriptionPackage
     *
     * @param model
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    @PutMapping("/restricted/edit")
    public ResponseModel restrictedPackageEdit(@RequestBody SubscriptionPackageModel model) {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_SUBSCRIPTION_PACKAGE_SAVED);
        response.setPayload(subscriptionPackageService.restrictedPackageEdit(model));
        return response;
    }

    /**
     *
     * @param offerSubscriptionPackageModel
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws ParseException
     * @throws KeyManagementException
     */
    @PostMapping("/addOffers")
    public ResponseModel addOffersInProgram(@RequestBody OfferSubscriptionPackageModel offerSubscriptionPackageModel) {
        return subscriptionPackageService.addOffersInSubscriptionPackage(offerSubscriptionPackageModel);
    }

    /**
     * Removing offers which has high price than subscription package price
     * @param subscriptionPackageId
     * @param newPackagePrice
     * @return
     */
    @GetMapping("/removeOffersWithHigherPrice")
    public ResponseModel removeOffersWithHigherPrice(@RequestParam Long subscriptionPackageId, @RequestParam Double newPackagePrice){
        return subscriptionPackageService.removeOffersWithHigherPrice(subscriptionPackageId,newPackagePrice);
    }

    @Deprecated
    @GetMapping("/sessionCountPerWeek")
    public ResponseModel getSessionCountPerWeek(){
        return subscriptionPackageService.getCountPerWeek();
    }

    /**
     * Get all the subscription package session counts
     * @return Response model with package session counts
     */
    @GetMapping("/session/count/all")
    public ResponseModel getPackageSessionCounts(){
        List<PackageSessionCount> packageSessionCounts = staticContentService.getPackageSessionCounts();
        if(packageSessionCounts.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, null, null);
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, packageSessionCounts);
    }

    @GetMapping("/cancellationDuration")
    public ResponseModel getSessionCountPerWeek(@RequestParam boolean isDays){
        return subscriptionPackageService.getCancellationDuration(isDays);
    }

    /**
     * Get instructor meetings without pagination for configure phase
     * @param meetingTypeId
     * @return
     */
    @GetMapping("/myMeetings")
    public ResponseModel getMyMeetings(@RequestParam Long meetingTypeId){
        return subscriptionPackageService.getMeetings(meetingTypeId);
    }

    @GetMapping("/allInstructorPackages")
    public ResponseModel getAllInstructorPackages(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String sortBy, @RequestParam String sortOrder, @RequestParam Optional<String> search) {
        return subscriptionPackageService.getAllInstructorPackages(pageNo, pageSize, sortBy, sortOrder, search);
    }

}
