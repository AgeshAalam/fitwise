package com.fitwise.rest.v2;

import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.UserService;
import com.fitwise.view.LoginUserView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.V2LoginResponseView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/fw/v2/user")
public class UserRestV2Controller {

    @Autowired
    private UserService userService;


    /**
     * User login
     *
     * @param userView the user view
     * @return the response model
     * @throws ApplicationException the application exception
     */
    @PostMapping(value = "/login")
    public ResponseModel userLogin(@RequestBody LoginUserView userView) throws ApplicationException {

        V2LoginResponseView loginResponseView = userService.v2Login(userView.getEmail(), userView.getPassword(), userView.getUserRole());
        ResponseModel responseModel = new ResponseModel();

        if (loginResponseView.isNewRolePrompt()) {
            responseModel.setMessage(MessageConstants.PROMPT_TO_ADD_NEW_ROLE);
        } else {
            responseModel.setMessage(MessageConstants.LOGIN_SUCCESS);
        }
        responseModel.setPayload(userService.v2Login(userView.getEmail(), userView.getPassword(), userView.getUserRole()));

        responseModel.setStatus(2000);
        return responseModel;
    }

}
