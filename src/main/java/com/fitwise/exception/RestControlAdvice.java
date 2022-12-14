package com.fitwise.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fitwise.constants.Constants;
import com.fitwise.view.ResponseModel;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * The Class RestControlAdvice.
 */
@RestControllerAdvice
@Slf4j
public class RestControlAdvice {

	/**
	 * Invalid request.
	 *
	 * @param exception the exception
	 * @return the response model
	 */
	@ExceptionHandler(PropertyReferenceException.class)
	@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
	public ResponseModel invalidRequest(PropertyReferenceException exception) {
		ResponseModel model = new ResponseModel();
		model.setStatus(Constants.UNPROCESSABLE_ENTITY);
		model.setMessage(exception.getMessage());
		log.warn(exception.getMessage(), exception);
		return model;
	}
	
	/**
	 * Http server error.
	 *
	 * @param exception the exception
	 * @return the response entity
	 */
	@ExceptionHandler(HttpServerErrorException.class)
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<ResponseModel> httpServerError(HttpServerErrorException exception) {
		ResponseEntity<ResponseModel> responseEntity = null;
		ResponseModel responseModel = new ResponseModel();
		ResponseModel model = new Gson().fromJson(exception.getResponseBodyAsString(), ResponseModel.class);
		responseModel.setStatus(model.getStatus());
		responseModel.setMessage(model.getMessage());
		responseEntity = new ResponseEntity<>(responseModel, exception.getStatusCode());
		log.error(exception.getMessage(), exception);
		return responseEntity;
	}
	
	/**
	 * Invalid data exception.
	 *
	 * @param exception the exception
	 * @return the response model
	 */
	@ExceptionHandler(InvalidFormatException.class)
	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	public ResponseModel invalidDataException(InvalidFormatException exception) {
		ResponseModel model = new ResponseModel();
		model.setStatus(Constants.BAD_REQUEST);
		model.setMessage(Constants.RESPONSE_INVALID_DATA);
		log.error(exception.getMessage(), exception);
		return model;
	}
	
	/**
	 * Http client error.
	 *
	 * @param exception the exception
	 * @return the response entity
	 */
	@ExceptionHandler(HttpClientErrorException.class)
	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	public ResponseEntity<ResponseModel> httpClientError(HttpClientErrorException exception) {
		ResponseModel responseModel = new ResponseModel();
		ResponseModel model = new Gson().fromJson(exception.getResponseBodyAsString(), ResponseModel.class);
		responseModel.setStatus(model.getStatus());
		responseModel.setMessage(model.getMessage());
		log.error(exception.getMessage(), exception);
		return new ResponseEntity<>(responseModel, exception.getStatusCode());
	}
	
	/**
	 * Http unknown host.
	 *
	 * @param exception the exception
	 * @return the response model
	 */
	@ExceptionHandler(ResourceAccessException.class)
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseModel httpUnknownHost(ResourceAccessException exception) {
		ResponseModel model = new ResponseModel();
		model.setStatus(Constants.ERROR_STATUS);
		model.setMessage(exception.getMessage());
		log.error(exception.getMessage(), exception);
		return model;
	}
	
	/**
	 * Application exception.
	 *
	 * @param applicationException the application exception
	 * @return the response entity
	 */
	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ResponseModel> applicationException
										(ApplicationException applicationException){
		ResponseEntity<ResponseModel> entity = null;
		ResponseModel model = new ResponseModel();
		model.setStatus(applicationException.getStatus());
		model.setMessage(applicationException.getMessage());
		log.debug(applicationException.getMessage());
		if(applicationException.getStatus() == Constants.BAD_REQUEST) {
			entity = new ResponseEntity<>(model, HttpStatus.BAD_REQUEST);
		} else if(applicationException.getStatus() == Constants.UNAUTHORIZED) {
			entity = new ResponseEntity<>(model, HttpStatus.UNAUTHORIZED);
		} else if(applicationException.getStatus() == Constants.CONFLICT || applicationException.getStatus() == Constants.CAN_EDIT) {
			entity = new ResponseEntity<>(model, HttpStatus.CONFLICT);
		} else if(applicationException.getStatus() == Constants.PRECONDITION_FAILED) {
			entity = new ResponseEntity<>(model, HttpStatus.PRECONDITION_FAILED);
		} else if(applicationException.getStatus() == Constants.UNPROCESSABLE_ENTITY) {
			entity = new ResponseEntity<>(model, HttpStatus.UNPROCESSABLE_ENTITY);
		} else if(applicationException.getStatus() == Constants.ERROR_STATUS) {
			entity = new ResponseEntity<>(model, HttpStatus.INTERNAL_SERVER_ERROR);
		} else if(applicationException.getStatus() == Constants.FORBIDDEN) {
			entity = new ResponseEntity<>(model, HttpStatus.FORBIDDEN);
		} else if(applicationException.getStatus() == Constants.NOT_FOUND) {
			entity = new ResponseEntity<>(model, HttpStatus.NOT_FOUND);
		} else if(applicationException.getStatus() == Constants.GONE) {
			entity = new ResponseEntity<>(model, HttpStatus.GONE);
		} else if(applicationException.getStatus() == Constants.EMPTY_RESPONSE_STATUS) {
			entity = new ResponseEntity<>(model, HttpStatus.NO_CONTENT);
		} else if(applicationException.getStatus() == Constants.NOT_EXIST_STATUS) {
			entity = new ResponseEntity<>(model, HttpStatus.NOT_FOUND);
		} else if(applicationException.getStatus() == Constants.RESET_CONTENT || applicationException.getStatus() == Constants.CAPCHA_FAILURE || applicationException.getStatus() == Constants.CONTENT_NEEDS_TO_BE_VALIDATE) {
			entity = new ResponseEntity<>(model, HttpStatus.OK);
		}
		return entity;
	}
	
    /**
     * <h2>Exception</h2>
     * <p>Handling common exception at runtime</p>
     * @param ex exception
     * @return error response with error message
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseModel handleException(final Exception ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseModel(Constants.ERROR_STATUS, "Internal Server Error", null);
    }

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseModel handleRequestJsonException(final HttpMessageNotReadableException ex) {
		log.error(ex.getMessage(), ex);
		return new ResponseModel(Constants.BAD_REQUEST, "Invalid request body", null);
	}

	@ExceptionHandler(NumberFormatException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseModel handleRequestJsonException(final NumberFormatException ex) {
		log.error(ex.getMessage(), ex);
		return new ResponseModel(Constants.BAD_REQUEST, "Invalid number format", null);
	}

}