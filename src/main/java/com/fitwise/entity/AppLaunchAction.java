package com.fitwise.entity;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AppLaunchAction {
	
	/** The action id. */
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int actionId;
	
	/** The action name. */
	private String actionName;
 
}
