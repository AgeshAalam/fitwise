package com.fitwise.entity.payments.appleiap;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.CreationTimestamp;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class VerifyReceipt extends AuditingEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private int id;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "program_id")
	private Programs program;
	
	@Column(name="receipt_data", columnDefinition = "MEDIUMTEXT")
	private String receiptData ;
	
	@Column(name="original_transaction")
	private String originalTxnId ;
	
	private String programName ;
	
	/*@Column(name = "created_date", updatable = false)
	@CreationTimestamp
	private Date createdDate;*/
}
