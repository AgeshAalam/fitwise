package com.fitwise.service;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.*;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.ProgramExpertiseGoalsMappingRepository;
import com.fitwise.repository.ProgramExpertiseMappingRepository;
import com.fitwise.repository.UserProgramGoalsMappingRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.request.UserProgramGoalsId;
import com.fitwise.response.UserGoalsResponse;
import com.fitwise.view.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The Class UserProgramGoalsMappingService.
 */
@Service
@Transactional
@Slf4j
public class UserProgramGoalsMappingService {

    /** The user program goals mapping repository. */
    @Autowired
    UserProgramGoalsMappingRepository userProgramGoalsMappingRepository;

    /** The user repository. */
    @Autowired
    UserRepository userRepository;

    /** The program expertise goals mapping repository. */
    @Autowired
    ProgramExpertiseGoalsMappingRepository programExpertiseGoalsMappingRepository;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private ProgramExpertiseMappingRepository programExpertiseMappingRepository;

	/**
	 * Save user and goals.
	 *
	 * @param userProgramGoalsId the user program goals id
	 * @throws Exception the exception
     * @return
	 */
    public ResponseModel saveUserAndGoals(UserProgramGoalsId userProgramGoalsId) throws ApplicationException {
        log.info("Save user goals starts");
        long start = System.currentTimeMillis();
        User user = userComponents.getUser();

        List<Long> programExpertiseGoalsMappingIdList = userProgramGoalsId.getProgramExpertiseGoalsMappingId();

        if (programExpertiseGoalsMappingIdList.isEmpty() || programExpertiseGoalsMappingIdList.size() == 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_MAPPING_ID_EMPTY, null);
        }

