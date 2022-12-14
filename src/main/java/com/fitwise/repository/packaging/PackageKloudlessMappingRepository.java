package com.fitwise.repository.packaging;
import com.fitwise.entity.calendar.UserKloudlessMeeting;
import com.fitwise.entity.instructor.Location;
import com.fitwise.entity.packaging.PackageKloudlessMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.subscription.PackageProgramSubscription;
import com.fitwise.entity.subscription.PackageSubscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * Created by Vignesh G on 24/09/20
 */
@Repository
public interface PackageKloudlessMappingRepository extends JpaRepository<PackageKloudlessMapping, Long> {

    /**
     * @param subscriptionPackage
     * @return
     */
    List<PackageKloudlessMapping> findBySubscriptionPackage(SubscriptionPackage subscriptionPackage);


    Long countBySubscriptionPackageSubscriptionPackageId(Long subscriptionPackageId);

    PackageKloudlessMapping findTop1BySubscriptionPackageAndUserKloudlessMeetingUserKloudlessMeetingId(SubscriptionPackage subscriptionPackage, Long userKloudlessMeeting);

    List<PackageKloudlessMapping> findByUserKloudlessMeeting(UserKloudlessMeeting userKloudlessMeeting);

    List<PackageKloudlessMapping> findByLocation(Location location);

    Optional<PackageKloudlessMapping> findBySessionMappingId(Long sessionMappingId);

    int countBySubscriptionPackage(SubscriptionPackage subscriptionPackage);
    
   


}
