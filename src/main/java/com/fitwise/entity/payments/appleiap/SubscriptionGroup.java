package com.fitwise.entity.payments.appleiap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SubscriptionGroup {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="subscription_id")
	private int id;

	@Column(name = "subscription_groupname")
	private String subscriptionGroupName;

	@Column(name = "subscription_displayname")
	private String subscriptionDisplayName;
	
}
