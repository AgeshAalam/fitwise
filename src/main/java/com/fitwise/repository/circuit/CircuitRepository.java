package com.fitwise.repository.circuit;

import com.fitwise.entity.Circuit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 11/05/20
 */
@Repository
public interface CircuitRepository extends JpaRepository<Circuit, Long> {

    /**
     * Get instructor's circuits list
     * @param userId
     * @return
     */
    List<Circuit> findByOwnerUserId(Long userId);

    /**
     * @param userId
     * @param pageable
     * @return
     */
    Page<Circuit> findByOwnerUserId(Long userId, Pageable pageable);

    /**
     * Find a circuit belonging to the instructor
     * @param circuitId
     * @param userId
     * @return
     */
    Circuit findByCircuitIdAndOwnerUserId(Long circuitId, Long userId);

    /**
     * Find a circuit by id
     * @param circuitId
     * @return
     */
    Circuit findByCircuitId(Long circuitId);

    /**
     * Find Circuits of instructor with a particular title
     * @param userId
     * @param circuitTitle
     * @return
     */
    List<Circuit> findByOwnerUserIdAndTitle(Long userId, String circuitTitle);

    /**Get list of Circuits of an instructor that matches a title search string
     * instructor
     * @param userId
     * @param titleSearch
     * @return
     */
    Page<Circuit> findByOwnerUserIdAndTitleIgnoreCaseContaining(Long userId, String titleSearch, Pageable pageable);
}
