package com.fitwise.repository.packaging;

import com.fitwise.entity.Programs;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 24/09/20
 */
@Repository
public interface PackageProgramMappingRepository extends JpaRepository<PackageProgramMapping, Long> {

    /**
     * @param subscriptionPackage
     * @return
     */
    List<PackageProgramMapping> findBySubscriptionPackage(SubscriptionPackage subscriptionPackage);

    /**
     * @param subscriptionPackage
     * @param program
     * @return
     */
    PackageProgramMapping findTop1BySubscriptionPackageAndProgram(SubscriptionPackage subscriptionPackage, Programs program);

    Long countBySubscriptionPackageSubscriptionPackageId(Long subscriptionPackageId);

    List<PackageProgramMapping> findByProgram(Programs programs);

    /**
     * @param programs
     * @param status
     * @return
     */
    List<PackageProgramMapping> findByProgramAndSubscriptionPackageStatus(Programs programs, String status);

    /**
     * Delete by package program id.
     *
     * @param packageProgramId the package program id
     */
    void deleteByPackageProgramId(final Long packageProgramId);

    /**
     * Count by subscription package.
     *
     * @param subscriptionPackage the subscription package
     * @return the int
     */
    int countBySubscriptionPackage(SubscriptionPackage subscriptionPackage);

    /**
     * Find by program program id.
     *
     * @param programId the program id
     * @return the list
     */
    List<PackageProgramMapping> findByProgramProgramId(Long programId);

}
