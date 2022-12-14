package com.fitwise.response.freeaccess;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class MemberMinimumDetails {
	
	private long userId;
	
	/** The first name. */
	private String firstName;
	
	/** The last name. */
	private String lastName;
	
	/** The email. */
	private String email;
	
	private String imageUrl;
	
	/** The onboarded on. */
	private Date onboardedOn;
	
	/** The onboarded date formatted. */
	private String onboardedDateFormatted;
	
}
