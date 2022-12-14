package com.fitwise.rest.workout;

import com.fitwise.constants.Constants;
import com.fitwise.constants.*;
import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.service.ExerciseService;
import com.fitwise.service.circuit.CircuitService;
import com.fitwise.view.ResponseModel;
import com.fitwise.workout.model.WorkoutModel;
import com.fitwise.workout.model.WorkoutResponseView;
import com.fitwise.workout.service.WorkoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/workout")
public class WorkoutController {

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    CircuitService circuitService;

    /**
     * Workout create or update
     *
     * @param model
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws ApplicationException
     */
    @PostMapping("/create")
    public ResponseModel createWorkout(@RequestBody WorkoutModel model) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ApplicationException {
        return workoutService.createWorkouts(model);
    }

    /**
     * Get the workout for the user
     *
     * @param workoutId
     * @return
     */
    @GetMapping
    public ResponseModel getWorkout(@RequestParam final Long workoutId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, workoutService.getWorkout(workoutId));
    }

    @GetMapping(value = "/all")
    public ResponseModel getWorkouts(Optional<String> searchName) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ResponseModel responseModel = new ResponseModel();
        List<WorkoutResponseView> workoutResponseViewList = workoutService.getWorkouts(null, searchName);
        responseModel.setPayload(workoutResponseViewList);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        return responseModel;
    }

    @DeleteMapping(value = "/deleteThumbnail")
    public ResponseModel deleteThumbnail(@RequestParam final Long imageId, @RequestParam final Long workoutId) throws ApplicationException {
        return workoutService.deleteThumbnail(imageId, workoutId);
    }

    @DeleteMapping(value = "/removeCircuit")
    public ResponseModel removeCircuit(@RequestParam Long circuitScheduleId) {
        circuitService.removeCircuitFromWorkout(circuitScheduleId);
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_CIRCUIT_REMOVED);

        return responseModel;
    }


    /**
     *
     * @param pageNo
     * @param pageSize
     * @param searchName
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    @GetMapping(value = "getAllInstructorWorkouts")
    public ResponseModel getAllInstructorWorkouts(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> searchName) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return workoutService.getAllInstructorWorkouts(pageNo, pageSize, sortOrder, sortBy, searchName);
    }

    @DeleteMapping(value = "/deleteWorkout")
    public ResponseModel deleteWorkout(@RequestParam final Long workoutId) throws ApplicationException {
        workoutService.deleteWorkout(workoutId);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_WORKOUT_DELETED, null);
    }

}
