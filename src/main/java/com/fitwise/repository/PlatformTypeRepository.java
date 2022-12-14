package com.fitwise.repository;

import com.fitwise.entity.PlatformType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Interface PlatformTypeRepository.
 */
@Repository
public interface PlatformTypeRepository extends JpaRepository<PlatformType, Long> {

    List<PlatformType> findAll();

    PlatformType findByPlatformTypeId(Long platformId);

    /**
     * @param platform
     * @return
     */
    PlatformType findByPlatform(String platform);

}
