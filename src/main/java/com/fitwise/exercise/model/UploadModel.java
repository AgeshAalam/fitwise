package com.fitwise.exercise.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadModel implements Serializable{

	/**
	 * The default serialization key
	 */
	private static final long serialVersionUID = 1L;
	
	/**  
	 * The status 
	*/
	@JsonProperty("status")
	private String status;
	
	/**  
	 * The status 
	*/
	@JsonProperty("uploadLink")
	private String upload_link;
		
	/**
	 *  The Approach
	 */
	@JsonProperty("approach")
	private String approach;
	
	/**  
	 * The status 
	*/
	@JsonProperty("size")
	private long size;
	
}
