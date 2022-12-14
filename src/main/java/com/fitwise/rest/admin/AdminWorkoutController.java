package com.fitwise.rest.admin;

import com.fitwise.admin.service.AdminWorkoutService;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Controller used to process and collect workout data for admin
 */
@RestController
@RequestMapping(value = "/v1/admin/workout")
public class AdminWorkoutController {

    @Autowired
    private AdminWorkoutService adminWorkoutService;

    /**
     * Get Workout for the given workoutID
     * @param workoutId
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    @GetMapping
    public ResponseModel getWorkout(@RequestParam final Long workoutId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, adminWorkoutService.getWorkout(workoutId));
    }
}
