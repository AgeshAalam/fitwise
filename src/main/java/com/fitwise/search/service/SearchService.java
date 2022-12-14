package com.fitwise.search.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.ProgramWiseGoal;
import com.fitwise.repository.*;
import com.fitwise.search.model.SearchedProgramsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitwise.constants.Constants;
import com.fitwise.entity.ProgramTypeLevelGoalMapping;
import com.fitwise.entity.Programs;
import com.fitwise.exception.ApplicationException;
import com.fitwise.search.model.SearchModel;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.ResponseModel;

/**
 * The Class SearchService.
 */
@Service
public class SearchService {

	/** The program repository. */
	@Autowired
	private ProgramRepository programRepository;
	
	/** The exper repo. */
	@Autowired
	private ExpertiseLevelRepository experRepo;
	
	/** The program type repository. */
	@Autowired
	private ProgramTypeRepository programTypeRepository;
	
	/** The instructor yearof experience repository. */
	@Autowired
	private InstructorYearofExperienceRepository instructorYearofExperienceRepository;
	
	/** The Program level goals repo. */
	@Autowired
	private com.fitwise.repository.ProgramLevelGoalsRepo ProgramLevelGoalsRepo;
	
	/** The exercise repository. */
	@Autowired
	ExerciseRepository exerciseRepository;
	
	/** The workout repository. */
	@Autowired
	WorkoutRepository workoutRepository;
	
	/** The equipments repository. */
	@Autowired
	EquipmentsRepository equipmentsRepository;
	
	/** The user profile repository. */
	@Autowired
	UserProfileRepository userProfileRepository;


	@Autowired
	private ProgramWiseGoalRepository programWiseGoalRepository;

	@Autowired
	UserComponents userComponents;
	
	/**
	 * Search filterby level.
	 *
	 * @param model the model
	 * @return the response model
	 * @throws ApplicationException the application exception
	 */
	/*
	 * Get Search Filter Data
	 * */
	public ResponseModel searchFilterbyLevel(SearchModel model) throws ApplicationException {

		ResponseModel response = new ResponseModel();
		

		List<Programs> superList = new ArrayList<Programs>();
		
		for(int i=0;i<model.getExpertsLevel().size();i++) {
			
			superList.addAll(programRepository.findByProgramExpertiseLevel(experRepo.findByExpertiseLevel(model.getExpertsLevel().get(i))));
		
		}
		
		for(int i=0;i<model.getProgramType().size();i++) {
			
			superList.addAll(programRepository.findByProgramType(programTypeRepository.findByProgramTypeName(model.getProgramType().get(i))));
		
		}
		
		for(int i=0;i<model.getDuration().size();i++) {
			
			superList.addAll(programRepository.findByDuration(model.getDuration().get(i)));
		}
		
		for(int i=0;i<model.getPrice().size();i++) {
			
			superList.addAll(programRepository.findByProgramPrice(model.getPrice().get(i)));
		}
		
		for(int i=0;i<model.getInstructorYearOfExperience().size();i++) {
			
			superList.addAll(programRepository.findByInstructorYearOfExperience(instructorYearofExperienceRepository.findByExperienceId(model.getInstructorYearOfExperience().get(i))));
		
		}
		ValidationUtils.throwException(model == null, "Search Filter cant be empty", Constants.BAD_REQUEST);
		response.setStatus(Constants.SUCCESS_STATUS);
		response.setPayload(response);
		response.setMessage("Successfully published program");

		return response;
	}
	
