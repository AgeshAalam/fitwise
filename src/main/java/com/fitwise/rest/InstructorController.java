package com.fitwise.rest;

import com.fitwise.constants.Constants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.Gender;
import com.fitwise.entity.TaxTypes;
import com.fitwise.entity.YearsOfExpertise;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.LocationModel;
import com.fitwise.model.instructor.VideoVersioningModel;
import com.fitwise.model.instructor.ZoomCredentialModel;
import com.fitwise.service.UserService;
import com.fitwise.service.admin.AdminExerciseService;
import com.fitwise.service.instructor.InstructorProgramService;
import com.fitwise.service.instructor.InstructorService;
import com.fitwise.view.AwardDeleteRequestView;
import com.fitwise.view.AwardsCreationView;
import com.fitwise.view.AwardsRequestView;
import com.fitwise.view.BasicDetailsRequestView;
import com.fitwise.view.CertificateDeleteRequestView;
import com.fitwise.view.CertificationCreationView;
import com.fitwise.view.CertificationRequestView;
import com.fitwise.view.ExperienceUserView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.TaxIdView;
import com.fitwise.view.UpdateProfileView;
import com.fitwise.view.instructor.OtherExpertiseView;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * The Class UserProfileController.
 */
@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping(value = "/v1/instructor")
public class InstructorController {

    /**
     * The user service.
     */
    @Autowired
    private UserService userService;

    @Autowired
    private InstructorProgramService instructorProgramService;

    @Autowired
    private InstructorService instructorService;

    @Autowired
    private AdminExerciseService adminExerciseService;

    @GetMapping("/getYearsOfExpertise")
    public ResponseModel getYearsOfExpertise() {
        List<YearsOfExpertise> yearsOfExpertiseList = userService.getYearsOfExpertise();
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(yearsOfExpertiseList);
        responseModel.setMessage(MessageConstants.MSG_YEARS_OF_EXPERTISE_RETRIEVED);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        return responseModel;
    }

    @GetMapping("/getTaxTypes")
    public ResponseModel getTaxTypes() {
        List<TaxTypes> taxTypesList = userService.getTaxTypes();
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(taxTypesList);
        responseModel.setMessage(MessageConstants.MSG_TAX_TYPES_RETRIEVED);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        return responseModel;
    }

    @GetMapping("/getGender")
    public ResponseModel getGender() {
        List<Gender> genderList = userService.getGender();
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(genderList);
        responseModel.setMessage(MessageConstants.MSG_GENDER_RETRIEVED);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        return responseModel;
    }

    @GetMapping("/getInstructorProfile")
    public ResponseModel getInstructorProfile() {
        return userService.getInstructorProfile();
    }

    @PostMapping("/addInstructorExperience")
    public ResponseModel addInstructorExperience(@RequestBody ExperienceUserView experienceUserView) {
        return userService.updateInstructorExperience(experienceUserView);
    }

    /**
     * Add the tax id with given details
     *
     * @param taxIdView
     * @return
     * @throws ApplicationException
     */
    @PostMapping("/updateTaxId")
    public ResponseModel addTaxId(@RequestBody TaxIdView taxIdView) {
        return userService.addTaxId(taxIdView);
    }

    /**
     * Delete the tax number
     *
     * @return
     */
    @DeleteMapping("/deleteTaxId")
    public ResponseModel deleteTaxId() {
        return userService.deleteTaxId();
    }

    @PostMapping("/createCertification")
    public ResponseModel createCertification(@RequestBody CertificationCreationView certificationCreationView) {
        return userService.createCertification(certificationCreationView);
    }

    @PostMapping("/updateCertification")
    public ResponseModel updateCertification(@RequestBody CertificationRequestView certificationRequestView) {
        return userService.updateCertification(certificationRequestView);

    }

    @PostMapping("/deleteCertification")
    public ResponseModel deleteCertification(@RequestBody CertificateDeleteRequestView certificateDeleteRequestView) {
        return userService.deleteCertification(certificateDeleteRequestView);

    }

