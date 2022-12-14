package com.fitwise.entity.product;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class FreeProduct {

	/** The free product id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long freeProductId;

	/** The type. */
	private String type;
	
	private Date freeAccessStartDate;
	
	private Date freeAccessEndDate;

	/** The updated on. */
	@UpdateTimestamp
	private Date updatedOn;

}
