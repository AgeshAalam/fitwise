package com.fitwise.rest;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.service.TokenService;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping(value = "/v1")
public class LogoutController {

    private final TokenService tokenService;

    /**
     * Logout and clear the session context and invalidate session
     * @param request
     * @return
     */
    @GetMapping(value = "/logout")
    public @ResponseBody ResponseModel logout(final HttpServletRequest request){
        tokenService.logoutUser(request);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_TOKEN_REVOKED, null);
    }

}
