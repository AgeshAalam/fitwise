package com.fitwise.service.v2.instructor;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.VideoUploadStatus;
import com.fitwise.constants.VimeoConstants;
import com.fitwise.entity.Gender;
import com.fitwise.entity.Images;
import com.fitwise.entity.Promotions;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.VideoManagement;
import com.fitwise.entity.social.SocialMedia;
import com.fitwise.entity.user.UserLinkExternal;
import com.fitwise.entity.user.UserLinkSocial;
import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.model.UploadModel;
import com.fitwise.exercise.model.VimeoModel;
import com.fitwise.exercise.service.VimeoService;
import com.fitwise.model.instructor.VideoVersioningModel;
import com.fitwise.program.model.InstructorPromoUploadModel;
import com.fitwise.program.model.PromoUploadResponseModel;
import com.fitwise.repository.GenderRepository;
import com.fitwise.repository.ImageRepository;
import com.fitwise.repository.PromotionRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.VideoManagementRepo;
import com.fitwise.repository.social.SocialMediaRepository;
import com.fitwise.repository.user.UserLinkExternalRepository;
import com.fitwise.repository.user.UserLinkSocialRepository;
import com.fitwise.service.instructor.InstructorProgramService;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.instructor.BasicDetailsView;
import com.fitwise.view.user.ExternalLinkView;
import com.fitwise.view.user.SocialLinkView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorProfileService {

    private final UserComponents userComponents;
    private final UserProfileRepository userProfileRepository;
    private final GenderRepository genderRepository;
    private final FitwiseQboEntityService fitwiseQboEntityService;
    private final PromotionRepository promotionRepository;
    private final ImageRepository imageRepository;
    private final VimeoService vimeoService;
    private final InstructorProgramService instructorProgramService;
    private final VideoManagementRepo videoManagementRepo;
    private final UserLinkSocialRepository userLinkSocialRepository;
    private final SocialMediaRepository socialMediaRepository;
    private final UserLinkExternalRepository userLinkExternalRepository;
    private final UserLinkService userLinkService;

    /**
     * Update instructor basic details
     * @param basicDetailsView User basic details
     */
    public void updateBasicDetails(BasicDetailsView basicDetailsView) {
        User user = userComponents.getUser();
        UserProfile userProfile = userProfileRepository.findByUser(user);
        RequestParamValidator.allowOnlyAlphabets(basicDetailsView.getFirstName(), ValidationMessageConstants.MSG_FIRST_NAME_ERROR);
        RequestParamValidator.stringLengthValidation(basicDetailsView.getFirstName(), null, 50L, ValidationMessageConstants.MSG_FIRST_NAME_ERROR);
        RequestParamValidator.allowOnlyAlphabets(basicDetailsView.getLastName(), ValidationMessageConstants.MSG_LAST_NAME_ERROR);
        RequestParamValidator.stringLengthValidation(basicDetailsView.getLastName(), null, 50L, ValidationMessageConstants.MSG_LAST_NAME_ERROR);
        userProfile.setFirstName(basicDetailsView.getFirstName());
        userProfile.setLastName(basicDetailsView.getLastName());
        if (!StringUtils.isEmpty(basicDetailsView.getCountryCode())) {
            userProfile.setCountryCode(basicDetailsView.getCountryCode());
        }
        if (!StringUtils.isEmpty(basicDetailsView.getContactNumber())) {
            if (StringUtils.isEmpty(basicDetailsView.getIsdCountryCode())) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_COUNTRY_CODE_IS_NULL, null);
            }
            if (StringUtils.isEmpty(basicDetailsView.getIsdCode())) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ISD_CODE_IS_NULL, null);
            }
            if (ValidationUtils.validatePhonenumber(basicDetailsView.getIsdCountryCode(), basicDetailsView.getContactNumber())) {
                userProfile.setContactNumber(basicDetailsView.getContactNumber());
                userProfile.setIsdCode(basicDetailsView.getIsdCode());
            } else {
                throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_PHONENUMBER_INVALID, null);
            }
        } else {
            userProfile.setContactNumber(null);
            userProfile.setIsdCode(null);
        }
        if(basicDetailsView.getShortBio() != null){
            RequestParamValidator.emptyString(basicDetailsView.getShortBio(), MessageConstants.MSG_ERR_SHORT_BIO);
            RequestParamValidator.stringLengthValidation(basicDetailsView.getShortBio(), 10L, 50L, MessageConstants.MSG_ERR_SHORT_BIO);
        }
        userProfile.setShortBio(basicDetailsView.getShortBio());
        userProfile.setBiography(basicDetailsView.getAbout());
        Gender gender;
        if (basicDetailsView.getGenderId() == null) {
            gender = genderRepository.findByGenderType(DBConstants.PREFER_NOT_TO_SAY);
        } else {
            gender = genderRepository.findByGenderId(basicDetailsView.getGenderId());
        }
        if (gender == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_GENDER_ERROR, null);
        }
        userProfile.setGender(gender);
        userProfile.setUser(user);
        userProfileRepository.save(userProfile);
        fitwiseQboEntityService.createOrUpdateQboUser(user, SecurityFilterConstants.ROLE_INSTRUCTOR);
        log.info("Update basic details completed.");
    }


    /**
     * Edit Instructor promo
     * @param promoUploadModel
     * @return
     */
    public PromoUploadResponseModel editInstructorPromo(InstructorPromoUploadModel promoUploadModel) {
        if (promoUploadModel.getPromotionId() == null || promoUploadModel.getPromotionId() == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_ID_NULL, null);
        }
        Promotions promotions = promotionRepository.findByPromotionId(promoUploadModel.getPromotionId());
        if (promotions == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_NOT_FOUND, null);
        }
        User user = userComponents.getUser();
        UserProfile userProfile = userProfileRepository.findByUserAndPromotion(user, promotions);
        if (userProfile == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_NOT_BELONGS_INSTRUCTOR, null);
        }
        String tempStatus;
        PromoUploadResponseModel promoUploadResponseModel = new PromoUploadResponseModel();
        if (promoUploadModel.getFileName() != null) {
            VideoVersioningModel videoVersioningModel = new VideoVersioningModel();
            videoVersioningModel.setFileName(promoUploadModel.getFileName());
            videoVersioningModel.setFileSize(Long.toString(promoUploadModel.getFileSize()));
            videoVersioningModel.setVersioningEntityId(promoUploadModel.getPromotionId());
            videoVersioningModel = instructorProgramService.createVideoVersion(videoVersioningModel, InstructorConstant.VIDEO_TYPE_PROMO_VIDEO, false);
            if (promotions.getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.UPLOAD) || promotions.getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.INPROGRESS)) {
                tempStatus = VideoUploadStatus.UPLOAD;
            } else {
                tempStatus = VideoUploadStatus.REUPLOAD;
            }
            /**
             * If the video processing was failed first time, marking it as upload status
             * If the video processing was failed more than one time, marking it as re-upload status
             */
            if (promotions.getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_PROCESSING_FAILED)) {
                tempStatus = VideoUploadStatus.UPLOAD;
            } else if (promotions.getVideoManagement().getUploadStatus().equalsIgnoreCase(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED)) {
                tempStatus = VideoUploadStatus.REUPLOAD;
            }
            promotions.getVideoManagement().setUploadStatus(tempStatus);
            videoManagementRepo.save(promotions.getVideoManagement());
            VimeoModel vimeoModel = new VimeoModel();
            doConstructVimeoModel(promoUploadModel, vimeoModel);
            vimeoModel.getUpload().setUpload_link(videoVersioningModel.getUploadLink());
            promoUploadResponseModel.setVimeoModel(vimeoModel);
        }
        if (promoUploadModel.getImageId() != null) {
            VideoManagement videoManagement = promotions.getVideoManagement();
            Images image = imageRepository.findByImageId(promoUploadModel.getImageId());
            if (image != null) {
                videoManagement.setThumbnail(image);
                videoManagementRepo.save(videoManagement);
            }
        }
        promoUploadResponseModel.setPromotions(promotions);
        return promoUploadResponseModel;
    }

    private void doConstructVimeoModel(InstructorPromoUploadModel promoUploadModel, VimeoModel vimeoModel) {
        vimeoModel.setName(promoUploadModel.getFileName());
        UploadModel upload = new UploadModel();
        upload.setSize(promoUploadModel.getFileSize());
        upload.setApproach(VimeoConstants.APPROACH);
        vimeoModel.setUpload(upload);
    }

    public void deleteInstructorPromo(Long promotionId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        User user = userComponents.getUser();
        deleteInstructorPromo(promotionId, user);
    }

    /**
     * Delete Instructor promo
     * @param promotionId
     */
    public void deleteInstructorPromo(Long promotionId, User user) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if (promotionId == 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_ID_NULL, MessageConstants.ERROR);
        }

        Promotions promotions = promotionRepository.findByPromotionId(promotionId);
        if (promotions == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_NOT_FOUND, MessageConstants.ERROR);
        }

        UserProfile userProfile = userProfileRepository.findByUserAndPromotion(user, promotions);
        if (userProfile == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROMOTION_NOT_BELONGS_INSTRUCTOR, null);
        }

        userProfile.setPromotion(null);
        userProfileRepository.save(userProfile);
        if (promotions.getVideoManagement() != null) {
            vimeoService.deleteVimeoVideo(promotions.getVideoManagement().getUrl());
        }
        promotionRepository.delete(promotions);
    }

    /**
     * Get all user social links
     * @return Social link view list
     */
    public List<SocialLinkView> getSocialLinks() {
        User user = userComponents.getUser();
        List<SocialLinkView> socialLinkViews = userLinkService.getSocialLinks(user);
        if(socialLinkViews.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, null, null);
        }
        return socialLinkViews;
    }

    /**
     * Add or update social links for the logged-in user
     * @param socialLinkViews Social link input
     */
    @Transactional
    public void addOrUpdateSocialLInks(List<SocialLinkView> socialLinkViews) {
        User user = userComponents.getUser();
        UserProfile userProfile = userProfileRepository.findByUser(user);
        List<UserLinkSocial> userLinkSocials = userLinkSocialRepository.findByUserProfile(userProfile);
        List<SocialMedia> socialMediaList = socialMediaRepository.findAll();
        Map<Long, SocialMedia> socialMediaMap = new HashMap<>();
        for (SocialMedia socialMedia : socialMediaList) {
            socialMediaMap.put(socialMedia.getSocialMediaId(), socialMedia);
        }
        Map<Long, UserLinkSocial> userLinkSocialHashMap = new HashMap<>();
        for (UserLinkSocial userLinkSocial : userLinkSocials) {
            userLinkSocialHashMap.put(userLinkSocial.getSocialMedia().getSocialMediaId(), userLinkSocial);
        }
        for(SocialLinkView socialLinkView : socialLinkViews){
            if(!StringUtils.isEmpty(socialLinkView.getLink()) && !ValidationUtils.isUrlValid(socialLinkView.getLink())){
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.ERROR_LINK_INVALID_CONTENT + " " + socialLinkView.getLink(), null);
            }
            if(socialMediaMap.get(socialLinkView.getSocialMediaId()) == null){
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.ERROR_SOCIAL_MEDIA_INVALID, null);
            } else {
                UserLinkSocial userLinkSocial;
                if (userLinkSocialHashMap.get(socialLinkView.getSocialMediaId()) == null) {
                    userLinkSocial = new UserLinkSocial();
                    userLinkSocial.setSocialMedia(socialMediaMap.get(socialLinkView.getSocialMediaId()));
                    userLinkSocial.setUserProfile(userProfile);
                } else {
                    userLinkSocial = userLinkSocialHashMap.get(socialLinkView.getSocialMediaId());
                }
                userLinkSocial.setLink(socialLinkView.getLink());
                userLinkSocialRepository.save(userLinkSocial);
            }
        }
    }

    /**
     * Add or update user external links
     * @param externalLinkView External link details
     */
    public void addOrUpdateExternalLInk(ExternalLinkView externalLinkView) {
        User user = userComponents.getUser();
        RequestParamValidator.emptyString(externalLinkView.getName(), MessageConstants.ERROR_LINK_NAME_INVALID);
        RequestParamValidator.emptyString(externalLinkView.getLink(), MessageConstants.ERROR_LINK_INVALID);
        RequestParamValidator.stringLengthValidation(externalLinkView.getName(), 5L, 30L, MessageConstants.ERROR_LINK_NAME_INVALID);
        RequestParamValidator.stringLengthValidation(externalLinkView.getLink(), null, 500L, MessageConstants.ERROR_LINK_INVALID);
        if(!ValidationUtils.isUrlValid(externalLinkView.getLink())){
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.ERROR_LINK_INVALID_CONTENT, null);
        }
        UserLinkExternal userLinkExternal;
        if (externalLinkView.getLinkId() != null) {
            userLinkExternal = userLinkExternalRepository.findByLinkId(externalLinkView.getLinkId());
            if (userLinkExternal == null) {
                throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.ERROR_INVALID_LINK_ID, null);
            }
        } else {
            userLinkExternal = new UserLinkExternal();
            UserProfile userProfile = userProfileRepository.findByUser(user);
            userLinkExternal.setUserProfile(userProfile);
        }
        Images image = imageRepository.findByImageId(externalLinkView.getImageId());
        if (image == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.ERROR_INVALID_IMAGE_ID, null);
        }
        userLinkExternal.setImage(image);
        userLinkExternal.setName(externalLinkView.getName());
        userLinkExternal.setLink(externalLinkView.getLink());
        userLinkExternalRepository.save(userLinkExternal);
    }

    /**
     * Get all logged-in user external links
     * @return External link view list
     */
    public List<ExternalLinkView> getExternalLinks() {
        User user = userComponents.getUser();
        List<ExternalLinkView> externalLinkViews = userLinkService.getExternalLinks(user);
        if(externalLinkViews.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, null, null);
        }
        return externalLinkViews;
    }

    /**
     * Delete the given link id for the logged in user
     * @param linkId Link id for deletion
     */
    @Transactional
    public void deleteExternalLink(final Long linkId) {
        User user = userComponents.getUser();
        UserLinkExternal userLinkExternal = userLinkExternalRepository.findByUserProfileUserAndLinkId(user, linkId);
        if (userLinkExternal == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.ERROR_INVALID_LINK_ID, null);
        }
        userLinkExternalRepository.delete(userLinkExternal);
    }
    
	public void addOrUpdateProfileLocation(final String location) {
		User user = userComponents.getUser();
		RequestParamValidator.emptyString(location, ValidationMessageConstants.MSG_LOCATION_NULL);
		UserProfile userProfile = userProfileRepository.findByUser(user);
		userProfile.setLocation(location);
		userProfileRepository.save(userProfile);
	}
	
	 @Transactional
		public void deleteProfileLocation() {
			User user = userComponents.getUser();
			UserProfile userProfile = userProfileRepository.findByUser(user);
			if (userProfile == null) {
				throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.ERROR_USER_NOT_FOUND, null);
			}
			userProfile.setLocation(null);
			userProfileRepository.save(userProfile);
		}
	 
	 public void addOrUpdateProfileGym(final String gym) {
			User user = userComponents.getUser();
			RequestParamValidator.emptyString(gym, ValidationMessageConstants.MSG_GYM_NULL);
			UserProfile userProfile = userProfileRepository.findByUser(user);
			userProfile.setGym(gym);
			userProfileRepository.save(userProfile);
		}
		
		 @Transactional
			public void deleteProfileGym() {
				User user = userComponents.getUser();
				UserProfile userProfile = userProfileRepository.findByUser(user);
				if (userProfile == null) {
					throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.ERROR_USER_NOT_FOUND, null);
				}
				userProfile.setGym(null);
				userProfileRepository.save(userProfile);
			}
}