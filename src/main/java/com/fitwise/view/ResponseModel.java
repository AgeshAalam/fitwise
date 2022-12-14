package com.fitwise.view;

/**
 * The Class ResponseModel.
 */
public class ResponseModel {

	/** The status. */
	private long status;
	
	/** The message. */
	private String message = "";
	
	/**
	 * Error
	 */
	private String error;
	
	/** The payload. */
	private Object payload;
	
	public ResponseModel() {
		
	}

	public ResponseModel(long status, String message, Object payload) {
		this.status = status;
		this.message = message;
		this.payload = payload;
	}
	
	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}
	
}
