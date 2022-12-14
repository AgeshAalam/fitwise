package com.fitwise.entity.product;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.UpdateTimestamp;

import com.fitwise.entity.Programs;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FreeAccessProgram {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long freeAccessProgramId;

    @ManyToOne(cascade = { CascadeType.DETACH })
    @JoinColumn(name = "free_product_id", nullable = false)
    private FreeProduct freeProduct;

    @ManyToOne
    @JoinColumn(name = "program_id", nullable = false)
    private Programs program;
    
	private boolean isActive;
	
	@UpdateTimestamp
	private Date updatedOn;

}
