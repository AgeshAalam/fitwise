package com.fitwise.repository;

import com.fitwise.entity.NotifyEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface NotifyEmailRepository extends JpaRepository<NotifyEmail, Long> {

    NotifyEmail findByEmailAndRole(final String email, final String role);

    /**
     * List of sign up email entries between dates
     * @param startDate
     * @param endDate
     * @return
     */
    List<NotifyEmail> findByCreatedDateBetween(Date startDate, Date endDate);

}
