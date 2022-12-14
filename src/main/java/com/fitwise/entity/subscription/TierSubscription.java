package com.fitwise.entity.subscription;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.User;
import com.fitwise.entity.instructor.Tier;

import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class TierSubscription extends AuditingEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tier_subscription_id")
	private Long tierSubscriptionId;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "tier_id")
	private Tier tier;

	@Column(name = "subscribed_date")
	private Date subscribedDate;

	@ManyToOne
	@JoinColumn(name = "subscription_plan_id")
	private SubscriptionPlan subscriptionPlan;

	@ManyToOne
	@JoinColumn(name = "subscription_status_id")
	private SubscriptionStatus subscriptionStatus;

	private boolean isAutoRenewal;

	@ManyToOne
	@JoinColumn(name = "platform_id")
	private PlatformType subscribedViaPlatform;

}
