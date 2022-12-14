package com.fitwise.repository.itms;

import com.fitwise.entity.Programs;
import com.fitwise.entity.itms.ItmsPublish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ITMSPublishRepository extends JpaRepository<ItmsPublish, Long> {

    List<ItmsPublish> findByProgram(final Programs program);

    List<ItmsPublish> findByNeedUpdate(final Boolean status);

    List<ItmsPublish> findAllByModifiedDateGreaterThanEqualAndModifiedDateLessThanEqual(final Date startdate, final Date enddate);

    /**
     * @param needUpdate
     * @param status
     * @return
     */
    List<ItmsPublish> findByNeedUpdateAndStatus(Boolean needUpdate, String status);

}
