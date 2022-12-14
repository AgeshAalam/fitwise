package com.fitwise.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitwise.entity.ProgramTypes;
import com.fitwise.repository.ProgramTypeRepository;

/**
 * The Class ProgramTypeService.
 */
@Service
@Slf4j
public class ProgramTypeService {

	/** The program type repository. */
	@Autowired
	ProgramTypeRepository programTypeRepository;
	
	/** The table name. */
	protected final String tableName = "ProgramTypes";

	/**
	 * Program types list.
	 *
	 * @return the list
	 */
	public List<ProgramTypes> programTypesList() {
		log.info("Get program types list starts.");
		long apiStartTimeMillis = new Date().getTime();
		List<ProgramTypes> programTypesList = new ArrayList<ProgramTypes>();
		try {
			programTypesList = programTypeRepository.findByOrderByProgramTypeNameAsc();
			if(programTypesList.isEmpty())
				throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE,
						MessageConstants.ERROR);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
		log.info("Get program types list ends.");
		return programTypesList;
	}

}
