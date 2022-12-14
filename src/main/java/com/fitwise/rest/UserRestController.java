package com.fitwise.rest;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.UserProfile;
import com.fitwise.exception.ApplicationException;
import com.fitwise.response.UserGoalsResponse;
import com.fitwise.service.UserForgotPasswordService;
import com.fitwise.service.UserProgramGoalsMappingService;
import com.fitwise.service.UserService;
import com.fitwise.view.AddNewRoleView;
import com.fitwise.view.LoginResponseView;
import com.fitwise.view.LoginUserView;
import com.fitwise.view.OtpView;
import com.fitwise.view.PostPhoneNumberView;
import com.fitwise.view.RegistrationUserView;
import com.fitwise.view.ResetPasswordRequestView;
import com.fitwise.view.ResetPasswordview;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.ValidateEmailView;
import com.fitwise.view.instructor.AddNewRoleViewWithOtp;
import com.intuit.ipp.exception.FMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The Class UserRestController.
 */
@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/user")
public class UserRestController {

    /**
     * The user service.
     */
    @Autowired
    private UserService userService;


    /**
     * The user program goals mapping service.
     */
    @Autowired
    private UserProgramGoalsMappingService userProgramGoalsMappingService;

    /**
     * The user forgot password service.
     */
    @Autowired
    private UserForgotPasswordService userForgotPasswordService;

    /**
     * Gets the forgot password.
     *
     * @param email the email
     * @return the forgot password
     * @throws ApplicationException         the application exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @GetMapping(value = "/forgotPassword")
    public ResponseModel getForgotPassword(@RequestParam final String email, @RequestParam Optional<String> role) throws ApplicationException, UnsupportedEncodingException {
        userForgotPasswordService.generateForgotPassword(email, role);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_RESET_CRED_SENT, null);
    }

    /**
     * Updatet password.
     *
     * @param resetPasswordview the reset passwordview
     * @return the response model
     * @throws ApplicationException         the application exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    @PostMapping(value = "/forgotPassword")
    public ResponseModel updatePassword(@RequestBody final ResetPasswordview resetPasswordview) throws UnsupportedEncodingException {
        if (userForgotPasswordService.resetUserPassword(resetPasswordview)) {
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CRED_RESET_DONE, null);
        } else {
            return new ResponseModel(Constants.ERROR_STATUS, MessageConstants.MSG_NO_TOKEN, null);
        }
    }

    /**
     * User register.
     *
     * @param userView the user view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseModel userRegister(@RequestBody RegistrationUserView userView) throws FMSException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_ADDED,
                userService.userSave(userView));
    }

    @PostMapping(value = "/validateEmail")
    public ResponseModel userEmailValidate(@RequestBody ValidateEmailView emailView) throws ApplicationException {
        return userService.userEmailValidate(emailView);
    }

    @PostMapping(value = "/validateEmailForSocialLogin")
    public ResponseModel userEmailValidateForSocialLogin(@RequestBody ValidateEmailView emailView) throws ApplicationException {
        return userService.userEmailValidateForSocialLogin(emailView);
    }

    @PostMapping(value = "/authenticateAndAddNewRole")
    public ResponseModel validatePasswordAndAddNewRole(@RequestBody AddNewRoleView addNewRoleView) throws ApplicationException {
        return userService.addNewRoleToUser(addNewRoleView);
    }

    @GetMapping(value = "/generateOtp")
    public ResponseModel generateOtp(@RequestParam final String email) {
        return userService.generateOtp(email);
    }

    @PostMapping(value = "/savePasswordAndNewRole")
    public ResponseModel savePasswordAndAddNewRole(@RequestBody AddNewRoleViewWithOtp newRoleView) throws ApplicationException {
        return userService.addNewRoleAndPassword(newRoleView);
    }

    /**
     * User login
     *
     * @param userView the user view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    @PostMapping(value = "/login")
    public ResponseModel userLogin(@RequestBody LoginUserView userView) throws ApplicationException {

        LoginResponseView loginResponseView = userService.login(userView.getEmail(), userView.getPassword(), userView.getUserRole());
        ResponseModel responseModel = new ResponseModel();

        if (loginResponseView.isNewRolePrompt()) {
            responseModel.setMessage(MessageConstants.PROMPT_TO_ADD_NEW_ROLE);
        } else {
            responseModel.setMessage(MessageConstants.LOGIN_SUCCESS);
        }
        responseModel.setPayload(userService.login(userView.getEmail(), userView.getPassword(), userView.getUserRole()));

        responseModel.setStatus(2000);
        return responseModel;
    }

    /**
     * Validate otp.
     *
     * @param otpView the otp view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    @PostMapping(value = "/validateOtp")
    public ResponseModel validateOtp(@RequestBody OtpView otpView) throws ApplicationException {
        ResponseModel responseModel = new ResponseModel();
        userService.validateOtp(otpView);
        responseModel.setPayload(null);
        responseModel.setMessage(ValidationMessageConstants.MSG_OTP_VALID);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        return responseModel;
    }

    /**
     * selected user goals
     *
     * @param userId
     * @return selected user goals
     */

    @GetMapping(value = "/userGoals/{userId}")
    public ResponseModel getSelectedUserGoals(@PathVariable("userId") long userId) {
        List<UserGoalsResponse> userGoalsResponse = userProgramGoalsMappingService.getUserGoals(userId);
        Map<String, Object> programGoalsMap = new HashMap<String, Object>();
        programGoalsMap.put(KeyConstants.KEY_SELECTED_USER_GOALS, userGoalsResponse);
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(programGoalsMap);
        responseModel.setMessage(MessageConstants.MSG_SELECTED_USER_GOALS);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        return responseModel;
    }

    /**
     * Instructor Profile
     *
     * @param userId
     * @return Instructor Profile
     */
    @GetMapping(value = "/getInstructors/")
    public ResponseModel getInstructors(@RequestParam int userId) throws ApplicationException {
        List<UserProfile> instructorProfileResponse = (List<UserProfile>) userService.getInstructorProfile(userId);
        Map<String, Object> response = new HashMap<String, Object>();
        response.put(KeyConstants.KEY_USER_DATA, instructorProfileResponse);
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(response);
        responseModel.setMessage(MessageConstants.MSG_USER_PROFILE_FETCHED);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        return responseModel;
    }

    /**
     * To reset password
     *
     * @param resetPasswordRequestView
     * @return
     * @throws ApplicationException
     * @throws UnsupportedEncodingException
     */
    @PostMapping(value = "/resetPassword")
    public ResponseModel resetPassword(@RequestBody final ResetPasswordRequestView resetPasswordRequestView) throws ApplicationException, UnsupportedEncodingException {
        return userService.resetPassword(resetPasswordRequestView);
    }


    /**
     * Method used to add phone number to the user's profile
     *
     * @param phoneNumberView
     * @return
     */
    @PostMapping("/postUserPhoneNumber")
    public ResponseModel postUserPhoneNumber(@RequestBody PostPhoneNumberView phoneNumberView) {
        return userService.addPhoneNumberToUserProfile(phoneNumberView);
    }


}
