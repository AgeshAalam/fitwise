package com.fitwise.repository.packaging;

import com.fitwise.entity.packaging.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Created by Vignesh G on 24/09/20
 */
public interface SessionTypeRepository extends JpaRepository<SessionType, Long> {

    /**
     * @param sessionType
     * @return
     */
    SessionType findBySessionType(String sessionType);


}
