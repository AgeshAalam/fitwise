package com.fitwise.repository;

import com.fitwise.entity.BlockedPrograms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockedProgramsRepository extends JpaRepository<BlockedPrograms, Long> {

    boolean existsByProgramProgramId(Long programId);

    /**
     * @param programId
     */
    void deleteByProgramProgramId(Long programId);

    /**
     * @param programId
     * @param blockType
     * @return
     */
    BlockedPrograms findByProgramProgramIdAndBlockType(Long programId, String blockType);

    /**
     * @param instructorId
     * @param blockType
     * @return
     */
    List<BlockedPrograms> findByProgramOwnerUserIdAndBlockType(Long instructorId, String blockType);

    /**
     * @param programIdList
     * @param blockType
     * @return
     */
    List<BlockedPrograms> findByProgramProgramIdInAndBlockType(List<Long> programIdList, String blockType);

}
