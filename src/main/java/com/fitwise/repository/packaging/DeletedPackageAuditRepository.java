package com.fitwise.repository.packaging;

import com.fitwise.entity.packaging.DeletedPackageAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 29/01/21
 */
@Repository
public interface DeletedPackageAuditRepository extends JpaRepository<DeletedPackageAudit, Long> {

    boolean existsBySubscriptionPackageSubscriptionPackageId(Long subscriptionPackage);
}
