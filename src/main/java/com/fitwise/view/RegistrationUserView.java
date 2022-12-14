package com.fitwise.view;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationUserView {

	private String firstName;

	private String lastName;

	@JsonIgnore
	private Long id;

	private String email;

	private String password;

	@JsonIgnore
	private String passwordConfirm;

	private String userRole;

	private int otp;



}