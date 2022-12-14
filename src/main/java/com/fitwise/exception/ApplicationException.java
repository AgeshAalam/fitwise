package com.fitwise.exception;

/**
 * The Class ApplicationException.
 */
public class ApplicationException extends RuntimeException {

	public ApplicationException(String message){
		super(message);
	}

	public ApplicationException(String message, Throwable exception){
		super(message, exception);
	}
	
	public ApplicationException(long status, String message, String error) {
		super();
		this.status = status;
		this.message = message;
		this.error = error;
	}
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private long status;

	/** The status. */
	private String message;

	/** The message. */
	private String error;

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

}
