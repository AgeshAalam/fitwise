package com.fitwise.rest.social;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.social.GoogleAuthService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.auth.GoogleAuthenticationView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;



/**
 * The Class GoogleAuthController.
 */
@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/social")
public class GoogleAuthController {

	/** The auth service. */
	@Autowired
	GoogleAuthService authService;

	/**
	 * Post google sign in.
	 *
	 * @param auth the auth
	 * @return the response model
	 * @throws MalformedURLException the malformed URL exception
	 * @throws ProtocolException the protocol exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ApplicationException the application exception
	 */
	@PostMapping("/postGoogleSignIn")
	public ResponseModel postGoogleSignIn(@RequestBody GoogleAuthenticationView auth)
			throws IOException {
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_USER_ADDED, authService.connectionGoogle(auth));
		
	}
}
