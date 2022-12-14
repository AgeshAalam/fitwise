package com.fitwise.repository.socialLogin;

import com.fitwise.entity.social.FacebookAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacebookAuthRepository extends JpaRepository<FacebookAuthentication, Long> {
    FacebookAuthentication findTop1ByFacebookUserProfileId(String userProfileId);

    FacebookAuthentication findTop1ByFacebookUserProfileIdAndUserRole(String userProfileId, String userRole);
    
    FacebookAuthentication findByFacebookUserProfileIdAndUserRole(String userProfileId, String userRole);

    FacebookAuthentication findTop1ByEmailAndUserRole(String email, String userRole);

    FacebookAuthentication findByEmailAndUserRole(String email, String userRole);
}
