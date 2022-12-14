package com.fitwise.entity.payments.appleiap;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AppleSubscriptionStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long subscriptionStatusId;

	private String subscriptionStatusName;
}
