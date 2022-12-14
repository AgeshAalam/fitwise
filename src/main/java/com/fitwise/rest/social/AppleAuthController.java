package com.fitwise.rest.social;

import com.fitwise.service.social.AppleAuthService;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.socialLogin.AppleLoginRequestView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/social")
public class AppleAuthController {

    @Autowired
    AppleAuthService appleAuthService;

    @PostMapping("/validateAppleLogin")
    public ResponseModel validateAppleIdAndSignIn(@RequestBody AppleLoginRequestView authorizationCode) throws Exception {
        return appleAuthService.validateAppleSignIn(authorizationCode);
    }

}
