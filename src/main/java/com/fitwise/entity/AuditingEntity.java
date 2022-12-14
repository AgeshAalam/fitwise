package com.fitwise.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * The AuditingEntity class
 * 
 * @author ameex
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AuditingEntity {

	/**
	 * the created date
	 */
	@Column(name = "created_date", updatable = false)
	@CreationTimestamp
	private Date createdDate;

	/**
	 * the modified date
	 */
	@Column(name = "modified_date")
	@UpdateTimestamp
	private Date modifiedDate;

}
