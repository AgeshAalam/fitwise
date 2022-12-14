package com.fitwise.rest.member;

import com.fitwise.service.UserService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/user")
public class UserController {

    /**
     * The user service.
     */
    @Autowired
    private UserService userService;


    @PostMapping("/uploadProfileImage")
    public ResponseModel uploadProfileImage(@RequestParam Long imageId) {
        return  userService.uploadProfileImage(imageId);
    }

    @PostMapping("/uploadCoverImage")
    public ResponseModel uploadCoverImage(@RequestParam Long imageId)  {
        return  userService.uploadCoverImage(imageId);
    }
}
