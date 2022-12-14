package com.fitwise.entity;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "images", indexes = {
		@Index(name = "index_id", columnList = "image_id", unique = true)
})
public class Images {

	@Id
	@GeneratedValue
	@Column(name = "image_id")
	private Long imageId;
	
	private String imagePath;

	private String fileName;

}