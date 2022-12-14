package com.fitwise.entity.calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CronofyAvailabilityRules {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userCronofyAvailabilityRulesId;
   
	@OneToOne
    private UserKloudlessMeeting userKloudlessMeeting;
	
	private String calenderId;
    
    private String bufferBefore;
    
    private String bufferAfter;
    
    private String startInterval;
    
    @Column(columnDefinition = "MEDIUMTEXT")
    private String weeklyPeriods;
    

}

