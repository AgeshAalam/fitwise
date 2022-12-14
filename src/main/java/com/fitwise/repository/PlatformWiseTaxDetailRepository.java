package com.fitwise.repository;

import com.fitwise.entity.PlatformType;
import com.fitwise.entity.PlatformWiseTaxDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Interface PlatformWiseTaxDetailRepository.
 */
@Repository
public interface PlatformWiseTaxDetailRepository extends JpaRepository<PlatformWiseTaxDetail, Long> {

    /**
     * Find by active and platform type in.
     *
     * @param status the status
     * @param platformTypes the platform types
     * @return the list
     */
    List<PlatformWiseTaxDetail> findByActiveAndPlatformTypeIn(boolean status, List<PlatformType> platformTypes);

    PlatformWiseTaxDetail findByActiveAndPlatformType(boolean status, PlatformType platformType);

    PlatformWiseTaxDetail findByActiveAndPlatformTypePlatform(boolean status, String platformType);

    PlatformWiseTaxDetail findByPlatformWiseTaxDetailId(Long platformWiseTaxDetailId);

}
