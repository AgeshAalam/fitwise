package com.fitwise.response.freeaccess;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FreeAccessProgramDetails {

	private Long freeAccessUserSpecificId;
	private String firstName;
	private String lastName;
	private String imageUrl;
	private String email;
	private Date onboardedOn;
	private String onboardedDateFormatted;
	private String programName;
	private Date freeAccessStartDate;
	private Date freeAccessEndDate;
	private String freeAccessStartDateFormatted;
	private String freeAccessEndDateFormatted;
	private Date lastAccess;
	private String lastAccessFormatted;

}
