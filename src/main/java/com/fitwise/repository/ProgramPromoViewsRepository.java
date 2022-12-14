package com.fitwise.repository;

import com.fitwise.entity.ProgramPromoViews;
import com.fitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ProgramPromoViewsRepository extends JpaRepository<ProgramPromoViews, Long>, JpaSpecificationExecutor<ProgramPromoViews> {

    @Query(value = "Select ppcs from ProgramPromoViews ppcs where ppcs.user in :users And program.programId = :programId group by user")
    List<ProgramPromoViews> findUniqueProgramPromoCompletion(@Param("users") List<User> users , @Param("programId") Long programId);

}
