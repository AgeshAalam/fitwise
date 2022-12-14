package com.fitwise.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserFcmToken {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userFcmTokenId;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	private String fcmtoken;

}
