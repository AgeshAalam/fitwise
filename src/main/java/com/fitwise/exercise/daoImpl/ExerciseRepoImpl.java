package com.fitwise.exercise.daoImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitwise.entity.Exercises;
import com.fitwise.entity.VideoManagement;
import com.fitwise.repository.ExerciseRepository;
import com.fitwise.repository.VideoManagementRepo;

/**
 * The Class ExerciseRepoImpl.
 */
@Service
public class ExerciseRepoImpl {

	/** The ex repo. */
	@Autowired
	private ExerciseRepository exRepo;

	/** The video repo. */
	@Autowired
	private VideoManagementRepo videoRepo;

	/**
	 * Gets the exercise by id.
	 *
	 * @param exId the ex id
	 * @return the exercise by id
	 */
	public Optional<Exercises> getExerciseById(Long exId) {
		return exRepo.findById(exId);
	}
		
	/**
	 * Save exercise.
	 *
	 * @param exercise the exercise
	 * @return the exercises
	 */
	public Exercises saveExercise(Exercises exercise) {
		
		return exRepo.save(exercise);
	}
	
	/**
	 * Save video management.
	 *
	 * @param videoManagement the video management
	 * @return the video management
	 */
	public VideoManagement saveVideoManagement(VideoManagement videoManagement) {
		return videoRepo.save(videoManagement);
	}
	
	/**
	 * Gets the video management by url.
	 *
	 * @param url the url
	 * @return the video management by url
	 */
	public VideoManagement getVideoManagementByUrl(String url) {
		return videoRepo.findByUrl(url);
	}

	/**
	 * Find by exercise id and owner user id.
	 *
	 * @param exerciseId the exercise id
	 * @param userId the user id
	 * @return the list
	 */
	public Exercises findByExerciseIdAndOwnerUserId(Long exerciseId, Long userId){
		return exRepo.findByExerciseIdAndOwnerUserId(exerciseId, userId);
	}
	
	/**
	 * Find by id.
	 *
	 * @param exerciseId the exercise id
	 * @return the exercises
	 */
	public Exercises findById(Long exerciseId) {
		return exRepo.findByExerciseId(exerciseId);
	}

	/**
	 * Find by owner user id.
	 *
	 * @param userId the user id
	 * @return the list
	 */
	public List<Exercises> findByOwnerUserId(Long userId){
		return exRepo.findByOwnerUserId(userId);
	}

	/**
	 * Find Exercise by owner and title
	 *
	 * @param userId
	 * @param title
	 * @return Exercise by owner and title
	 */
	public List<Exercises> findByOwnerUserIdAndTitle(Long userId, String title) {
		return exRepo.findByOwnerUserIdAndTitleIgnoreCase(userId, title);
	}

}
