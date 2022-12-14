package com.fitwise.rest.v2.instructor;

import com.fitwise.constants.Constants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.program.model.InstructorPromoUploadModel;
import com.fitwise.program.model.PromoUploadModel;
import com.fitwise.program.service.ProgramService;
import com.fitwise.service.instructor.InstructorProgramService;
import com.fitwise.service.v2.instructor.InstructorProfileService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.instructor.BasicDetailsView;
import com.fitwise.view.user.ExternalLinkView;
import com.fitwise.view.user.SocialLinkView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@RequestMapping(value = "/fw/v2/instructor")
@RequiredArgsConstructor
public class InstructorProfileController {

    private final InstructorProfileService instructorProfileService;
    private final InstructorProgramService instructorProgramService;
    private final ProgramService programService;

    /**
     * Update instructor basic details
     *
     * @param basicDetails User basic details
     * @return Success response
     */
    @PutMapping(value = "/profile/basic")
    public ResponseModel updateProfileBasicDetails(@RequestBody BasicDetailsView basicDetails) {
        instructorProfileService.updateBasicDetails(basicDetails);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_BASIC_DETAILS_SAVED, null);
    }

    /**
     * Add instructor promo video
     *
     * @param promoUploadModel
     * @return
     */
    @PostMapping(value = "/profile/promo")
    public ResponseModel addInstructorPromo(@RequestBody PromoUploadModel promoUploadModel) throws IOException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_INSTRUCTOR_PROMOTION_ADDED, programService.uploadPromotion(promoUploadModel, KeyConstants.KEY_INSTRUCTOR));

    }

    /**
     * Edit instructor promo video
     *
     * @param promoUploadModel
     * @return
     */
    @PutMapping(value = "/profile/promo")
    public ResponseModel updateInstructorPromo(@RequestBody InstructorPromoUploadModel promoUploadModel) throws IOException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_INSTRUCTOR_PROMOTION_UPDATED, instructorProfileService.editInstructorPromo(promoUploadModel));
    }

    /**
     * Updated the upload completed for promo
     *
     * @param promotionId
     * @return
     * @throws IOException
     */
    @PutMapping(value = "/profile/promo/uploadcompleted")
    public ResponseModel updatePromoVideoUploaded(@RequestParam Long promotionId) {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SCS_VIDEO_UPLOAD, instructorProgramService.updateVideoUploaded(promotionId, InstructorConstant.VIDEO_TYPE_PROMO_VIDEO));
    }

    /**
     * Delete Instructor promo
     *
     * @param promotionId
     * @return
     * @throws IOException
     */
    @DeleteMapping(value = "/profile/promo")
    public ResponseModel deleteInstructorPromo(@RequestParam Long promotionId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        instructorProfileService.deleteInstructorPromo(promotionId);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROMOTION_DELETED, null);
    }

    /**
     * Get all social links for the user
     * @return ResponseModel with social links
     */
    @GetMapping(value = "/profile/link/social")
    public ResponseModel getProfileSocialLinks(){
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, instructorProfileService.getSocialLinks());
    }

    /**
     * Update user social links for the instructor
     * @param socialLinkViews Social link details
     * @return Success response
     */
    @PutMapping(value = "/profile/link/social")
    public ResponseModel addOrUpdateSocialLinks(@RequestBody List<SocialLinkView> socialLinkViews){
        instructorProfileService.addOrUpdateSocialLInks(socialLinkViews);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.SUCCESS_LINK_UPDATE, null);
    }

    /**
     * Get all the user external links
     * @return Response with external links
     */
    @GetMapping(value = "/profile/link/external")
    public ResponseModel getExternalLinks(){
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, instructorProfileService.getExternalLinks());
    }

    /**
     * Add or update user external links
     * @param externalLinkView External link details
     * @return Success response
     */
    @PutMapping(value = "/profile/link/external")
    public ResponseModel addOrUpdateExternalLinks(@RequestBody ExternalLinkView externalLinkView){
        instructorProfileService.addOrUpdateExternalLInk(externalLinkView);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.SUCCESS_LINK_UPDATE, null);
    }

    /**
     * Delete the external link
     * @param linkId Reference for external link deletion
     * @return Response
     */
    @DeleteMapping(value = "/profile/link/external")
    public ResponseModel deleteExternalLinks(@RequestParam Long linkId){
        instructorProfileService.deleteExternalLink(linkId);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.SUCCESS_CONTENT_DELETED, null);
    }
    
    @PutMapping(value = "/profile/location")
	public ResponseModel addOrUpdateProfileLocation(@RequestParam String location) {
		instructorProfileService.addOrUpdateProfileLocation(location);
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_LOCATION_DETAILS_SAVED, null);
	}
    
    @DeleteMapping(value = "/profile/location")
    public ResponseModel deleteProfileLocation(){
        instructorProfileService.deleteProfileLocation();
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.SUCCESS_CONTENT_DELETED, null);
    }
    
    @PutMapping(value = "/profile/gym")
	public ResponseModel addOrUpdateProfileGym(@RequestParam String gym) {
		instructorProfileService.addOrUpdateProfileGym(gym);
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_GYM_DETAILS_SAVED, null);
	}
    
    @DeleteMapping(value = "/profile/gym")
    public ResponseModel deleteProfileGym(){
        instructorProfileService.deleteProfileGym();
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.SUCCESS_CONTENT_DELETED, null);
    }
}
