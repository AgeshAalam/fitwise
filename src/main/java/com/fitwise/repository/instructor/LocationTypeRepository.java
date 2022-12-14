package com.fitwise.repository.instructor;

import com.fitwise.entity.instructor.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationTypeRepository extends JpaRepository<LocationType,Long> {

    LocationType findByLocationTypeId(Long locationTypeId);

}
