package com.fitwise.repository;

import com.fitwise.entity.User;
import com.fitwise.entity.UserCommunicationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 21/09/20
 */
@Repository
public interface UserCommunicationDetailRepository extends JpaRepository<UserCommunicationDetail, Long> {

    /**
     * @param user
     * @return
     */
    UserCommunicationDetail findByUser(User user);

}
