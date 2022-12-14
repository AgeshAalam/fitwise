package com.fitwise.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class Promotions.
 */
@Entity
@Getter
@Setter
public class Promotions {

	/** The promotion id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long promotionId;
	
	/** The video management. */
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "video_management_id")
	private VideoManagement videoManagement;
	
	/** The title. */
	private String title;
	
	/** The description. */
	private String description;
	
	/** The active. */
	private boolean active;

}
