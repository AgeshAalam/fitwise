package com.fitwise.repository;

import com.fitwise.entity.Promotions;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long>, JpaSpecificationExecutor<UserProfile> {

    UserProfile findByUser(User user);

    UserProfile findByUserUserId(final Long userId);

    List<UserProfile> findByFirstNameIgnoreCaseContaining(String user);

    @Query("SELECT up from UserProfile up join up.user us on us.userId = up.user.userId " +
            "join UserRoleMapping urm on us.userId = urm.user.userId " +
            "join UserRole ur on ur.roleId = urm.userRole.roleId " +
            "where ur.name=:role and  concat(up.firstName , up.lastName) like %:searchString%")
    List<UserProfile> findByName(@Param("searchString") String searchString , @Param("role") String role);

    @Query("SELECT up from UserProfile up join up.user us on us.userId = up.user.userId " +
            "join UserRoleMapping urm on us.userId = urm.user.userId " +
            "join UserRole ur on ur.roleId = urm.userRole.roleId " +
            "where ur.name=:role and  concat(up.firstName , ' ', up.lastName) like %:searchString%")
    List<UserProfile> findByFirstNameAndLastName(@Param("searchString") String searchString , @Param("role") String role);

    int countByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Find User Profile list for a list of users
     *
     * @param users
     * @return
     */
    List<UserProfile> findByUserIn(List<User> users);

    UserProfile findByUserAndPromotion(User user, Promotions promotions);

}
