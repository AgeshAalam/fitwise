package com.fitwise.rest.v2.exercise;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.model.ExerciseModel;
import com.fitwise.exercise.service.v2.ExerciseServiceV2;
import com.fitwise.view.ResponseModel;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/v2/exercise")
@RequiredArgsConstructor
public class ExerciseV2Controller {

	private final ExerciseServiceV2 exerciseServiceV2;

	/**
	 * Creates the exercise.
	 *
	 * @param request the request
	 * @return the response model
	 */
	@PostMapping(value = "/create")
	public ResponseModel createExercise(@RequestBody ExerciseModel request) {
		ResponseModel response = null;
		try {
			response = exerciseServiceV2.createExerciseV2(request);
		} catch (ApplicationException applicationExc) {
			response = new ResponseModel();
			response.setStatus(applicationExc.getStatus());
			response.setError(applicationExc.getMessage());
		} catch (IOException ioExc) {
			response = new ResponseModel();
			response.setStatus(500);
			response.setError(ioExc.getMessage());
		}
		return response;
	}
	
	/**
	 * Edits the exercise.
	 *
	 * @param request the request
	 * @return the response model
	 */
	@PostMapping(value = "/edit")
	public ResponseModel editExercise(@RequestBody ExerciseModel request) {
		ResponseModel response = null;
		try {
			response = exerciseServiceV2.editExerciseV2(request);
		} catch (ApplicationException aex) {
			response = new ResponseModel();
			response.setStatus(aex.getStatus());
			response.setError(aex.getMessage());
		} catch (IOException iex) {
			response = new ResponseModel();
			response.setStatus(500);
			response.setError(iex.getMessage());
		}
		return response;
	}

}
