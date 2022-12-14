package com.fitwise.response.freeaccess;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramMinimumDetails {
	
	/** The program id. */
	private long programId;
	
	/** The program name. */
	private String programName;
	
	/** The instructor name. */
	private String instructorName;
	
	/** The formatted program price. */
	private String formattedProgramPrice;
	
	/** The program price. */
	private String programPrice;

}
