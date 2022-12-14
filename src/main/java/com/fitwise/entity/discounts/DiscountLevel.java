package com.fitwise.entity.discounts;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DiscountLevel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int levelId;
	
	private String discountLevelName;// Program or Instructor level

}
