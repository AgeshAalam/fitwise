package com.fitwise.repository.instructor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.instructor.TierTypeDetails;

@Repository
public interface TierTypeRepository extends JpaRepository<TierTypeDetails,Long>{

}
