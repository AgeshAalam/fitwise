package com.fitwise.repository.instructor;

import com.fitwise.entity.instructor.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location,Long> {

    List<Location> findByUserUserIdOrderByCreatedDateDesc(Long userId);

    Location findByUserUserIdAndIsDefault(Long userId, boolean isDefault);

    Location findByLocationId(Long locationId);

    Location findByUserUserIdAndLocationId(Long userId, Long locationId);


}
