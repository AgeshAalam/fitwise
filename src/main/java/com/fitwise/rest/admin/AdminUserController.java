package com.fitwise.rest.admin;

import com.fitwise.admin.service.AdminService;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.UserService;
import com.fitwise.service.admin.AdminUserService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/admin")
public class AdminUserController {

    /**
     * The user service.
     */
    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @Autowired
    AdminUserService adminUserService;


    /**
     * Gets the member.
     *
     * @param pageSize the page size
     * @param pageNo   the page count
     * @return the members
     * @throws ApplicationException the application exception
     * @Param sortOrder for asc or dsc
     * @Param sortBy the memers
     * @Param searchName for searching single member
     */

    @GetMapping(value = "/getMember")
    public ResponseModel getMember(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> search, @RequestParam String blockStatus) throws Exception {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put(KeyConstants.KEY_MEMBER, userService.getMember(pageNo, pageSize, sortOrder, sortBy, search, blockStatus));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
    }

    /**
     * Gets the instructor.
     *
     * @param pageSize the page size
     * @param pageNo   the page count
     * @return the instructors
     * @throws ApplicationException the application exception
     * @Param sortOrder for asc or dsc
     * @Param sortBy the instructors
     * @Param searchName for searching single instructor
     */

    @GetMapping(value = "/getInstructor")
    public ResponseModel getInstructor(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> search, @RequestParam String blockStatus) throws Exception {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put(KeyConstants.KEY_INSTRUCTORS, userService.getInstructor(pageNo, pageSize, sortOrder, sortBy, search, blockStatus));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
    }

    /**
     * Gets the programs.
     *
     * @param pageSize the page size
     * @param pageNo   the page count
     * @return the programs
     * @throws ApplicationException the application exception
     * @Param sortOrder for asc or dsc
     * @Param sortBy the instructors
     * @Param searchName is searching for programs
     */

    @GetMapping(value = "/getProgram")
    public ResponseModel getProgram(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam Optional<Long> programTypeId, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> search, @RequestParam String blockStatus) throws Exception {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put(KeyConstants.KEY_PROGRAM, userService.getProgram(pageNo, pageSize, programTypeId, sortOrder, sortBy, search, blockStatus));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
    }


    /**
     * Gets the flagged viseos.
     *
     * @param pageSize the page size
     * @param pageNo   the page count
     * @return the instructors
     * @throws ApplicationException the application exception
     * @Param sortOrder for asc or dsc
     * @Param sortBy the instructors
     * @Param searchName is searching for flagged videos
     */
    @GetMapping(value = "/getFlaggedVideos")
    public ResponseModel getFlaggedVideos(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> search, @RequestParam Optional<String> blockStatus) throws ApplicationException {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put(KeyConstants.KEY_FLAGGED_VIDEOS, adminService.getFlaggedVideos(pageNo, pageSize, sortOrder, sortBy, search, blockStatus));
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
    }

    @GetMapping(value = "/getFlaggedVideoDetails")
    public ResponseModel getFlaggedVideoDetails(@RequestParam final Long exerciseId) throws ApplicationException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(adminService.getFlaggedVideoDetails(exerciseId));

