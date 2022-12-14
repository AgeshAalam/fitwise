package com.fitwise.entity.calendar;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Zone {
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 private Long zoneId;
	 
	 private String zoneName;

	 private String zoneCode;
	 
	 private String zonedisplayName;
}

