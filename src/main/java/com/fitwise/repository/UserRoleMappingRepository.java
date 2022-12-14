package com.fitwise.repository;

import com.fitwise.entity.User;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, Long> {

    List<UserRoleMapping> findByUserRoleRoleId(final long roleId);

    List<UserRoleMapping> findByUserRoleName(String role);

    List<UserRoleMapping> findByUserUserIdAndUserRoleName(final long userId,String userRole);

    //AKHIL
    List<UserRoleMapping> findByUser(User user);

    List<UserRoleMapping> findByUserUserId(Long userId);

    UserRoleMapping findTop1ByUserUserIdAndUserRoleName(final long userId,String userRole);

    int countByUserRoleNameAndCreatedDateBetween(String role, Date startDate, Date endDate);

}
