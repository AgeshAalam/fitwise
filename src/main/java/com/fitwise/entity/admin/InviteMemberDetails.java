package com.fitwise.entity.admin;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class InviteMemberDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long inviteMemberId;
	private String email;
	private String firstName;
	private String lastName;
	private boolean userRegistered;
	@CreationTimestamp
	private Date createdDate;
	@UpdateTimestamp
	private Date modifiedDate;

}
