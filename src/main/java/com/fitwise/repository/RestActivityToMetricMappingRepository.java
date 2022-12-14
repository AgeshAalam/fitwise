package com.fitwise.repository;

import com.fitwise.entity.RestActivityToMetricMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 18/05/20
 */
@Repository
public interface RestActivityToMetricMappingRepository extends JpaRepository<RestActivityToMetricMapping, Long> {

    /**
     * Get RestActivityToMetricMapping liat for a RestActivity Id
     * @param restActivityId
     * @return
     */
    List<RestActivityToMetricMapping> findByRestActivityRestActivityId(Long restActivityId);

    /**
     * Get mapping based on activity and metric
     * @param restActivityId
     * @param restMetricId
     * @return
     */
    RestActivityToMetricMapping findByRestActivityRestActivityIdAndRestMetricRestMetricId(Long restActivityId, Long restMetricId);

}
