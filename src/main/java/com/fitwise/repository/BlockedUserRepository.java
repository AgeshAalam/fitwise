package com.fitwise.repository;

import com.fitwise.entity.BlockedUser;
import com.fitwise.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {

    /**
     * Check if a user is blocked for a particular role.
     *
     * @param userId
     * @param role
     * @return
     */
    boolean existsByUserUserIdAndUserRoleName(Long userId, String role);

    /**
     * Delete entry for a user for a particular role
     *
     * @param userId
     * @param userRole1
     */
    void deleteByUserUserIdAndUserRole(Long userId, UserRole userRole1);

    List<BlockedUser> findByUserRoleName(String role);

    BlockedUser findByUserUserIdAndUserRoleRoleId(Long userId, Long roleId);
}