        return response;
    }

    @GetMapping(value = "/flaggedReasonDetails")
    public ResponseModel getFlaggedReasonDetails(@RequestParam Long exerciseId, @RequestParam Long reasonId, @RequestParam int pageNo, @RequestParam int pageSize) throws ApplicationException {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(adminService.getFlaggedReasonDetails(exerciseId, reasonId, pageNo, pageSize));

        return response;
    }

    /**
     * @param exerciseId
     * @return
     * @throws ApplicationException
     */
    @PostMapping(value = "/blockFlaggedVideo")
    public ResponseModel blockFlaggedVideo(@RequestParam final Long exerciseId) throws ApplicationException {
        String responseMsg = adminService.blockFlaggedVideo(exerciseId);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setPayload(null);
        response.setMessage(responseMsg);
        return response;
    }

    /**
     * API to unblock a blocked flagged video
     * @param exerciseId
     * @return
     * @throws ApplicationException
     */
    @PostMapping(value = "/unblockFlaggedVideo")
    public ResponseModel unblockFlaggedVideo(@RequestParam final Long exerciseId) throws ApplicationException {
        adminService.unblockFlaggedVideo(exerciseId);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setPayload(null);
        response.setMessage(MessageConstants.MSG_EXERCISE_FLAG_STATUS_UNBLOCKED);
        return response;
    }

    @PostMapping(value = "/ignoreFlaggedVideo")
    public ResponseModel ignoreFlaggedVideo(@RequestParam final Long exerciseId) throws ApplicationException {
        adminService.ignoreFlaggedVideo(exerciseId);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setPayload(null);
        response.setMessage(MessageConstants.MSG_EXERCISE_FLAG_STATUS_IGNORED);
        return response;
    }

    /**
     * Gets the program types count.
     *
     *  * @return ResponseModel
     */

    @GetMapping(value = "getProgramTypesCount")
    public ResponseModel getProgramTypesCount(@RequestParam String blockStatus){
       return userService.getProgramTypeCount(blockStatus);
    }


    /**
     * Used to get the users enrollment based on role for year and month
     *
     * @param role      User's role in Fitwise
     * @param date      Should be in format MM/dd/yyyy
     * @param isForYear Boolean to check whether the enrollment count is requested for year or month
     * @return
     * @throws ApplicationException
     * @throws ParseException
     */

    /**
     * @DateTimeFormat(pattern="MM/dd/yyyy") it is used for validating wrong dates
     * wrong dates  "Cannot parse "11/32/2020": Value 32 for dayOfMonth must be in the range [1,30]"
     * "Cannot parse "72/20/2020": Value 72 for monthOfYear must be in the range [1,12]"
     * */
    @GetMapping("/getUsersEnrollmentCount")
    public ResponseModel getUsersEnrollmentCount(@RequestParam String role, @RequestParam @DateTimeFormat(pattern= StringConstants.PATTERN_DATE) @Valid Date date, @RequestParam boolean isForYear) throws ApplicationException, ParseException {
        if (isForYear) {
            return adminService.getUsersEnrollmentCountForAYear(role, date);
        }
        return adminService.getUsersEnrollmentCountForAMonth(role, date);
    }

    /**
     * To get instructor details
     *
     * @param userId
     * @param userId
     * @param pageNo
     * @param pageSize
     * @return
     * @throws ApplicationException
     * @GetMapping(value="/getInstructorDetails") public ResponseModel getInstructorDetails(@RequestParam int userId) throws ApplicationException, ParseException {
     * return adminService.getInstructorDetails(userId);
     * <p>
     * <p>
     * }
     * <p>
     * /**
     * To get Instructor published programs
     */
    @GetMapping(value = "/getInstructorPublishedPrograms")
    public ResponseModel getPublishedPrograms(@RequestParam final Long userId, @RequestParam final int pageNo, @RequestParam final int pageSize) throws ApplicationException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, adminUserService.getProgramsOfInstructor(userId, pageNo, pageSize));
    }

    /**
     * Used to get ProgramAnalytics for year and month
     *
     * @param startDate Should be in format MM/dd/yyyy
     * @param endDate   Should be in format MM/dd/yyyy
     * @param isForYear Boolean to check whether the programAnalytics count is requested for year or month
     * @return
     * @throws ApplicationException
     * @throws ParseException
     */

    /**
     * @DateTimeFormat(pattern="MM/dd/yyyy") it is used for validating wrong dates
     * wrong dates  "Cannot parse "11/32/2020": Value 32 for dayOfMonth must be in the range [1,30]"
     * "Cannot parse "72/20/2020": Value 72 for monthOfYear must be in the range [1,12]"
     * */
    @GetMapping("/getProgramAnalytics")
    public ResponseModel getProgramAnalytics(@RequestParam @DateTimeFormat(pattern= StringConstants.PATTERN_DATE) @Valid Date startDate, @RequestParam @DateTimeFormat(pattern= StringConstants.PATTERN_DATE) @Valid Optional<Date> endDate,
                                             @RequestParam boolean isForYear) throws ParseException, ApplicationException {
        if (isForYear) {
            return adminService.getProgramAnalyticsForYear(startDate, endDate.get());
        }
        return adminService.getProgramAnalyticsForMonth(startDate);
    }

    /**
     * Used to get Specialization for year and month
     *
     * @param startDate     Should be in format MM/dd/yyyy
     * @param endDate      Should be in format MM/dd/yyyy
     * @param isForYear Boolean to check whether the Specialization is requested for year or month
     * @return
     * @throws ApplicationException
     * @throws ParseException
     */

    /**
    * @DateTimeFormat(pattern="MM/dd/yyyy") it is used for validating wrong dates
     * wrong dates  "Cannot parse "11/32/2020": Value 32 for dayOfMonth must be in the range [1,30]"
     * "Cannot parse "72/20/2020": Value 72 for monthOfYear must be in the range [1,12]"
    * */
    @GetMapping("/getSpecialization")
    public ResponseModel getSpecialization(@RequestParam @DateTimeFormat(pattern= StringConstants.PATTERN_DATE) @Valid Date startDate, @RequestParam @DateTimeFormat(pattern= StringConstants.PATTERN_DATE) @Valid Optional<Date> endDate, @RequestParam boolean isForYear ) throws ParseException, ApplicationException
    {
        if(isForYear){
            return adminService.getSpecializationForYear(startDate , endDate.get());
        }
        return adminService.getSpecializationForMonth(startDate , startDate);
    }

    /**
     * To get member profile details and subscription details
     * @param userId
     * @return
     * @throws ApplicationException
     * @throws ParseException
     */
    @GetMapping(value = "/getMemberDetails")
    public ResponseModel getMemberDetails(@RequestParam Long userId) throws ApplicationException, ParseException {
        return adminService.getMemberDetails(userId);
    }

    /**
     * API for MemberProgramHistory under admin -> member tab
     * @param userId
     * @param pageNo
     * @param pageSize
     * @return
     * @throws ApplicationException
     * @throws ParseException
     */
    @GetMapping(value = "/memberProgramHistory")
    public ResponseModel getMemberProgramHistory(@RequestParam Long userId, @RequestParam int pageNo, @RequestParam int pageSize) {
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(adminService.getMemberProgramHistory(userId, pageNo, pageSize));

        return response;
    }


    /**
     * To get member profile details and subscription details
     * @param instructorId
     * @return
     * @throws ApplicationException
     * @throws ParseException
     */
    @GetMapping(value = "/getInstructorDetails")
    public ResponseModel getInstructorDetails(@RequestParam Long instructorId) throws ApplicationException, ParseException {
        return adminService.getInstructorDetails(instructorId);
    }

    @GetMapping(value = "/all")
    public ResponseModel getAllAdminUsers(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy,
                                          @RequestParam Optional<String> search){
        return  adminUserService.getAdminUsers(pageNo, pageSize, sortOrder, sortBy, search);
    }
}
