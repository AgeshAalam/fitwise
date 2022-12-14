package com.fitwise.repository;

import com.fitwise.entity.RestActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 18/05/20
 */
@Repository
public interface RestActivityRepository extends JpaRepository<RestActivity, Long> {

    /**
     * Get RestActivity object by activity name
     * @param restActivity
     * @return
     */
    RestActivity findByRestActivity(String restActivity);

}
