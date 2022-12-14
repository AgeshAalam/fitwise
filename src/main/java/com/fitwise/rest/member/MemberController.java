package com.fitwise.rest.member;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.model.member.ExpertiseAndGoalsModel;
import com.fitwise.model.member.MemberInstructorFilterModel;
import com.fitwise.service.UserService;
import com.fitwise.service.member.MemberService;
import com.fitwise.view.GoalsRequestView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.UserRequestView;
import com.fitwise.view.member.MemberFilterView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The Class MemberController.
 */
@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/member")
public class MemberController {

    /** The user service. */
    @Autowired
    private UserService userService;

    @Autowired
    private MemberService memberService;

    /**
     * Get Instructor filter
     * @return
     * @throws ApplicationException
     */
    @GetMapping(value = "/instructorFilter")
    public ResponseModel getInstructorFilter() throws ApplicationException {
        Map<String, List<MemberFilterView>> filterRespMap = new HashMap<>();
        filterRespMap.put(KeyConstants.KEY_FILTER_DATA, memberService.getInstructorFilter());
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(filterRespMap);
        return responseModel;
    }

    /**
     * Instructor Profiles for member app
     *
     * @param pageNo
     * @param pageSize
     * @param instructorListModel
     * @return Instructor Profile
     * @throws ApplicationException the application exception
     */
    @PutMapping(value = "/getInstructors")
    public ResponseModel getInstructors(@RequestParam int pageNo, @RequestParam int pageSize, @RequestBody MemberInstructorFilterModel instructorListModel, @RequestParam Optional<String> search) throws ApplicationException {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, memberService.getInstructors(pageNo, pageSize, instructorListModel, search));
    }

    /**
     * @return Returns User profile response model
     */
    @GetMapping("/getUserProfile")
    public ResponseModel getUserProfile() {
        return userService.getUserProfile();
    }

    @PostMapping("/updateUserProfile")
    public ResponseModel updateUserProfile(@RequestBody UserRequestView userView) {
        return userService.updateUserProfile(userView);

    }

    /**
     * API to update ExpertiseAndGoals of a member
     * @param expertiseAndGoalsModel
     * @return
     * @throws ApplicationException
     */
    @PutMapping("/updateProgramExpertiseAndGoals")
    public ResponseModel updateExpertiseAndGoals(@RequestBody ExpertiseAndGoalsModel expertiseAndGoalsModel) throws ApplicationException {
        memberService.updateExpertiseAndGoals(expertiseAndGoalsModel);
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(null);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_USER_PROFILE_SAVED);
        return responseModel;
    }

    @GetMapping("/getInstructorProfileForMember")
    public ResponseModel getInstructorProfileForMember(@RequestParam Long userId, @RequestParam Optional<Integer> pageNo, @RequestParam Optional<Integer> pageSize) throws ApplicationException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return memberService.getInstructorProfileForMember(userId, pageNo, pageSize);
    }

    @PutMapping("/getGoalsForProgramTypeAndExpertiseLevel")
    public ResponseModel getGoals(@RequestBody GoalsRequestView goalsRequestView) throws ApplicationException {
        Map<String, Object> goalMap = new HashMap<>();
        goalMap.put(KeyConstants.KEY_PROGRAM_GOALS, userService.getGoals(goalsRequestView));

        ResponseModel responseModel = new ResponseModel();
        responseModel.setMessage(MessageConstants.MSG_GOALS_RETRIEVED);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setPayload(goalMap);
        return responseModel;
    }

    @PostMapping(value = "/updateUserWorkoutStatus")
    public ResponseModel updateUserWorkoutStatus(@RequestParam Long programId, @RequestParam Long workoutId){
        return userService.updateUserWorkoutStatus(programId , workoutId);
    }

}
