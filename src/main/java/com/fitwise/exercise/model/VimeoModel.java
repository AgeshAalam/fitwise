package com.fitwise.exercise.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VimeoModel implements Serializable{

	/**
	 * Default Serialization Id
	 */
	private static final long serialVersionUID = 1L;
	
	/**  
	 * The Uri 
	*/
	@JsonProperty("uri")
	private String uri;
	
	/**
	 * The Name
	 */
	@JsonProperty("name")
	private String name;
	
	/**
	 * Created time
	 */
	@JsonProperty("createdTime")
	private String created_time;
	
	/**
	 *  Modified time
	 */
	@JsonProperty("modifiedTime")
	private String modified_time;
	
	/**
	 * Upload 
	 */
	@JsonProperty("upload")
	private UploadModel upload;

	@JsonProperty("videoId")
	private long videoId;
}
