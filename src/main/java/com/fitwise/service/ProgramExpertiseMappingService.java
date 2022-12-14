package com.fitwise.service;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.entity.ExpertiseLevels;
import com.fitwise.entity.ProgramExpertiseMapping;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.ProgramExpertiseMappingRepository;
import com.fitwise.response.ProgramExpertiseMappingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ProgramExpertiseMappingService {

    @Autowired
    ProgramExpertiseMappingRepository programExpertiseMappingRepository;

    public List<ProgramExpertiseMappingResponse> getAllExpertiseLevelsByProgramType(long[] listOfProgramTypes) {
        log.info("getAllExpertiseLevelsByProgramType starts.");
        long apiStartTimeMillis = System.currentTimeMillis();
        List<ProgramExpertiseMappingResponse> responseEntities = new ArrayList<>();
        try {
			long profilingStartTimeMillis = System.currentTimeMillis();
            for (int index = 0; index < listOfProgramTypes.length; index++) {
                long programTypeId = listOfProgramTypes[index];
                List<ProgramExpertiseMapping> list1 = programExpertiseMappingRepository.findByProgramTypeProgramTypeId(programTypeId);
                List<ExpertiseLevels> expertiseLevelList = new ArrayList<>();
                ProgramExpertiseMappingResponse responseEntity = null;
                for (ProgramExpertiseMapping list : list1) {
                    ProgramTypes programType = list.getProgramType();
                    ExpertiseLevels expertiseLevel = list.getExpertiseLevel();
                    responseEntity = new ProgramExpertiseMappingResponse();
                    responseEntity.setProgramTypeId(programType.getProgramTypeId());
                    responseEntity.setProgramTypeName(programType.getProgramTypeName());
                    responseEntity.setIconUrl(programType.getIconUrl());
                    expertiseLevelList.add(expertiseLevel);
                }
                responseEntity.setExpertiseLevel(expertiseLevelList);
                responseEntities.add(responseEntity);
            }
			long profilingEndTimeMillis = System.currentTimeMillis();
			log.info("Response construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        } catch (Exception exception) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.ERROR,
                    MessageConstants.ERROR);
        }
        if (responseEntities.isEmpty())
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE,
                    MessageConstants.ERROR);
        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getAllExpertiseLevelsByProgramType ends.");
        return responseEntities;
    }
}
