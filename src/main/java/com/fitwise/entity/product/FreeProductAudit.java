package com.fitwise.entity.product;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class FreeProductAudit.
 */
@Entity

/**
 * Gets the free product.
 *
 * @return the free product
 */
@Getter

/**
 * Sets the free product.
 *
 * @param freeProduct the new free product
 */
@Setter
public class FreeProductAudit {

	/** The free product audit id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long freeProductAuditId;

	/** The access duration. */
	private String accessDuration;

	/** The updated on. */
	@UpdateTimestamp
	private Date updatedOn;

	/** The free product. */
	@ManyToOne
	@JoinColumn(name = "free_product_id")
	private FreeProduct freeProduct;

}
