package com.fitwise.view;

import java.io.Serializable;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7940223790893884149L;

	private Long userId;

	private String clientId;

	private String practiceId;

	private String uuid;
	
	private Set<String> roleList;
	
	private String userName;
}
