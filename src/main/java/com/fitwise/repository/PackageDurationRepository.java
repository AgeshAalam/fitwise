package com.fitwise.repository;

import com.fitwise.entity.Duration;
import com.fitwise.entity.PackageDuration;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Created by Vignesh G on 07/01/21
 */
public interface PackageDurationRepository  extends JpaRepository<PackageDuration, Long> {

    /**
     * @param duration
     * @return
     */
    PackageDuration findByDuration(Long duration);

}
