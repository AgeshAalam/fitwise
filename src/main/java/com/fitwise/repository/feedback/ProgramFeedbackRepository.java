package com.fitwise.repository.feedback;

import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.ProgramFeedback;

import java.util.List;

@Repository
public interface ProgramFeedbackRepository extends JpaRepository<ProgramFeedback, Long>{

    List<ProgramFeedback> findByProgramProgramId(long programId);

    List<ProgramFeedback> findByProgram(Programs program);

    /**
     * @param user
     * @return
     */
    List<ProgramFeedback> findByUser(User user);

}
