package com.fitwise.rest.social;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.social.FbAuthService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.auth.FacebookAuthenticationView;
import com.fitwise.view.socialLogin.FBPhoneNumberOnlyRequestView;
import com.fitwise.view.socialLogin.FbUserProfileIDWithEmailRequestView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/social")
public class FbAuthController {

    @Autowired
    private FbAuthService fbAuthService;

    private String tokenValidationUrl = "";

    /**
     * Used to login into platform using a facebook account with email
     *
     * @param auth
     * @return
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     * @throws ApplicationException
     */
    @PostMapping("/postFacebookSignIn")
    public ResponseModel postFacebookSignIn(@RequestBody FacebookAuthenticationView auth)
            throws MalformedURLException, ProtocolException, IOException, ApplicationException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_ADDED, fbAuthService.connectionFacebook(auth));
    }


    /**
     * If a facebook account has only phone number and not email, in that case email will be get manually from
     * the user and saved against user profile id
     *
     * @return
     */
    @PostMapping("/validateFbUserProfileId")
    public ResponseModel validateUserProfileId(@RequestBody FBPhoneNumberOnlyRequestView fbPhoneNumberOnlyRequestView) throws IOException {
        return fbAuthService.validateFbUserProfileId(fbPhoneNumberOnlyRequestView);
    }


    /**
     * If a facebook account doesn't have any email under its account and only phone number, email will be got from user
     * Checking whether the email is validated using OTP
     * Checking whether any local fitwise accounts are available already for entered email. If yes, throw conflict and asking using whether we can merge account
     *
     * @param fbUserProfileIDWithEmailRequestView
     * @return
     * @throws IOException
     */
    @PostMapping("/validateAndLoginUsingUserEnteredEmail")
    public ResponseModel validateUserEnteredEmail(@RequestBody FbUserProfileIDWithEmailRequestView fbUserProfileIDWithEmailRequestView) throws IOException {
        return fbAuthService.validateAndLoginUsingUserEnteredEmail(fbUserProfileIDWithEmailRequestView);
    }
}
