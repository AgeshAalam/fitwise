package com.fitwise.entity.instructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.Data;

@Entity
@Data
public class Tier {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long tierId;

	private String tierType;

	private Boolean isActive;

	@OneToOne
	@JoinColumn(name = "tier_type_detail_id")
	private TierTypeDetails tierTypeDetails;

}
