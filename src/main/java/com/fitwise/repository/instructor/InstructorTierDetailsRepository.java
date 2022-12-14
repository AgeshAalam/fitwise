package com.fitwise.repository.instructor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.User;
import com.fitwise.entity.instructor.InstructorTierDetails;

@Repository
public interface InstructorTierDetailsRepository extends JpaRepository<InstructorTierDetails,Long>{
	
	InstructorTierDetails findByUser(User user);
	
	InstructorTierDetails findByUserAndActive(User user, boolean active);


}
