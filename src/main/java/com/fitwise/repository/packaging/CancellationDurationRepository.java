package com.fitwise.repository.packaging;

import com.fitwise.entity.packaging.CancellationDuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CancellationDurationRepository extends JpaRepository<CancellationDuration,Long> {

    List<CancellationDuration> findByIsDays(boolean isDays);

}