	/**
	 * Search keyword.
	 *
	 * @param model the model
	 * @return the response model
	 * @throws ApplicationException the application exception
	 */
	/*
	 * Get Search Keyword Data
	 * */
	public ResponseModel searchKeyword(SearchModel model) throws ApplicationException {

		ResponseModel response = new ResponseModel();
		
		ValidationUtils.throwException(model.getExercise() == null, "Search Keyword cant be empty", Constants.BAD_REQUEST);
		exerciseRepository.findByTitleIgnoreCaseContaining(model.getExercise());
		
		ValidationUtils.throwException(model.getWorkout() == null, "Search Keyword cant be empty", Constants.BAD_REQUEST);
		workoutRepository.findByTitleIgnoreCaseContaining(model.getWorkout());
		
		ValidationUtils.throwException(model.getProgram() == null, "Search Keyword cant be empty", Constants.BAD_REQUEST);
		programRepository.findByTitleIgnoreCaseContaining(model.getProgram());
		
		ValidationUtils.throwException(model.getProgramTypeName() == null, "Search Keyword cant be empty", Constants.BAD_REQUEST);
		ProgramTypeLevelGoalMapping mapping = ProgramLevelGoalsRepo.findByProgramType(programTypeRepository.findByProgramTypeName(model.getProgramTypeName()));
		
		
		ValidationUtils.throwException(model.getProgramTypeName() == null, "Search Keyword cant be empty", Constants.BAD_REQUEST);
		programRepository.findByProgramType(programTypeRepository.findByProgramTypeName(model.getProgramTypeName()));
		
		ValidationUtils.throwException(model.getEquipment() == null, "Search Keyword cant be empty", Constants.BAD_REQUEST);
		equipmentsRepository.findByEquipmentNameIgnoreCaseContaining(model.getEquipment());
		
		ValidationUtils.throwException(model.getDuration() == null, "Search Keyword cant be empty", Constants.BAD_REQUEST);
		programRepository.findByDuration(model.getDuration());
		
		ValidationUtils.throwException(model.getExercise() == null, "Search Keyword cant be empty", Constants.BAD_REQUEST);
		userProfileRepository.findByFirstNameIgnoreCaseContaining(model.getClient());
		
		ValidationUtils.throwException(model.getInstructor() == null, "Search Keyword cant be empty", Constants.BAD_REQUEST);
		userProfileRepository.findByFirstNameIgnoreCaseContaining(model.getInstructor());
		
		ValidationUtils.throwException(model == null, "Search Keyword cant be empty", Constants.BAD_REQUEST);
		response.setStatus(Constants.SUCCESS_STATUS);
		response.setPayload(response);
		response.setMessage("Successfully published program");
		return response;
	}

	public ResponseModel programSearch(String searchName){
		List<SearchedProgramsResponse> searchedProgramsResponses = new ArrayList<>();
		List<Programs> programs = null;
		if(searchName.isEmpty()){
			programs = programRepository.findByStatus(InstructorConstant.PUBLISH);
		}else {
			String searchName1 = searchName.replaceAll(" ", "");
			 programs = programRepository.getSearchedPrograms(searchName1, searchName1);
			List<ProgramWiseGoal> programWiseGoals = programWiseGoalRepository.findByProgramExpertiseGoalsMappingProgramGoalsProgramGoalIgnoreCaseContainingAndProgramStatus(searchName1, InstructorConstant.PUBLISH);
			for (ProgramWiseGoal programWiseGoal : programWiseGoals) {
				programs.add(programWiseGoal.getProgram());
			}
		}
		for (Programs program : programs.stream().distinct().collect(Collectors.toList())) {
			try {
				SearchedProgramsResponse searchedProgramsResponse = new SearchedProgramsResponse();
				searchedProgramsResponse.setProgramId(program.getProgramId());
				searchedProgramsResponse.setProgramTitle(program.getTitle());
				searchedProgramsResponse.setImageUrl(program.getImage().getImagePath());
				searchedProgramsResponse.setExpertiseLevel(program.getProgramExpertiseLevel().getExpertiseLevel());
				searchedProgramsResponses.add(searchedProgramsResponse);
			}catch(Exception exception){
				//if program is empty, we are removing
			}
		}
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, searchedProgramsResponses );
	}
}
