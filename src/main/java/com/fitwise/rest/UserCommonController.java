package com.fitwise.rest;

import com.ecwid.maleorang.MailchimpException;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.delete.service.DeleteService;
import com.fitwise.entity.social.SocialMedia;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.NotifyEmailService;
import com.fitwise.service.StaticContentService;
import com.fitwise.service.UserService;
import com.fitwise.service.fcm.UserFcmTokenService;
import com.fitwise.view.NotifyMeView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.fcm.UserFcmTokenView;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * The Class UserCommonController.
 */
@RestController
@RequestMapping(value = "/v1/user/common")
@RequiredArgsConstructor
public class UserCommonController {

	/** The user fcm token service. */
	@Autowired
	private UserFcmTokenService userFcmTokenService;

	/** To notify email service. */
	@Autowired
	private NotifyEmailService notifyEmailService;

	@Autowired
	private DeleteService deleteService;

	@Autowired
	private UserService userService;

	private final StaticContentService staticContentService;

	/**
	 * Save user fcm token.
	 *
	 * @param userFcmToken the user fcm token
	 * @return the response model
	 */
	@PostMapping(value = "/updateFcmToken")
	public ResponseModel saveUserFcmToken(@RequestBody final UserFcmTokenView userFcmToken) {
		userFcmTokenService.saveUserFcmToken(userFcmToken);
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_FCM_TOKEN_SAVED, null);
	}

	/**
	 * Delete user's fcm token
	 * @param userFcmToken
	 * @return
	 */
	@DeleteMapping(value = "/removeFcmToken")
	public ResponseModel removeFcmToken(@RequestBody final UserFcmTokenView userFcmToken) {
		userFcmTokenService.removeFcmToken(userFcmToken);
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_FCM_TOKEN_REMOVED, null);
	}

	/**
	 * Notify me.
	 *
	 * @param notifyMeView the email
	 * @return the response model
	 * @throws MailchimpException 
	 * @throws IOException 
	 */
	@PostMapping(value = "/notifyMe")
	public ResponseModel notifyMe(@RequestBody final NotifyMeView notifyMeView, HttpServletRequest request) throws IOException, MailchimpException {
		return notifyEmailService.saveNotifyEmail(notifyMeView,request);
	}

	/**
	 * get all the delete reasons
	 */
	@GetMapping(value = "/getDeleteReasons")
	public ResponseModel getDeleteReasons(){
		return  new ResponseModel(Constants.SUCCESS_STATUS , MessageConstants.MSG_DELETE_REASONS_RETRIEVED, deleteService.getDeleteReasons());
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
	 * To get Single video details
	 * @param vimeoId
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws KeyManagementException
	 */
	@GetMapping(value = "/videoDetails")
	public ResponseModel getVideoDetails(@RequestParam Long vimeoId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		return userService.getVideoDetails(vimeoId);
	}

	/**
	 * Get all supported social media
	 * @return response with social media list
	 */
	@GetMapping(value = "/social/all")
	public ResponseModel getAllSocialMedia(){
		List<SocialMedia> socialMediaList = staticContentService.getSocialMedia();
		if(socialMediaList.isEmpty()){
			throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, null, null);
		}
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, socialMediaList);
	}

}