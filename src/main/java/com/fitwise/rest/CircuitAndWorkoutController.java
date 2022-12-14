package com.fitwise.rest;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Workouts;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.circuit.CircuitModel;
import com.fitwise.repository.WorkoutRepository;
import com.fitwise.service.circuit.CircuitService;
import com.fitwise.view.ResponseModel;
import com.fitwise.workout.model.WorkoutModel;
import com.fitwise.workout.model.WorkoutResponseView;
import com.fitwise.workout.service.WorkoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/fw/v2")
public class CircuitAndWorkoutController {

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private CircuitService circuitService;

    @Autowired
    private WorkoutRepository workoutRepository;

    @PostMapping("/workout/create")
    public ResponseModel createWorkout(@RequestBody WorkoutModel model) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ApplicationException {
        return workoutService.createWorkouts(model);
    }

    @GetMapping("/workout")
    public ResponseModel getWorkout(@RequestParam final Long workoutId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, workoutService.getWorkout(workoutId));
    }

    @GetMapping(value = "/workout/all")
    public ResponseModel getWorkouts(Optional<String> searchName) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ResponseModel responseModel = new ResponseModel();
        List<WorkoutResponseView> workoutResponseViewList = workoutService.getWorkouts(null, searchName);
        responseModel.setPayload(workoutResponseViewList);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        return responseModel;
    }

    @GetMapping("/admin/workout")
    public WorkoutResponseView getWorkoutForAdmin(final Long workoutId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        Workouts workout = workoutRepository.findByWorkoutId(workoutId);
        if(workout == null){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_NOT_FOUND, null);
        }
        return workoutService.constructWorkoutResponseView(workout);
    }

    @PostMapping("/circuit/create")
    public ResponseModel createCircuit(@RequestBody CircuitModel circuitModel) throws ApplicationException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_CIRCUIT_SAVED);
        response.setPayload(circuitService.createCircuit(circuitModel));
        return response;
    }

    @GetMapping("/circuit")
    public ResponseModel getCircuit(@RequestParam final Long circuitId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(circuitService.getCircuit(circuitId));
        return response;
    }
}
