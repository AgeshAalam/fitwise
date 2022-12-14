package com.fitwise.response;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminInviteMembersDetails {

	private String email;
	private String firstName;
	private String lastName;
	private String status;
	private Date inviteSentOn;
	private String inviteSentOnFormatted;

}
