package com.fitwise.response.freeaccess;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddFreeAccess {

	private List<FreeAccessResponse> freePrograms;
	private List<FreeAccessResponse> freePackages;

	@Getter
	@Setter
	public static class FreeAccessResponse {
		private String email;
		private String title;
		private String status;
		private String message;
	}

}
