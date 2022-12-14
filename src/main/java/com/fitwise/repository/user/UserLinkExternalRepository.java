package com.fitwise.repository.user;

import com.fitwise.entity.User;
import com.fitwise.entity.user.UserLinkExternal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLinkExternalRepository extends JpaRepository<UserLinkExternal, Long> {

    UserLinkExternal findByLinkId(final Long linkId);
    UserLinkExternal findByUserProfileUserAndLinkId(final User user, final Long linkId);
    List<UserLinkExternal> findByUserProfileUser(final User user);

}
