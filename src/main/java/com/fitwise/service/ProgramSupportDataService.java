package com.fitwise.service;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.Duration;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.DurationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class ProgramSupportDataService.
 */
@Service
public class ProgramSupportDataService {
	
	/** The duration repo. */
	@Autowired
	private DurationRepo durationRepo;
	
	/**
	 * Gets the all duration.
	 *
	 * @return the all duration
	 */
	public List<Duration> getAllDuration() {
		List<Duration> durations= durationRepo.findAll(Sort.by("duration").ascending());
		if(durations.isEmpty()) {
			throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, MessageConstants.ERROR);
		}
		return durations;
	}

}
