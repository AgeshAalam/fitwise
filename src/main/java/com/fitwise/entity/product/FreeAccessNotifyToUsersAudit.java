package com.fitwise.entity.product;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.UpdateTimestamp;

import com.fitwise.entity.User;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FreeAccessNotifyToUsersAudit {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long freeProductNotifyToUsersAuditId;

	@ManyToOne
	@JoinColumn(name = "free_product_id")
	private FreeProduct freeProduct;
	
	private String type;
	
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
	@UpdateTimestamp
	private Date updatedOn;
}
