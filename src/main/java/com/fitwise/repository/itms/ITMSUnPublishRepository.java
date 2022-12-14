package com.fitwise.repository.itms;

import com.fitwise.entity.Programs;
import com.fitwise.entity.itms.ItmsUnpublish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ITMSUnPublishRepository extends JpaRepository<ItmsUnpublish, Long> {

    List<ItmsUnpublish> findByProgram(final Programs program);

    List<ItmsUnpublish> findByNeedUpdate(final Boolean status);

    List<ItmsUnpublish> findAllByModifiedDateGreaterThanEqualAndModifiedDateLessThanEqual(Date time, Date time1);
}
