package com.fitwise.repository.socialLogin;

import com.fitwise.entity.social.AppleAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppleAuthRepository extends JpaRepository<AppleAuthentication, Long> {
    AppleAuthentication findByAppleUserId(String appleUserId);

    AppleAuthentication findByAppleUserIdAndUserRole(String appleUserId, String userRole);

    AppleAuthentication findTop1ByAppleUserId(String appleUserId);

    AppleAuthentication findByUserUserIdAndUserRole(Long userId, String userRole);
}
