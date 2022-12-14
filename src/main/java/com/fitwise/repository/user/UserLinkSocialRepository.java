package com.fitwise.repository.user;

import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.user.UserLinkSocial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLinkSocialRepository extends JpaRepository<UserLinkSocial, Long> {

    List<UserLinkSocial> findByUserProfile(final UserProfile userProfile);
    List<UserLinkSocial> findByUserProfileUser(final User user);
}
