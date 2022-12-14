package com.fitwise.repository.calendar;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fitwise.entity.calendar.Zone;

@Repository
public interface ZoneRepository extends JpaRepository<Zone,Long> {

}

