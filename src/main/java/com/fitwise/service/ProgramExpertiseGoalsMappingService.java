package com.fitwise.service;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.entity.ExpertiseLevels;
import com.fitwise.entity.ProgramExpertiseGoalsMapping;
import com.fitwise.entity.ProgramExpertiseMapping;
import com.fitwise.entity.ProgramGoals;
import com.fitwise.entity.ProgramSubTypes;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.ExpertiseLevelRepository;
import com.fitwise.repository.ProgramExpertiseGoalsMappingRepository;
import com.fitwise.repository.ProgramExpertiseMappingRepository;
import com.fitwise.repository.ProgramSubTypeRepository;
import com.fitwise.repository.ProgramTypeRepository;
import com.fitwise.request.ProgramTypeAndExpertiseLevel;
import com.fitwise.response.ProgramExpertiseGoalsMappingResponse;
import com.fitwise.response.ProgramGoalsView;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ProgramExpertiseGoalsMappingService {

    @Autowired
    ProgramExpertiseGoalsMappingRepository programExpertiseGoalsMappingRepository;

    @Autowired
    ProgramExpertiseMappingRepository ProgramExpertiseMappingRepository;

    @Autowired
    ExpertiseLevelRepository expertiseLevelRepository;

    @Autowired
    ProgramTypeRepository programTypeRepository;
    
    @Autowired
    ProgramSubTypeRepository programSubTypeRepository;

    public List<ProgramExpertiseGoalsMappingResponse> getAllGoalsOnTypeAndLevel(List<ProgramTypeAndExpertiseLevel> programExpertiseIds) {
        log.info("getAllGoalsOnTypeAndLevel starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        List<ProgramExpertiseGoalsMappingResponse> listOfProgramExpertiseGoalsResponses = new ArrayList<>();
        try {
            long profilingStartTimeMillis = System.currentTimeMillis();
            for (ProgramTypeAndExpertiseLevel programExpertiseId : programExpertiseIds) {
                List<ProgramGoalsView> programGoalsViews = new ArrayList<>();

                List<ProgramExpertiseMapping> programExpertiseMappings;
                ExpertiseLevels expertiseLevel = expertiseLevelRepository.findByExpertiseLevelId(programExpertiseId.getExpertiseLevelId());
                // if expertise level is "ALL then getting goals for all levels of that type
                if (expertiseLevel.getExpertiseLevel().equalsIgnoreCase(KeyConstants.KEY_ALL_LEVELS)) {
                    ProgramTypes programTypes = programTypeRepository.findByProgramTypeId(programExpertiseId.getProgramTypeId());
                    List<ProgramExpertiseGoalsMapping> programExpertiseGoalsMappingList = programExpertiseGoalsMappingRepository.findByProgramExpertiseMappingProgramTypeProgramTypeId(programTypes.getProgramTypeId());
                    programExpertiseGoalsMappingList = programExpertiseGoalsMappingList.stream()
                            .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(ProgramExpertiseGoalsMapping::getProgramGoals, comparing(ProgramGoals::getProgramGoalId)))),
                                    ArrayList::new));
					/*
					 * ProgramSubTypes subType = null; if (programTypes != null) { subType =
					 * programSubTypeRepository
					 * .findByProgramTypeProgramTypeId(programTypes.getProgramTypeId()); }
					 */
                    ProgramExpertiseGoalsMappingResponse programExpertiseGoalsMappingResponse = new ProgramExpertiseGoalsMappingResponse();
                    programExpertiseGoalsMappingResponse.setProgramTypeId(programTypes.getProgramTypeId());
                    programExpertiseGoalsMappingResponse.setProgramType(programTypes.getProgramTypeName());
					/*
					 * if (subType != null) {
					 * programExpertiseGoalsMappingResponse.setProgramSubTypeId(subType.getId());
					 * programExpertiseGoalsMappingResponse.setProgramSubTypeName(subType.getName())
					 * ; }
					 */
                    programExpertiseGoalsMappingResponse.setExpertiseLevelId(expertiseLevel.getExpertiseLevelId());
                    programExpertiseGoalsMappingResponse.setExpertiseLevel(expertiseLevel.getExpertiseLevel());
                    for (ProgramExpertiseGoalsMapping programExpertiseGoalsMapping : programExpertiseGoalsMappingList) {
                        ProgramGoalsView programGoalsView = new ProgramGoalsView();
                        programGoalsView.setProgramTypeLevelGoalMappingId(programExpertiseGoalsMapping.getProgramExpertiseGoalsMappingId());
                        programGoalsView.setProgramGoalId(programExpertiseGoalsMapping.getProgramGoals().getProgramGoalId());
                        programGoalsView.setProgramGoal(programExpertiseGoalsMapping.getProgramGoals().getProgramGoal());
                        programGoalsViews.add(programGoalsView);
                    }
                    programExpertiseGoalsMappingResponse.setProgramGoals(programGoalsViews);
                    listOfProgramExpertiseGoalsResponses.add(programExpertiseGoalsMappingResponse);
                } else {
                    programExpertiseMappings = ProgramExpertiseMappingRepository.findByProgramTypeProgramTypeIdAndExpertiseLevelExpertiseLevelId(programExpertiseId.getProgramTypeId(), programExpertiseId.getExpertiseLevelId());

                    for (ProgramExpertiseMapping programExpertiseMapping1 : programExpertiseMappings) {
                        List<ProgramExpertiseGoalsMapping> programExpertiseGoal = programExpertiseGoalsMappingRepository.findByProgramExpertiseMappingProgramExpertiseMappingId(programExpertiseMapping1.getProgramExpertiseMappingId());
						/*
						 * ProgramSubTypes subType = null; if (programExpertiseMapping1 != null &&
						 * programExpertiseMapping1.getProgramType() != null) { subType =
						 * programSubTypeRepository.findByProgramTypeProgramTypeId(
						 * programExpertiseMapping1.getProgramType().getProgramTypeId()); }
						 */
                        
                        ProgramExpertiseGoalsMappingResponse programExpertiseGoalsMappingResponse = new ProgramExpertiseGoalsMappingResponse();
                        programExpertiseGoalsMappingResponse.setProgramTypeId((programExpertiseMapping1.getProgramType().getProgramTypeId()));
                        programExpertiseGoalsMappingResponse.setProgramType(programExpertiseMapping1.getProgramType().getProgramTypeName());
						/*
						 * if (subType != null) {
						 * programExpertiseGoalsMappingResponse.setProgramSubTypeId(subType.getId());
						 * programExpertiseGoalsMappingResponse.setProgramSubTypeName(subType.getName())
						 * ; }
						 */
                        programExpertiseGoalsMappingResponse.setExpertiseLevelId(programExpertiseMapping1.getExpertiseLevel().getExpertiseLevelId());
                        programExpertiseGoalsMappingResponse.setExpertiseLevel(programExpertiseMapping1.getExpertiseLevel().getExpertiseLevel());
                        for (ProgramExpertiseGoalsMapping programExpertiseGoalsMapping : programExpertiseGoal) {
                            ProgramGoalsView programGoalsView = new ProgramGoalsView();
                            programGoalsView.setProgramTypeLevelGoalMappingId(programExpertiseGoalsMapping.getProgramExpertiseGoalsMappingId());
                            programGoalsView.setProgramGoalId(programExpertiseGoalsMapping.getProgramGoals().getProgramGoalId());
                            programGoalsView.setProgramGoal(programExpertiseGoalsMapping.getProgramGoals().getProgramGoal());
                            programGoalsViews.add(programGoalsView);
                        }
                        programExpertiseGoalsMappingResponse.setProgramGoals(programGoalsViews);
                        listOfProgramExpertiseGoalsResponses.add(programExpertiseGoalsMappingResponse);
                    }
                }
            }
            long profilingEndTimeMillis = System.currentTimeMillis();
            log.info("Query and data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        } catch (Exception e) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.ERROR,
                    MessageConstants.ERROR);
        }
        if (listOfProgramExpertiseGoalsResponses.isEmpty())
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE,
                    MessageConstants.ERROR);

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getAllGoalsOnTypeAndLevel ends.");

        return listOfProgramExpertiseGoalsResponses;
    }

}
