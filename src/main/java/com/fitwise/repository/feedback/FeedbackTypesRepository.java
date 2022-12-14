package com.fitwise.repository.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.FeedbackTypes;

@Repository
public interface FeedbackTypesRepository extends JpaRepository<FeedbackTypes, Long>{

	FeedbackTypes findByfeedbackTypeId(long id);
	
}
