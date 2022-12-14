package com.fitwise.repository;

import com.fitwise.entity.ProgramRating;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ProgramRatingRepository extends JpaRepository<ProgramRating, Long> {


    List<ProgramRating> findByProgram(Programs program);

    /**
     * @param programId
     * @return
     */
    List<ProgramRating> findByProgramProgramId(Long programId);

    /**
     * @param program
     * @param user
     * @return
     */
    ProgramRating findByProgramAndUser(Programs program, User user);

    List<ProgramRating> findByProgramAndModifiedDateBetween(Programs program, Date startDate, Date endDate);

    /**
     * @param user
     * @return
     */
    List<ProgramRating> findByUser(User user);
}
