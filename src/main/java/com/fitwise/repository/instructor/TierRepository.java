package com.fitwise.repository.instructor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.instructor.Tier;

@Repository
public interface TierRepository extends JpaRepository<Tier,Long> {

	List<Tier> findByIsActiveTrue();
	
	Tier findByTierId(Long tierId);
	
}
