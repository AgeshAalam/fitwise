package com.fitwise.exercise.service.v2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.VideoUploadStatus;
import com.fitwise.entity.Equipments;
import com.fitwise.entity.Exercises;
import com.fitwise.entity.Images;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.VideoManagement;
import com.fitwise.exception.ApplicationException;
import com.fitwise.exercise.daoImpl.ExerciseRepoImpl;
import com.fitwise.exercise.model.ExerciseModel;
import com.fitwise.exercise.model.SupportingVideoModel;
import com.fitwise.exercise.model.VimeoModel;
import com.fitwise.exercise.service.ExerciseService;
import com.fitwise.exercise.service.VimeoService;
import com.fitwise.repository.EquipmentsRepository;
import com.fitwise.repository.ExerciseRepository;
import com.fitwise.repository.ImageRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.VideoManagementRepo;
import com.fitwise.service.instructor.InstructorProgramService;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.ResponseModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExerciseServiceV2 {

	private final UserComponents userComponents;
	private final UserProfileRepository userProfileRepository;
	private final VimeoService vimeoService;
	private final ExerciseRepoImpl exerciseImpl;
	private final ExerciseService exerciseService;
	private final ImageRepository imageRepository;
	private final VideoManagementRepo videoManagementRepo;
    private final ExerciseRepository exerciseRepository;
    private final EquipmentsRepository equipmentsRepository;


	/**
	 * Creates the exercise.
	 *
	 * @param request the request
	 * @return the response model
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Transactional
	public ResponseModel createExerciseV2(final ExerciseModel request) throws IOException {
		log.info("Create Exercise starts");
		User user = userComponents.getUser();
		Date createExerciseStart = new Date();
		ResponseModel response = new ResponseModel();
		UserProfile userProfile = userProfileRepository.findByUser(user);
		if (request.getFileName() != null && !request.getFileName().isEmpty() && userProfile.getFirstName() != null
				&& userProfile.getLastName() != null) {
			request.setFileName(
					userProfile.getFirstName() + " " + userProfile.getLastName() + " - " + request.getFileName());
		}
		VimeoModel vimeoModel = new VimeoModel();
		// Getting video Id & Verify is valid video Id or not
		Long videoId = request.getVideoId();
		if (videoId != null && videoId != 0) {
			validateVideoManageMentId(videoId, null, false);
		} else {
			exerciseService.doConstructVimeoModel(request, vimeoModel);
			long profilingStart = new Date().getTime();
			vimeoModel = vimeoService.createVideoPlaceholder(vimeoModel);
			long profilingEnd = new Date().getTime();
			log.info("Creating placeholder for exercise video : time taken in millis : "
					+ (profilingEnd - profilingStart));
			request.setVimeoData(vimeoModel);

		}
		exerciseService.doValidateExercise(request);
		// Supporting Video vimeo model
		VimeoModel supportingVimeoModel = null;
		SupportingVideoModel supportingVideoModel = request.getSupportingVideoModel();
		if (supportingVideoModel != null) {
			// Get support video id & verify
			Long supportVideoId = supportingVideoModel.getVideoId();
			if (supportVideoId != null && supportVideoId != 0) {
				validateVideoManageMentId(supportVideoId, null, true);
			} else {
				supportingVimeoModel = new VimeoModel();
				if (request.getFileName() != null && !request.getFileName().isEmpty() && userProfile.getFirstName() != null
						&& userProfile.getLastName() != null) {
					supportingVideoModel.setFileName(userProfile.getFirstName() + " " + userProfile.getLastName()
							+ " - " + supportingVideoModel.getFileName());
				}
				exerciseService.doConstructSupportingVideoVimeoModel(supportingVideoModel, supportingVimeoModel);
				Date supportStart = new Date();
				log.info("createVideoPlaceholder for supporting video started at : " + supportStart + " by "
						+ user.getEmail());
				supportingVimeoModel = vimeoService.createVideoPlaceholder(supportingVimeoModel);
				Date supportEnd = new Date();
				log.info("createVideoPlaceholder for supporting video ended at : " + supportEnd + " by "
						+ user.getEmail());
				log.info("createVideoPlaceholder for supporting video completion took : "
						+ (supportEnd.getTime() - supportStart.getTime()) + " ms for " + user.getEmail());
				request.getSupportingVideoModel().setVimeoData(supportingVimeoModel);
			}

		}
		// Need to store exercise in db
		Exercises exercise = new Exercises();
		doConstructExerciseEntityV2(exercise, request, vimeoModel, supportingVimeoModel);
		// Saving exercise
		exercise = exerciseImpl.saveExercise(exercise);
		request.setExerciseId(exercise.getExerciseId());

		Date createExerciseEnd = new Date();
		log.info("createExercise for exercise ended at : " + createExerciseEnd + " by " + user.getEmail());
		log.info("createExercise completion took : " + (createExerciseEnd.getTime() - createExerciseStart.getTime())
				+ " ms for " + user.getEmail());
		response.setMessage("Successfully stored exercise and created placeholder for video");
		if ((videoId != null && videoId != 0) && supportingVideoModel != null
				&& (supportingVideoModel.getVideoId() != null && supportingVideoModel.getVideoId() != 0)) {
			response.setMessage("Successfully stored exercise");
		}
		response.setPayload(request);
		response.setStatus(Constants.SUCCESS_STATUS);
		return response;
	}

	/**
	 * Do construct exercise entity.
	 *
	 * @param exercise the exercise
	 * @param request the request
	 * @param vimeoModel the vimeo model
	 * @param supportingVideoVimeoModel the supporting video vimeo model
	 */
	private void doConstructExerciseEntityV2(Exercises exercise, ExerciseModel request, VimeoModel vimeoModel,
			VimeoModel supportingVideoVimeoModel) {
		User user = userComponents.getUser();
		String exerciseTitle = request.getTitle();
		exercise.setTitle(exerciseTitle);
		exercise.setDescription(request.getDescription());
		// Duplicate Exercise title validation
		List<Exercises> exercisesWithSameTitle = exerciseImpl.findByOwnerUserIdAndTitle(user.getUserId(),
				exerciseTitle);
		if (!exercisesWithSameTitle.isEmpty()) {
			throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_EXERCISE_DUPLICATE_TITLE,
					MessageConstants.ERROR);
		}
		exerciseService.doValidateRoleAsInstructor(user);
		exercise.setOwner(user);
		VideoManagement videoManagement = null;
		Long videoId = request.getVideoId();
		if (videoId != null && videoId != 0) {
			videoManagement = validateVideoManageMentId(videoId, request, false);
			request.setDuration(videoManagement.getDuration());
		} else {
			videoManagement = new VideoManagement();
			videoManagement.setOwner(user);
			Images image = imageRepository.findByImageId(request.getImageId());
			if (image != null)
				videoManagement.setThumbnail(image);
			videoManagement.setUploadStatus(VideoUploadStatus.UPLOAD);
			request.setVideoUploadStatus(VideoUploadStatus.UPLOAD);
			exerciseService.doConstructVideoManagement(exercise, vimeoModel, videoManagement);
		}
		exercise.setVideoManagement(videoManagement);
		if (request.getSupportingVideoModel() != null) {
			VideoManagement supportingVideoManagement = null;
			// Get support video id & verify
			Long supportVideoId = request.getSupportingVideoModel().getVideoId();
			if (supportVideoId != null && supportVideoId != 0) {
				supportingVideoManagement = validateVideoManageMentId(supportVideoId, request, true);
				request.getSupportingVideoModel().setDuration(supportingVideoManagement.getDuration());
			} else {
				supportingVideoManagement = new VideoManagement();
				supportingVideoManagement.setOwner(user);
				Images supportVideoImage = imageRepository
						.findByImageId(request.getSupportingVideoModel().getImageId());
				if (supportVideoImage != null)
					supportingVideoManagement.setThumbnail(supportVideoImage);
				supportingVideoManagement.setUploadStatus(VideoUploadStatus.UPLOAD);
				request.getSupportingVideoModel().setVideoUploadStatus(VideoUploadStatus.UPLOAD);
				exerciseService.doConstructSupportVideoManagement(request.getSupportingVideoModel(),
						supportingVideoVimeoModel, supportingVideoManagement);
			}
			exercise.setSupportVideoManagement(supportingVideoManagement);
		}
        /*
         * Iterating and setting exercise to list from request object
         */
        List<Equipments> equipmentsList = new ArrayList<>();
        for (Equipments equipment : request.getEquipments()) {
            Equipments equipmentFromDb = equipmentsRepository.findByEquipmentId(equipment.getEquipmentId());
            equipmentsList.add(equipmentFromDb);
        }
        if (!equipmentsList.isEmpty())
            exercise.setEquipments(equipmentsList);
	}

	/**
	 * Validate video manage ment id.
	 *
	 * @param videoId the video id
	 * @param exeModel the exe model
	 * @param isSupportVideo the is support video
	 */
	private VideoManagement validateVideoManageMentId(final Long videoId, final ExerciseModel exeModel,
			final boolean isSupportVideo) {
		VideoManagement videoForId = videoManagementRepo.findByVideoManagementId(videoId);
		ValidationUtils.throwException(videoForId == null, "Invalid Video Id", Constants.BAD_REQUEST);
		if (exeModel != null) {
			VimeoModel vimeoModel = new VimeoModel();
			vimeoModel.setVideoId(videoForId.getVideoManagementId());
			vimeoModel.setUri(videoForId.getUrl());
			vimeoModel.setName(videoForId.getTitle());
			if (isSupportVideo) {
				exeModel.getSupportingVideoModel().setVideoUploadStatus(videoForId.getUploadStatus());
				exeModel.getSupportingVideoModel().setVimeoData(vimeoModel);
			} else {
				exeModel.setVideoUploadStatus(videoForId.getUploadStatus());
				exeModel.setVimeoData(vimeoModel);
			}
		}
		return videoForId;
	}
	
	
	/**
	 * Edits the exercise V 2.
	 *
	 * @param exerciseModel the exercise model
	 * @return the response model
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ResponseModel editExerciseV2(ExerciseModel exerciseModel)
			throws IOException {
		log.info("Edit exercise started " + new Date());
		Exercises exercises;
		User user = userComponents.getUser();
		UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId());
		if (exerciseModel.getExerciseId() != null) {
			exercises = exerciseRepository.findByExerciseId(exerciseModel.getExerciseId());
			if (exercises == null) {
				throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_NOT_FOUND,
						null);
			} else {
				if (exercises.isByAdmin()) {
					throw new ApplicationException(Constants.FORBIDDEN,
							ValidationMessageConstants.MSG_STOCK_EXERCISE_CANNOT_EDITED_BY_INSTRUCTOR, null);
				}
			}
		} else {
			throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_NOT_FOUND,
					MessageConstants.ERROR);
		}
		if (exerciseModel.getTitle() != null && !exercises.getTitle().equals(exerciseModel.getTitle())) {
			throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EXERCISE_TITLE_CHANGE,
					MessageConstants.ERROR);
		}
		int count = 0;
		boolean isEqual = false;
		if (exerciseModel.getEquipments().size() == exercises.getEquipments().size()) {
			for (Equipments equipment : exerciseModel.getEquipments()) {
				for (Equipments equipments : exercises.getEquipments()) {
					if (equipment.getEquipmentId().equals(equipments.getEquipmentId())) {
						count++;
					}
				}
			}
			if (count == exerciseModel.getEquipments().size()) {
				isEqual = true;
			}
		}
		if (exerciseModel.getEquipments() != null && !isEqual) {
			exercises.setEquipments(null);
			exerciseRepository.save(exercises);
			List<Equipments> equipmentsList = new ArrayList<>();
			for (Equipments equipment : exerciseModel.getEquipments()) {
				Equipments equipmentFromDb = equipmentsRepository.findByEquipmentId(equipment.getEquipmentId());
				equipmentsList.add(equipmentFromDb);
			}
			if (!equipmentsList.isEmpty())
				exercises.setEquipments(equipmentsList);
			exerciseRepository.save(exercises);
		}

		VimeoModel vimeoModel = new VimeoModel();
		VideoManagement videoMangement = null;
		// Getting video Id & Verify is valid video Id or not
		Long videoId = exerciseModel.getVideoId();
		if (videoId != null && videoId != 0) {
			videoMangement = validateVideoManageMentId(videoId, null, false);
		} else {
			exerciseService.doConstructVimeoModel(exerciseModel, vimeoModel);
			long profilingStart = new Date().getTime();
			vimeoModel = vimeoService.createVideoPlaceholder(vimeoModel);
			long profilingEnd = new Date().getTime();
			log.info("Creating placeholder for exercise video : time taken in millis : "
					+ (profilingEnd - profilingStart));
			exerciseModel.setVimeoData(vimeoModel);
			//
			videoMangement = new VideoManagement();
			videoMangement.setOwner(user);
			videoMangement.setTitle(exerciseModel.getTitle());
			Images image = imageRepository.findByImageId(exerciseModel.getImageId());
			if (image != null)
				videoMangement.setThumbnail(image);
			videoMangement.setUploadStatus(VideoUploadStatus.UPLOAD);
			exerciseModel.setVideoUploadStatus(VideoUploadStatus.UPLOAD);
			exerciseService.doConstructVideoManagement(exercises, vimeoModel, videoMangement);
			videoManagementRepo.save(videoMangement);
		}
		if(videoMangement != null)
			exercises.setVideoManagement(videoMangement);

		VimeoModel supportingVimeoModel = null;
		SupportingVideoModel supportingVideoModel = exerciseModel.getSupportingVideoModel();
		if (supportingVideoModel != null) {
			VideoManagement supportingVideoManagement = null;
			// Get support video id & verify
			Long supportVideoId = supportingVideoModel.getVideoId();
			if (supportVideoId != null && supportVideoId != 0) {
				supportingVideoManagement = validateVideoManageMentId(supportVideoId, null, true);
			} else {
				supportingVimeoModel = new VimeoModel();
				if (exerciseModel.getFileName() != null && !exerciseModel.getFileName().isEmpty()
						&& userProfile.getFirstName() != null && userProfile.getLastName() != null) {
					supportingVideoModel.setFileName(userProfile.getFirstName() + " " + userProfile.getLastName()
							+ " - " + supportingVideoModel.getFileName());
				}
				exerciseService.doConstructSupportingVideoVimeoModel(supportingVideoModel, supportingVimeoModel);
				Date supportStart = new Date();
				log.info("createVideoPlaceholder for supporting video started at : " + supportStart + " by "
						+ user.getEmail());
				supportingVimeoModel = vimeoService.createVideoPlaceholder(supportingVimeoModel);
				Date supportEnd = new Date();
				log.info("createVideoPlaceholder for supporting video ended at : " + supportEnd + " by "
						+ user.getEmail());
				log.info("createVideoPlaceholder for supporting video completion took : "
						+ (supportEnd.getTime() - supportStart.getTime()) + " ms for " + user.getEmail());
				exerciseModel.getSupportingVideoModel().setVimeoData(supportingVimeoModel);

				supportingVideoManagement = new VideoManagement();
				supportingVideoManagement.setOwner(user);
				//supportingVideoManagement.setTitle(exerciseModel.getTitle());

				Images supportVideoImage = imageRepository
						.findByImageId(exerciseModel.getSupportingVideoModel().getImageId());
				if (supportVideoImage != null)
					supportingVideoManagement.setThumbnail(supportVideoImage);
				supportingVideoManagement.setUploadStatus(VideoUploadStatus.UPLOAD);
				exerciseModel.getSupportingVideoModel().setVideoUploadStatus(VideoUploadStatus.UPLOAD);
				exerciseService.doConstructSupportVideoManagement(exerciseModel.getSupportingVideoModel(),
						supportingVimeoModel, supportingVideoManagement);
				videoManagementRepo.save(supportingVideoManagement);
			}
			if(supportingVideoManagement != null)
				exercises.setSupportVideoManagement(supportingVideoManagement);
		}
		
		if (exercises.getSupportVideoManagement() != null && supportingVideoModel == null) {
			exercises.setSupportVideoManagement(null);
		}
		exerciseRepository.save(exercises);

		log.info("Exercise edit completed on " + new Date());
		return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_EXERCISE_UPDATED, exerciseModel);
	}
}
