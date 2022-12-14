package com.fitwise.rest.circuit;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.circuit.CircuitModel;
import com.fitwise.model.circuit.CircuitModelWithSetsAndReps;
import com.fitwise.service.circuit.CircuitService;
import com.fitwise.view.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Optional;

/*
 * Created by Vignesh G on 11/05/20
 */
@RestController
@CrossOrigin("*")
@Slf4j
@RequestMapping(value = "/v1/circuit")
public class CircuitController {

    @Autowired
    private CircuitService circuitService;

    /**
     * Validate the circuit name for duplicate
     * @param circuitName
     * @return
     */
    @GetMapping(value = "/validate/name")
    public ResponseModel validateName(@RequestParam String circuitName, @RequestParam Long workoutId){
        circuitService.validateName(circuitName, workoutId);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CIRCUIT_NAME_AVAILABLE, null);
    }

    @PostMapping("/create")
    public ResponseModel createCircuit(@RequestBody CircuitModel circuitModel) throws ApplicationException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_CIRCUIT_SAVED);
        response.setPayload(circuitService.createCircuit(circuitModel));
        return response;
    }

    @PostMapping("/createCircuitWithSetsAndReps")
    public ResponseModel createCircuitWithSetsAndReps(@RequestBody CircuitModelWithSetsAndReps circuitModel) throws ApplicationException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_CIRCUIT_SAVED);
        response.setPayload(circuitService.createCircuitWithSetsAndReps(circuitModel));
        return response;
    }

    @GetMapping(value = "/allInstructorCircuits")
    public ResponseModel getAllInstructorCircuits(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> searchName) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(circuitService.getAllInstructorCircuits(pageNo, pageSize, sortOrder, sortBy, searchName));
        return response;
    }

    @GetMapping(value = "/circuitRepeatCounts")
    public ResponseModel getCircuitRepeatCounts() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(circuitService.getCircuitRepeatCounts());
        return response;
    }

    @GetMapping
    public ResponseModel getCircuit(@RequestParam final Long circuitId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(circuitService.getCircuit(circuitId));
        return response;
    }

    @GetMapping(value = "/getCircuitWithSetsAndReps")
    public ResponseModel getCircuitWithSetsAndReps(@RequestParam final Long circuitId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        Long startTime = new Date().getTime();
        log.info("Get Circuit start : " + startTime);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(circuitService.getCircuitWithSetsAndReps(circuitId));
        log.info("Get Circuit End : " + (new Date().getTime() - startTime));
        return response;
    }

    @DeleteMapping(value = "/deleteCircuit")
    public ResponseModel deleteCircuit(@RequestParam final Long circuitId) throws ApplicationException {
        circuitService.deleteCircuit(circuitId);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CIRCUIT_DELETED, null);
    }

    @DeleteMapping(value = "/removeExercise")
    public ResponseModel removeExercise(@RequestParam Long exerciseScheduleId) {
        circuitService.removeExercise(exerciseScheduleId);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_EXERCISE_REMOVED_FROM_CIRCUIT, null);
    }

    @GetMapping(value = "/setsCount")
    public ResponseModel getSetsCount() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(circuitService.getSetsCount());
        return response;
    }

    /**
     * Remove the voice over from circuit
     * @param exerciseScheduleId
     * @return
     */
    @DeleteMapping(value = "/removeVoiceOver")
    public ResponseModel removeVoiceOver(@RequestParam Long exerciseScheduleId, @RequestParam Long circuitAndVoiceOverMappingId, @RequestParam Boolean isAudioCircuit) {
        circuitService.removeVoiceOver(exerciseScheduleId, circuitAndVoiceOverMappingId, isAudioCircuit);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_VOICE_OVER_REMOVED_FROM_CIRCUIT, null);
    }

}
