package com.fitwise.repository.member;

import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.member.completion.ProgramCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 02/03/21
 */
@Repository
public interface ProgramCompletionRepository extends JpaRepository<ProgramCompletion, Long> {

    /**
     * @param member
     * @param program
     * @return
     */
    ProgramCompletion findByMemberAndProgram(User member, Programs program);

}
