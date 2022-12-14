package com.fitwise.repository.packaging;

import com.fitwise.entity.packaging.SessionCountPerWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionCountPerWeekRepository extends JpaRepository<SessionCountPerWeek,Long> {
}
