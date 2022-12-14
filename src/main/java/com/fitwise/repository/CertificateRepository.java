package com.fitwise.repository;

import com.fitwise.entity.InstructorCertification;
import com.fitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Interface CertificateRepository.
 */
@Repository
public interface CertificateRepository extends JpaRepository<InstructorCertification,Long> {

    /**
     * Find by user.
     *
     * @param user the user
     * @return the list
     */
    List<InstructorCertification> findByUser(User user);

    Long countByUser(final User user);
    
    /**
     * Find by instructor certificate id.
     *
     * @param instructorCertificateId the instructor certificate id
     * @return the instructor certification
     */
    InstructorCertification findByUserUserIdAndInstructorCertificateId(final long userId,final long instructorCertificateId);


}
