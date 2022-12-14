package com.fitwise.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class Equipments.
 */
@Entity
@Getter
@Setter
public class Equipments extends AuditingEntity {

	/** The equipment id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long equipmentId;
	
	/** The equipment. */
	private String equipmentName;

	private String iconUrl;

	private boolean isPrimary;

}