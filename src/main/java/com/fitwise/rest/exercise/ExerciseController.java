package com.fitwise.rest.exercise;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fitwise.constants.Constants;
import com.fitwise.constants.*;
import com.fitwise.entity.WorkoutRestVideos;
import com.fitwise.repository.WorkoutRestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.model.ExerciseModel;
import com.fitwise.exercise.service.ExerciseService;
import com.fitwise.view.ResponseModel;

/**
 * The Class ExerciseController.
 */
@RestController
@CrossOrigin("*")
@RequestMapping(value="/v1/exercise")
public class ExerciseController {
	
	/** The exercise service. */
	@Autowired
	private ExerciseService exerciseService;

	@Autowired
	private WorkoutRestRepository workoutRestRepository;
	
	/**
	 * Creates the exercise.
	 *
	 * @param request the request
	 * @return the response model
	 */
	@PostMapping(value="/create")
	public ResponseModel createExercise(@RequestBody ExerciseModel request) {
		ResponseModel response = null;
		try {
			response = exerciseService.createExercise(request);			
		}catch(ApplicationException aex) {
			response= new ResponseModel();
			response.setStatus(aex.getStatus());
			response.setError(aex.getMessage());			
		}catch(IOException iex) {
			response= new ResponseModel();
			response.setStatus(500);
			response.setError(iex.getMessage());
		}
		return response;
	}

	/**
	 * Validate the exercise name for duplicate
	 * @param exerciseName
	 * @return
	 */
	@GetMapping(value = "/validate/name")
	public ResponseModel validateProgramName(@RequestParam String exerciseName){
		exerciseService.validateName(exerciseName);
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SCS_EX_NAME_VALID, null);
	}
	
	/**
	 * Gets the exercise.
	 *
	 * @param exerciseId the exercise id
	 * @return the exercise
	 * @throws ApplicationException the application exception
	 */
	@GetMapping
	public ResponseModel getExercise(@RequestParam final Long exerciseId) {
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, exerciseService.getExercise(exerciseId));
	}
	
	/**
	 * Gets the exercises.
	 *
	 * @param
	 * @return the exercises
	 * @throws ApplicationException the application exception
	 */
	@GetMapping(value = "/all")
	public ResponseModel getExercises(Optional<String> searchName) throws ApplicationException {
		Map<String, Object> response = new HashMap<>();
		response.put("exercises", exerciseService.getExercises(searchName));
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, response);
	}

	/**
	 * Gets the equipments.
	 *
	 * @return the equipments
	 */
	@GetMapping(value = "/equipments")
	public ResponseModel getEquipments(){
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, exerciseService.getEquipments());
	}

	@GetMapping("/getRestDuration")
	public ResponseModel getRestDuration(){
		ResponseModel responseModel=new ResponseModel();
		List<WorkoutRestVideos> workoutRestVideos=workoutRestRepository.findAll();
		responseModel.setPayload(workoutRestVideos);
		responseModel.setStatus(Constants.SUCCESS_STATUS);
		responseModel.setMessage(MessageConstants.MSG_REST_DURATION_FETCHED);
		return responseModel;

	}

	/**
	 *
	 * @param pageNo
	 * @param pageSize
	 * @param searchName
	 * @return
	 */
	@GetMapping(value = "/getAllInstructorExercises")
	public ResponseModel getAllInstructorExercises(@RequestParam final int pageNo, @RequestParam final int pageSize, @RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> searchName) {
		return exerciseService.getAllInstructorExercises(pageNo, pageSize, sortOrder, sortBy, searchName);
	}

	@DeleteMapping(value = "/deleteExercise")
	public ResponseModel deleteExercise(@RequestParam final Long exerciseId) throws ApplicationException {
		exerciseService.deleteExercise(exerciseId);
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_EXERCISE_DELETED, null);
	}

	@PostMapping(value="/edit")
	public ResponseModel editExercise(@RequestBody ExerciseModel request) {
		ResponseModel response = null;
		try {
			response = exerciseService.editExercise(request);
		}catch(ApplicationException aex) {
			response= new ResponseModel();
			response.setStatus(aex.getStatus());
			response.setError(aex.getMessage());
		}catch(IOException iex) {
			response= new ResponseModel();
			response.setStatus(500);
			response.setError(iex.getMessage());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return response;
	}
	
	/**
	 * Gets the all instructor videos.
	 *
	 * @param pageNo the page no
	 * @param pageSize the page size
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @param searchName the search name
	 * @return the all instructor videos
	 */
	@GetMapping(value = "/getInstructorVideos")
	public ResponseModel getAllInstructorVideos(@RequestParam final int pageNo, @RequestParam final int pageSize,
			@RequestParam String sortOrder, @RequestParam String sortBy, @RequestParam Optional<String> searchName) {
		return exerciseService.getInstructorVideos(pageNo, pageSize, sortOrder, sortBy, searchName);
	}
}
