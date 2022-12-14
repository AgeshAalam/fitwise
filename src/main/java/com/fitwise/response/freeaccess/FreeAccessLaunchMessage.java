package com.fitwise.response.freeaccess;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FreeAccessLaunchMessage {

	private boolean userHaveFreeAccess = false;
	private String message;
	private int freeProgramsCount = 0;
	private int freePackagesCount = 0;
}
