package com.fitwise.repository.fcm;

import com.fitwise.entity.User;
import com.fitwise.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    /**
     * @param user
     * @param fcmToken
     * @return
     */
    List<UserFcmToken> findByUserAndFcmtoken(User user, String fcmToken);

    /**
     * @param user
     * @return
     */
    List<UserFcmToken> findByUser(User user);

}