    @PostMapping("/createAwards")
    public ResponseModel createAwards(@RequestBody AwardsCreationView awardsCreationView) {
        return userService.createAwards(awardsCreationView);

    }

    @PostMapping("/updateAwards")
    public ResponseModel updateAwards(@RequestBody AwardsRequestView awardsRequestView) {
        return userService.updateAwards(awardsRequestView);

    }

    @PostMapping("/deleteAwards")
    public ResponseModel deleteAwards(@RequestBody AwardDeleteRequestView awardDeleteRequestView) {
        return userService.deleteAwards(awardDeleteRequestView);

    }

    @PostMapping("/updateBasicDetails")
    public ResponseModel updateBasicDetails(@RequestBody BasicDetailsRequestView basicDetailsRequestView) {
        return userService.updateBasicDetails(basicDetailsRequestView);
    }

    @PostMapping("/updateInstructorProfile")
    public ResponseModel updateInstructorProfile(@RequestBody UpdateProfileView updateProfileView) {
        return instructorProgramService.updateInstructorProfile(updateProfileView);
    }

    /**
     * API to get details of a specific client
     *
     * @param userId
     * @return
     */
    @GetMapping("/getClientDetails")
    public ResponseModel getClientDetails(@RequestParam Long userId) {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_CLIENT_DETAILS_FETCHED);
        responseModel.setPayload(instructorProgramService.getClientDetails(userId));
        return responseModel;
    }

    /**
     * API to get Packages of Instructor's client
     * @param userId
     * @return
     * @throws ApplicationException
     */
    @GetMapping("/getClientPackages")
    public ResponseModel getClientPackages(@RequestParam Long userId) {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_CLIENT_DETAILS_FETCHED);
        responseModel.setPayload(instructorProgramService.getClientPackages(userId));
        return responseModel;
    }

    @PostMapping("/addOtherExpertise")
    public ResponseModel addOtherExpertise(@RequestBody List<OtherExpertiseView> otherExpertiseList) {
        return instructorProgramService.saveOtherExpertise(otherExpertiseList);
    }

    @DeleteMapping("/removeProfileImage")
    public ResponseModel removeProfileImage() {
        return userService.removeProfileImage();
    }


    @DeleteMapping("/removeCoverImage")
    public ResponseModel removeCoverImage() {
        return userService.removeCoverImage();
    }

    /**
     * Updated the upload completed for exercise
     *
     * @param entityId
     * @return
     */
    @PutMapping(value = "/exercise/video/uploadcompleted")
    public ResponseModel updateExerciseVideoUploaded(@RequestParam Long entityId) {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SCS_VIDEO_UPLOAD, instructorProgramService.updateVideoUploaded(entityId, InstructorConstant.VIDEO_TYPE_EXERCISE_VIDEO));
    }

    /**
     * Updated the upload completed for support
     *
     * @param entityId
     * @return
     */
    @PutMapping(value = "/exercise/support/video/uploadcompleted")
    public ResponseModel updateSupportVideoUploaded(@RequestParam Long entityId) {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SCS_VIDEO_UPLOAD, instructorProgramService.updateVideoUploaded(entityId, InstructorConstant.VIDEO_TYPE_EXERCISE_SUPPORT_VIDEO));
    }

    /**
     * Creating the version for program promotion video
     *
     * @param videoVersioningModel
     * @return
     */
    @PutMapping(value = "/program/promotion/video/version")
    public ResponseModel createExercisePromoVideoVersion(@RequestBody VideoVersioningModel videoVersioningModel) {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SCS_VIDEO_VERSION, instructorProgramService.createVideoVersion(videoVersioningModel, InstructorConstant.VIDEO_TYPE_PROMO_VIDEO, false));
    }

    /**
     * Updated the upload completed for promo
     *
     * @param entityId
     * @return
     * @throws IOException
     */
    @PutMapping(value = "/program/promotion/video/uploadcompleted")
    public ResponseModel updatePromoVideoUploaded(@RequestParam Long entityId) {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SCS_VIDEO_UPLOAD, instructorProgramService.updateVideoUploaded(entityId, InstructorConstant.VIDEO_TYPE_PROMO_VIDEO));
    }

    /**
     * API to update zoom link
     *
     * @param zoomCredentialModel
     * @return
     * @throws ApplicationException
     */
    @PutMapping(value = "/updateZoomCredentials")
    public ResponseModel updateZoomCredentials(@RequestBody ZoomCredentialModel zoomCredentialModel) {
        instructorService.updateZoomCredentials(zoomCredentialModel);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_ZOOM_LINK_UPDATED);
        response.setPayload(null);
        return response;
    }

    @GetMapping(value = "/externalClients")
    public ResponseModel getExternalClientsOfAnInstructor(@RequestParam Optional<String> search) {
        return instructorService.getExternalClients(search);
    }

    /**
     * Save Location
     *
     * @param locationModel
     * @return
     */
    @PostMapping(value = "/location")
    public ResponseModel saveLocation(@RequestBody LocationModel locationModel) {
        return instructorService.saveLocation(locationModel);
    }

    /**
     * Delete location
     *
     * @param locationId
     * @return
     */
    @DeleteMapping(value = "/location")
    public ResponseModel deleteLocation(@RequestParam Long locationId) {
        return instructorService.deleteLocation(locationId);
    }

    /**
     * Get All locations of an instructors
     *
     * @return
     */
    @GetMapping(value = "/locations")
    public ResponseModel getLocations() {
        return instructorService.getLocationsOfAnUser();
    }


    @GetMapping(value = "/locationTypes")
    public ResponseModel getLocationTypes() {
        return instructorService.getLocationTypes();
    }

    /**
     * Get Exercise categories with count
     * @return
     */
    @GetMapping(value = "/stockExercises/categoriesWithCount")
    public ResponseModel getExerciseCategoriesWithCount(){
        return adminExerciseService.getExerciseCategoriesWithCount();
    }

    /**
     * Get All stock exercises
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param categoryIds
     * @param searchName
     * @return
     */
    @GetMapping(value = "/stockExercises")
    public ResponseModel getAllStockExercises(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy,
                                              @RequestParam(required = false) List<Long> categoryIds, @RequestParam(required = false) List<Long> equipmentIds, @RequestParam Optional<String> searchName ){
        return adminExerciseService.getAllStockExercises(pageNo, pageSize, sortOrder, sortBy, categoryIds, equipmentIds, searchName, false);
    }
    
	/**
	 * Gets the all stock exercises videos.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @param categoryIds the category ids
	 * @param searchName the search name
	 * @return the all stock exercises videos
	 */
	@GetMapping(value = "/stockExercisesVideos")
	public ResponseModel getAllStockExercisesVideos(@RequestParam int pageNo, @RequestParam int pageSize,
			@RequestParam String sortOrder, @RequestParam String sortBy,
			@RequestParam(required = false) List<Long> categoryIds, @RequestParam Optional<String> searchName) {
		return adminExerciseService.getStockExercisesVideos(pageNo, pageSize, sortOrder, sortBy, categoryIds,
				searchName, false);
	}
	
	/**
     * Get All tier types
     *
     * @return
     */
    @GetMapping(value = "/getTierTypes")
    public ResponseModel getTierTypes() {
        return instructorService.getTierTypes();
    }

	/**
	 * Get User Tier Details
	 *
	 * @return
	 */
	@GetMapping(value = "/getUserTierDetails")
	public ResponseModel getUserTierDetails() {
		return instructorService.getUserTierDetails();
	}

    /**
     * Get Exiting Tier Details and Requested Tier Details
     * @param tierId Requested Tier Id
     * @return
     */
    @GetMapping(value = "/getExistingAndRequestedTierDetails")
    public ResponseModel getExistingAndRequestedTierDetails(@RequestParam Long tierId) {
        return instructorService.getExistingAndRequestedTierDetails(tierId);
    }

}