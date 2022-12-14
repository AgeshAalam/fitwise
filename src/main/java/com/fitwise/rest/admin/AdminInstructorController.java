package com.fitwise.rest.admin;

import com.fitwise.constants.Constants;
import com.fitwise.constants.ExportConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.service.admin.AdminInstructorService;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/admin/instructor")
@RequiredArgsConstructor
public class AdminInstructorController {

    private final AdminInstructorService adminInstructorService;
    private final FitwiseUtils fitwiseUtils;

    /**
     * Get Trial clients
     *
     * @param pageNo
     * @param pageSize
     * @param instructorId
     * @param searchName
     * @return
     * @throws ParseException
     */
    @GetMapping(value = "/client/all/trial")
    public ResponseModel getTrialClientsOfAnInstructor(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam Long instructorId, @RequestParam Optional<String> searchName) throws ParseException {
        return adminInstructorService.getTrialClientsOfAnInstructor(pageNo, pageSize, instructorId, searchName);
    }

    /**
     * Get Subscribed clients
     *
     * @param pageNo
     * @param pageSize
     * @param instructorId
     * @param searchName
     * @param type
     * @return
     */
    @GetMapping(value = "/client/all/subscribed")
    public ResponseModel getSubscribedClientsOfAnInstructor(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam Long instructorId, @RequestParam Optional<String> searchName,
                                                            @RequestParam Optional<String> type) {
        return adminInstructorService.getSubscribedClientsOfAnInstructor(pageNo, pageSize, instructorId, searchName, type);
    }

    /**
     * Get Lapsed clients
     *
     * @param pageNo
     * @param pageSize
     * @param instructorId
     * @param searchName
     * @param type
     * @return
     * @throws ParseException
     */
    @GetMapping(value = "/client/all/lapsed")
    public ResponseModel getLapsedClientsOfAnInstructor(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam Long instructorId, @RequestParam Optional<String> searchName,
                                                        @RequestParam Optional<String> type) throws ParseException {
        return adminInstructorService.getLapsedClientsOfAnInstructor(pageNo, pageSize, instructorId, searchName, type);
    }

    /**
     * Get all the instructors with filter and sort
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param search
     * @param blockStatus
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/all")
    public ResponseModel getInstructor(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> search, @RequestParam String blockStatus) {
        Map<String, Object> response = new HashMap<>();
        response.put(KeyConstants.KEY_INSTRUCTORS, adminInstructorService.getInstructor(pageNo, pageSize, sortOrder, sortBy, search, blockStatus));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
    }

    /**
     * Export Instructor details for Admin
     * @param sortOrder
     * @param sortBy
     * @param search
     * @param blockStatus
     * @return
     */
    @GetMapping(value = "all/export")
    public ResponseEntity<InputStreamResource> export(@RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> search, @RequestParam String blockStatus){
        ByteArrayInputStream byteArrayInputStream = adminInstructorService.export(sortOrder, sortBy, search, blockStatus);
        InputStreamResource file = new InputStreamResource(byteArrayInputStream);
        HttpHeaders httpHeaders = fitwiseUtils.getHttpDownloadHeaders(ExportConstants.FILE_NAME_INSTRUCTOR);
        return new ResponseEntity<>(file, httpHeaders, OK);
    }
}