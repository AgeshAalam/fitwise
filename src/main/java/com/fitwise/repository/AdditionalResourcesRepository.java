package com.fitwise.repository;

import com.fitwise.entity.additionalResources.AdditionalResources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditionalResourcesRepository extends JpaRepository<AdditionalResources, Long> {
}
