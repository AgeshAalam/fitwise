package com.fitwise.program.daoImpl;

import com.fitwise.entity.Programs;
import com.fitwise.repository.ProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * The Class ProgramsRepoImpl.
 */
@Service
public class ProgramsRepoImpl {

    /**
     * The program repo.
     */
    @Autowired
    private ProgramRepository programRepo;

    /**
     * Save program.
     *
     * @param program the program
     * @return the programs
     */
    public Programs saveProgram(Programs program) {
        return programRepo.save(program);
    }

    /**
     * Gets the program.
     *
     * @param programId the program id
     * @return the program
     */
    public Programs getProgram(Long programId) {
        return programRepo.findByProgramId(programId);
    }

    /**
     * Find by program id and owner user id.
     *
     * @param programId the program id
     * @param userId    the user id
     * @return the list
     */
    public Programs findByProgramIdAndOwnerUserId(final Long programId, final Long userId) {
        return programRepo.findByProgramIdAndOwnerUserId(programId, userId);
    }

    /**
     * Find by program type id.
     *
     * @param programTypeId the program type id
     * @return the list
     */
    public List<Programs> findByProgramTypeId(final Long programTypeId) {
        return programRepo.findByProgramTypeProgramTypeId(programTypeId);
    }

    /**
     * Gets program by owner and title.
     *
     * @param userId
     * @param title
     * @return Program by owner and title
     */
    public Programs findByOwnerUserIdAndTitle(long userId, String title) {
        return programRepo.findByOwnerUserIdAndTitleIgnoreCase(userId, title);
    }

}
