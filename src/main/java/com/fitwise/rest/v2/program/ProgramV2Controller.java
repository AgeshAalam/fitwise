package com.fitwise.rest.v2.program;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.service.v2.program.ProgramQuickTourService;
import com.fitwise.service.v2.program.ProgramV2Service;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

/**
 * The Class ProgramController.
 */
@RestController
@RequestMapping(value = "/fw/v2/program")
@RequiredArgsConstructor
public class ProgramV2Controller {

    private final ProgramQuickTourService programQuickTourService;
    private final ProgramV2Service programV2Service;

    /**
     * Get sTour videos List
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping(value = "/tourvideos")
    public ResponseModel getTourVideosList(@RequestParam int pageNo, @RequestParam int pageSize) {
        return programQuickTourService.getQuickTourVideos(pageNo, pageSize);
    }

    /**
     * Get Program L2 for instructor
     * @param programId
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws ParseException
     */
    @GetMapping
    public ResponseModel getProgram(@RequestParam final Long programId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, programV2Service.getProgram(programId));
    }

    /**
     * Get sample program L2 details
     * @param programId
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws ParseException
     */
    @GetMapping(value = "/sample")
    public ResponseModel getSampleProgram(@RequestParam Long programId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, programV2Service.getSampleProgram(programId));
    }
}