package com.fitwise.repository;

import com.fitwise.entity.OtherExpertise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtherExpertiseRepository extends JpaRepository<OtherExpertise,Long> {

    OtherExpertise findByOtherExpertiseId(final long otherExpertiseId);

    List<OtherExpertise> findByUserUserId(final long userId);

}