        long countOfRows = programExpertiseGoalsMappingRepository.countByProgramExpertiseGoalsMappingIdIn(programExpertiseGoalsMappingIdList);
        if (programExpertiseGoalsMappingIdList.size() != countOfRows) {
            throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.NOT_EXIST_MAPPING_ID, null);
        }
        log.info("Basic validations and collecting goal mapping ids : Time taken in millis : " + (System.currentTimeMillis() - start));

        long profilingStart = System.currentTimeMillis();
        //To check whether multiple expertise levels selected for a single program type
        Map<String, String> programLevelMap = new HashMap<>();
        List<ProgramExpertiseGoalsMapping> programExpertiseGoalsMappingList = programExpertiseGoalsMappingRepository.findByProgramExpertiseGoalsMappingIdIn(programExpertiseGoalsMappingIdList);
        for (ProgramExpertiseGoalsMapping programExpertiseGoalsMapping : programExpertiseGoalsMappingList) {
            ProgramExpertiseMapping programExpertiseMapping = programExpertiseGoalsMapping.getProgramExpertiseMapping();
            String programTypeName = programExpertiseMapping.getProgramType().getProgramTypeName();
            String expertiseLevel = programExpertiseMapping.getExpertiseLevel().getExpertiseLevel();
            if (!programLevelMap.containsKey(programTypeName)) {
                programLevelMap.put(programTypeName, expertiseLevel);
            } else {
                if (!programLevelMap.get(programTypeName).equals(expertiseLevel)) {
                    throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_MULTIPLE_EXPERTISE_LEVELS_NOT_ALLOWED, MessageConstants.ERROR);
                }
            }
        }
        log.info("Validation for multiple expertise levels on same program type : Time taken in millis : " + (System.currentTimeMillis() - profilingStart));

        profilingStart = System.currentTimeMillis();
        boolean doesUserHaveGoals = userProgramGoalsMappingRepository.existsByUserUserId(userProgramGoalsId.getUserId());
        log.info("Query to check user has goals : Time taken in millis : " + (System.currentTimeMillis() - profilingStart));

        String responseMsg = null;

        List<ProgramExpertiseGoalsMapping> toAddProgramExpertiseGoalsMapping = new ArrayList<>();
        if (doesUserHaveGoals) {
            profilingStart = System.currentTimeMillis();
            List<UserProgramGoalsMapping> existingUserProgramGoalsMappingList = userProgramGoalsMappingRepository.findByUserUserId(user.getUserId());

            List<UserProgramGoalsMapping> removeUserProgramGoalsMapping = new ArrayList<>();
            List<ProgramExpertiseGoalsMapping> retainProgramExpertiseGoalsMapping = new ArrayList<>();
            for (UserProgramGoalsMapping userProgramGoalsMapping : existingUserProgramGoalsMappingList) {
                if (!programExpertiseGoalsMappingIdList.contains(userProgramGoalsMapping.getProgramExpertiseGoalsMapping().getProgramExpertiseGoalsMappingId())) {
                    /*Getting list of UserProgramGoalsMapping to remove
                     * These ProgramExpertiseGoalsMapping are not present in user input */
                    removeUserProgramGoalsMapping.add(userProgramGoalsMapping);
                } else if (programExpertiseGoalsMappingIdList.contains(userProgramGoalsMapping.getProgramExpertiseGoalsMapping().getProgramExpertiseGoalsMappingId())) {
                    /*Getting list of ProgramExpertiseGoalsMapping to be retained for the user
                     * These ProgramExpertiseGoalsMapping are already present in DB*/
                    retainProgramExpertiseGoalsMapping.add(userProgramGoalsMapping.getProgramExpertiseGoalsMapping());
                }
            }

            List<Long> retainProgramExpertiseGoalsMappingIdList = retainProgramExpertiseGoalsMapping.stream()
                    .map(programExpertiseGoalsMapping -> programExpertiseGoalsMapping.getProgramExpertiseGoalsMappingId())
                    .collect(Collectors.toList());

            /*Getting list of ProgramExpertiseGoalsMapping to be added for the user
             * These ProgramExpertiseGoalsMapping are not present in DB*/
            for (ProgramExpertiseGoalsMapping programExpertiseGoalsMapping : programExpertiseGoalsMappingList) {
                if (!retainProgramExpertiseGoalsMappingIdList.contains(programExpertiseGoalsMapping.getProgramExpertiseGoalsMappingId())) {
                    toAddProgramExpertiseGoalsMapping.add(programExpertiseGoalsMapping);
                }
            }

            /*deleting irrelevant existing goals*/
            if (!removeUserProgramGoalsMapping.isEmpty()) {
                userProgramGoalsMappingRepository.deleteAll(removeUserProgramGoalsMapping);
            }

            log.info("DB update for deleting existing goals : Time taken in millis : " + (System.currentTimeMillis() - profilingStart));

            responseMsg = MessageConstants.MSG_USER_GOALS_UPDATE;
        } else {
            toAddProgramExpertiseGoalsMapping = programExpertiseGoalsMappingList;
            responseMsg = MessageConstants.MSG_USER_GOALS_SAVED;
        }

        profilingStart = System.currentTimeMillis();
        List<UserProgramGoalsMapping> userProgramGoalsMappings = new ArrayList<>();
        for (ProgramExpertiseGoalsMapping programExpertiseGoalsMapping : toAddProgramExpertiseGoalsMapping) {
            UserProgramGoalsMapping userProgramGoalsMapping = new UserProgramGoalsMapping();
            userProgramGoalsMapping.setUser(user);
            userProgramGoalsMapping.setProgramExpertiseGoalsMapping(programExpertiseGoalsMapping);
            userProgramGoalsMappings.add(userProgramGoalsMapping);
        }
        if (!userProgramGoalsMappings.isEmpty()) {
            userProgramGoalsMappingRepository.saveAll(userProgramGoalsMappings);
        }
        log.info("DB update for saving goals : Time taken in millis : " + (System.currentTimeMillis() - profilingStart));

        log.info("Save user goals : Total Time taken in millis : " + (System.currentTimeMillis() - start));
        log.info("Save user goals ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, responseMsg, null);
    }

    /**
     * Gets the user goals.
     *
     * @param userId the user id
     * @return the user goals
     */
    public List<UserGoalsResponse> getUserGoals(long userId) {
        List<UserGoalsResponse> userGoalsResponses = new ArrayList<>();
        Map<Long, UserGoalsResponse> userGoalsResponseMap =  new HashMap<>();
        List<UserProgramGoalsMapping> userProgramGoals = userProgramGoalsMappingRepository.findByUserUserId(userId);
        for (UserProgramGoalsMapping userProgramGoal : userProgramGoals) {
            Long programExpertiseGoalsMappingId = userProgramGoal.getProgramExpertiseGoalsMapping().getProgramExpertiseGoalsMappingId();
            ProgramExpertiseGoalsMapping programExpertiseGoalsMapping = programExpertiseGoalsMappingRepository.findByProgramExpertiseGoalsMappingId(programExpertiseGoalsMappingId);
            Long mappingId = programExpertiseGoalsMapping.getProgramExpertiseMapping().getProgramExpertiseMappingId();
            if (userGoalsResponseMap.containsKey(mappingId)) {
                UserGoalsResponse userGoalsResponse = userGoalsResponseMap.get(mappingId);
                List<ProgramGoals> programGoalsList = userGoalsResponse.getProgramGoals();
                programGoalsList.add(programExpertiseGoalsMapping.getProgramGoals());
                userGoalsResponse.setProgramGoals(programGoalsList);
                userGoalsResponseMap.put(mappingId , userGoalsResponse);
            } else {
                UserGoalsResponse userGoalsResponse = new UserGoalsResponse();
                userGoalsResponse.setProgramTypeId(programExpertiseGoalsMapping.getProgramExpertiseMapping().getProgramType().getProgramTypeId());
                userGoalsResponse.setProgramType(programExpertiseGoalsMapping.getProgramExpertiseMapping().getProgramType().getProgramTypeName());
                userGoalsResponse.setExpertiseLevelId(programExpertiseGoalsMapping.getProgramExpertiseMapping().getExpertiseLevel().getExpertiseLevelId());
                userGoalsResponse.setExpertiseLevel(programExpertiseGoalsMapping.getProgramExpertiseMapping().getExpertiseLevel().getExpertiseLevel());
                List<ProgramGoals> programGoals = new ArrayList<>();
                programGoals.add(programExpertiseGoalsMapping.getProgramGoals());
                userGoalsResponse.setProgramGoals(programGoals);
                userGoalsResponseMap.put(mappingId, userGoalsResponse);
            }
        }
        for (long key : userGoalsResponseMap.keySet()) {
            userGoalsResponses.add(userGoalsResponseMap.get(key));
        }
        return userGoalsResponses;
    }

}
