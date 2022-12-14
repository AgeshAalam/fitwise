package com.fitwise.rest.v2;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.service.circuit.CircuitService;
import com.fitwise.service.v2.circuit.CircuitV2Service;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Rest for v2.0 Circuits
 */
@RestController
@RequestMapping(value = "/fw/v2/circuit")
@RequiredArgsConstructor
public class V2CircuitController {

    private final CircuitService circuitService;
    private final CircuitV2Service circuitV2Service;

    /**
     * Get circuit details with corresponding to given circuitId
     * @param circuitId
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    @GetMapping(value = "/detail")
    public @ResponseBody ResponseModel circuit(@RequestParam final Long circuitId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, circuitV2Service.getCircuit(circuitId));
    }

    /**
     * Get the circuit with vimeo video urls and standards for exercise videos
     * @param circuitId
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    @GetMapping(value = "/getCircuit")
    public @ResponseBody ResponseModel getCircuit(@RequestParam final Long circuitId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(circuitService.getCircuitWithSetsAndReps(circuitId));
        return response;
    }
}
