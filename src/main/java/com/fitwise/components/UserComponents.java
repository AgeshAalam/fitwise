package com.fitwise.components;

import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fitwise.constants.Constants;
import com.fitwise.entity.User;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.UserRepository;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;

/**
 * The Class UserComponents.
 */
@Component
public class UserComponents {

    /** The user repository. */
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisService redisService;

    /**
     * Gets the user.
     *
     * @return the user
     * @throws ApplicationException the application exception
     */
    public User getUser() {
        LinkedTreeMap<String, Object> userDetails = (LinkedTreeMap)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = (String) userDetails.get("username");
        User user = userRepository.findByEmail(userName);
        if(user == null){
            throw new ApplicationException(Constants.UNAUTHORIZED, ValidationMessageConstants.MSG_TOKEN_INVALID, null);
        }
        return user;
    }

    public String getRole() {
        LinkedTreeMap<String, Object> userDetails = (LinkedTreeMap)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ArrayList<LinkedTreeMap<String, Object>> authorities = (ArrayList<LinkedTreeMap<String, Object>>) userDetails.get("authorities");
        LinkedTreeMap<String, Object> authority = authorities.get(0);
        String role = (String) authority.get("role");

        return role;
    }

    public User getAndValidateUser() {
        User user = null;
        try{
            LinkedTreeMap<String, Object> userDetails = (LinkedTreeMap)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userName = (String) userDetails.get("username");
            user = userRepository.findByEmail(userName);
            if(user == null){
                throw new ApplicationException(Constants.UNAUTHORIZED, ValidationMessageConstants.MSG_TOKEN_INVALID, null);
            }
        }catch (Exception e){
            //for guest user
        }

        return user;
    }

    public String getTimeZone(String token) {
        String timeZone = null;
        if (token != null) {
            timeZone = redisService.get(token + "_" + KeyConstants.KEY_TIME_ZONE);
        }
        return timeZone;
    }

}
