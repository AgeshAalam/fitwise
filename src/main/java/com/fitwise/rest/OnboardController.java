package com.fitwise.rest;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.request.ProgramTypeAndExpertiseLevel;
import com.fitwise.request.ProgramTypeAndExpertiseLevelRequest;
import com.fitwise.request.ProgramTypesRequest;
import com.fitwise.request.UserDeviceDetails;
import com.fitwise.request.UserProgramGoalsId;
import com.fitwise.response.ProgramExpertiseGoalsMappingResponse;
import com.fitwise.response.ProgramExpertiseMappingResponse;
import com.fitwise.service.OnboardDataService;
import com.fitwise.service.ProgramExpertiseGoalsMappingService;
import com.fitwise.service.ProgramExpertiseMappingService;
import com.fitwise.service.ProgramSupportDataService;
import com.fitwise.service.ProgramTypeService;
import com.fitwise.service.UserProgramGoalsMappingService;
import com.fitwise.service.freeproduct.FreeAccessService;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class OnboardController.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/onboard")
@Slf4j
public class OnboardController {

    /**
     * The program type service.
     */
    @Autowired
    private ProgramTypeService programTypeService;

    /**
     * The program expertise goals mapping service.
     */
    @Autowired
    ProgramExpertiseGoalsMappingService programExpertiseGoalsMappingService;

    /**
     * The program expertise mapping service.
     */
    @Autowired
    ProgramExpertiseMappingService programExpertiseMappingService;

    /**
     * The user program goals mapping service.
     */
    @Autowired
    UserProgramGoalsMappingService userProgramGoalsMappingService;

    /**
     * The program support data service.
     */
    @Autowired
    private ProgramSupportDataService programSupportDataService;

    private final OnboardDataService onboardDataService;
    private final FreeAccessService freeAccessService;

    /**
     * Gets the program types.
     *
     * @return the program types
     */
    @GetMapping("/getProgramTypes")
    public ResponseModel getProgramTypes() {
        log.info("Get program types starts.");
        long apiStartTimeMillis = new Date().getTime();
        List<ProgramTypes> programTypesList = programTypeService.programTypesList();
        Map<String, Object> programMap = new HashMap<>();
        programMap.put(KeyConstants.KEY_PROGRAM_TYPES, programTypesList);
        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(programMap);
        responseModel.setMessage(MessageConstants.MSG_PROGRAM_TYPES_RETRIEVED);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get program types ends.");
        return responseModel;
    }

    /**
     * Gets the expertise levels based on programType.
     *
     * @param programTypesRequest the program types request
     * @return the expertise levels
     * @Param programTypeId
     */
    @PostMapping(value = "/getExpertiseLevelsOnProgramType")
    public ResponseModel getAllExpertiseLevelsByProgramType(@RequestBody ProgramTypesRequest programTypesRequest) {
        long[] programTypes = programTypesRequest.getListOfProgramTypes();
        List<ProgramExpertiseMappingResponse> expertiseLevels = programExpertiseMappingService.getAllExpertiseLevelsByProgramType(programTypes);

        Map<String, Object> expertiseLevelMap = new HashMap<>();
        expertiseLevelMap.put(KeyConstants.KEY_EXPERTISE_LEVELS, expertiseLevels);

        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(expertiseLevelMap);
        responseModel.setMessage(MessageConstants.MSG_EXPERTISE_LEVELS_RETRIEVED);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        return responseModel;
    }

    /**
     * Gets the programGoals based on ProgramType and ExpertiseLevel.
     *
     * @param selectedTypeAndLevel the selected type and level
     * @return the ProgramGoals
     * @Param programTypeId and expertiseId
     */
    @PostMapping(value = "/getGoalsOnTypeAndLevel")
    public ResponseModel getAllProgramExpertiseGoalsMapping(@RequestBody ProgramTypeAndExpertiseLevelRequest selectedTypeAndLevel) {
        List<ProgramTypeAndExpertiseLevel> typeAndLevelList = selectedTypeAndLevel.getSelectedTypeAndLevel();
        List<ProgramExpertiseGoalsMappingResponse> goalsList = programExpertiseGoalsMappingService.getAllGoalsOnTypeAndLevel(typeAndLevelList);

        Map<String, Object> programGoalsMap = new HashMap<>();
        programGoalsMap.put(KeyConstants.KEY_PROGRAM_GOALS, goalsList);

        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(programGoalsMap);
        responseModel.setMessage(MessageConstants.MSG_GOALS_RETRIEVED);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        return responseModel;
    }

    /**
     * save user selected goals.
     *
     * @param userProgramGoalsId the user program goals id
     * @return User selected goals saved
     * @Param userId and list of mappingIds
     */
    @PostMapping(value = "/saveUserAndGoals")
    public ResponseModel saveUserAndGoals(@RequestBody UserProgramGoalsId userProgramGoalsId) {
        return userProgramGoalsMappingService.saveUserAndGoals(userProgramGoalsId);
    }

    /**
     * Gets the durations.
     *
     * @return the durations
     */
    @GetMapping(value = "/durations")
    public ResponseModel getDurations() {
        Map<String, Object> response = new HashMap<>();
        response.put(KeyConstants.KEY_DURATIONS, programSupportDataService.getAllDuration());
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
    }

    /**
     * Gets the program plan data.
     *
     * @return the program plan data
     */
    @GetMapping(value = "/programPlanMetadata")
    public ResponseModel getProgramPlanData() {
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, onboardDataService.getProgramPlanData());
    }

    @GetMapping(value = "/equipments")
    public ResponseModel getEquipments() {
        Map<String, Object> response = new HashMap<>();
        response.put(KeyConstants.KEY_EQUIPMENTS, onboardDataService.getEquipments());
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
    }

    /**
     * Save user and goals.
     * Returns the platform specific details
     * Since we are internally auditing user's activeness using this api, we are getting user device details and role
     *
     * @param deviceDetails the device details
     * @return the response model
     */
    @PostMapping(value = "/getAppConfigData")
    public ResponseModel getAppConfigData(@RequestBody UserDeviceDetails deviceDetails) {
        return onboardDataService.getAppConfigData(deviceDetails);
    }

    /**
     * Get transition video vimeo id
     * @return ResponseModel
     */
    @GetMapping(value = "/transitionvideos")
    public ResponseModel getInfoVideos(){
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, onboardDataService.getInfoVideos());
    }

    /**
     * Gets the free access details.
     *
     * @return the free access details
     */
    @GetMapping(value = "/freeaccess")
    public ResponseModel getFreeAccessDetails(){
        return freeAccessService.getUserFreeAccessProgramAndPackagesCounts();
    }
}
