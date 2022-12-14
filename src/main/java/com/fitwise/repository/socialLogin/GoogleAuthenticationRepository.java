package com.fitwise.repository.socialLogin;

import com.fitwise.entity.social.GoogleAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 08/07/20
 */
@Repository
public interface GoogleAuthenticationRepository extends JpaRepository<GoogleAuthentication, Long> {
    GoogleAuthentication findByUserUserIdAndUserRole(Long userId, String userRole);
}
