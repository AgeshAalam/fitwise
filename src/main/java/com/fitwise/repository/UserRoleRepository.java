package com.fitwise.repository;

import com.fitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.UserRole;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    UserRole findByName(final String userRole);

    UserRole findByRoleId(final Long roleId);

    List<UserRole> findAll();
}