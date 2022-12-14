package com.fitwise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.InstructorAwards;
import com.fitwise.entity.User;

/**
 * The Interface AwardsRepository.
 */
@Repository
public interface AwardsRepository extends JpaRepository<InstructorAwards,Long> {

    /**
     * Find by user.
     *
     * @param user the user
     * @return the list
     */
    List<InstructorAwards> findByUser(User user);
    
    /**
     * Find by awards id.
     *
     * @param awardsId the awards id
     * @return the instructor awards
     */
    InstructorAwards findByUserUserIdAndAwardsId(final long userId,final long awardsId);

}
