package com.fitwise.repository.circuit;

import com.fitwise.entity.Circuit;
import com.fitwise.entity.CircuitRepeatCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 13/05/20
 */
@Repository
public interface CircuitRepeatCountRepository extends JpaRepository<CircuitRepeatCount, Long> {

}
