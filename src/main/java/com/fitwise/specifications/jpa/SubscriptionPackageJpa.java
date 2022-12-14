package com.fitwise.specifications.jpa;

import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.packaging.PackageExternalClientMapping;
import com.fitwise.entity.packaging.PackageKloudlessMapping;
import com.fitwise.entity.packaging.PackageMemberMapping;
import com.fitwise.entity.packaging.PackageOfferMapping;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.properties.StripeProperties;
import com.fitwise.specifications.jpa.dao.OfferCountDao;
import com.fitwise.specifications.jpa.dao.OfferDao;
import com.fitwise.specifications.jpa.dao.PackageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SubscriptionPackageJpa {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private StripeProperties stripeProperties;

    /**
     * Get List of packages of an instructor
     *
     * @param instructorId
     * @param member
     * @param pageNo
     * @param pageSize
     * @return
     */
    public List<PackageDao> getInstructorPackages(Long instructorId, User member, int pageNo, int pageSize, Optional<String> search) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PackageDao> criteriaQuery = criteriaBuilder.createQuery(PackageDao.class);
        Root<SubscriptionPackage> subscriptionPackageRoot = criteriaQuery.from(SubscriptionPackage.class);
        Predicate nonRestrictedPredicate = criteriaBuilder.isFalse(subscriptionPackageRoot.get("isRestrictedAccess"));
        Predicate instructorIdPredicate = criteriaBuilder.equal(subscriptionPackageRoot.get("owner").get("userId"), instructorId);
        Predicate statusPredicate = criteriaBuilder.like(subscriptionPackageRoot.get("status"), KeyConstants.KEY_PUBLISH);
        // Get program count
        Subquery packageProgramSubquery = criteriaQuery.subquery(PackageProgramMapping.class);
        Root<PackageProgramMapping> packageProgramMappingRoot = packageProgramSubquery.from(PackageProgramMapping.class);
        Predicate packagePredicateForProgram = criteriaBuilder.equal(packageProgramMappingRoot.get("subscriptionPackage").
                get("subscriptionPackageId"), subscriptionPackageRoot.get("subscriptionPackageId"));
        packageProgramSubquery.where(packagePredicateForProgram);
        packageProgramSubquery.select(criteriaBuilder.count(packageProgramMappingRoot));
        // Get session count
        Subquery packageSessionSubquery = criteriaQuery.subquery(PackageKloudlessMapping.class);
        Root<PackageKloudlessMapping> packageKloudlessMappingRoot = packageSessionSubquery.from(PackageKloudlessMapping.class);
        Predicate packagePredicateForSession = criteriaBuilder.equal(packageKloudlessMappingRoot.get("subscriptionPackage").
                get("subscriptionPackageId"), subscriptionPackageRoot.get("subscriptionPackageId"));
        packageSessionSubquery.where(packagePredicateForSession);
        packageSessionSubquery.select(criteriaBuilder.sum(packageKloudlessMappingRoot.get("totalSessionCount")));
        Predicate accessPredicate = constructMemberAccessPredicate(criteriaQuery, criteriaBuilder, subscriptionPackageRoot, member, nonRestrictedPredicate);
        Predicate finalPredicate = criteriaBuilder.and(accessPredicate, instructorIdPredicate, statusPredicate);

        Expression<String> title = subscriptionPackageRoot.get("title");
        //Search by package title
        if (search.isPresent() && !search.get().isEmpty()) {
            Predicate searchPredicate = criteriaBuilder.like(title, "%" + search.get() + "%");
            finalPredicate = criteriaBuilder.and(finalPredicate, searchPredicate);
        }
        criteriaQuery.where(finalPredicate);
        Expression<Long> subscriptionPackageId = subscriptionPackageRoot.get("subscriptionPackageId");
        Expression<String> status = subscriptionPackageRoot.get("status");
        Expression<Long> duration = subscriptionPackageRoot.get("packageDuration").get("duration");
        Expression<String> imageUrl = subscriptionPackageRoot.get("image").get("imagePath");
        Expression<Double> price = subscriptionPackageRoot.get("price");
        Expression<Long> programCount = packageProgramSubquery.getSelection();
        Expression<Long> sessionCount = packageSessionSubquery.getSelection();
        criteriaQuery.multiselect(subscriptionPackageId, title, status, duration, imageUrl, programCount, sessionCount, price);
        return entityManager.createQuery(criteriaQuery).setFirstResult(pageNo * pageSize).setMaxResults(pageSize).getResultList();

    }

    private Predicate constructMemberAccessPredicate(CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder, Root<SubscriptionPackage> subscriptionPackageRoot, User member, Predicate nonRestrictedPredicate){
        Predicate accessPredicate;
        if(member != null){
            Subquery packageMemberSubquery = criteriaQuery.subquery(PackageMemberMapping.class);
            Root<PackageMemberMapping> packageMemberMappingRoot = packageMemberSubquery.from(PackageMemberMapping.class);
            Predicate packageIdPredicate = criteriaBuilder.equal(packageMemberMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"),
                    subscriptionPackageRoot.get("subscriptionPackageId"));
            Predicate memberIdPredicate = criteriaBuilder.equal(packageMemberMappingRoot.get("user").get("userId"), member.getUserId());
            packageMemberSubquery.where(criteriaBuilder.and(packageIdPredicate, memberIdPredicate));
            packageMemberSubquery.select(packageMemberMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"));
            Subquery packageExternalClientSubquery = criteriaQuery.subquery(PackageExternalClientMapping.class);
            Root<PackageExternalClientMapping> packageExternalClientMappingRoot = packageExternalClientSubquery.from(PackageExternalClientMapping.class);
            Predicate packagePredicate = criteriaBuilder.equal(packageExternalClientMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"),
                    subscriptionPackageRoot.get("subscriptionPackageId"));
            Predicate memberPredicate = criteriaBuilder.like(packageExternalClientMappingRoot.get("externalClientEmail"), member.getEmail());
            packageExternalClientSubquery.where(criteriaBuilder.and(packagePredicate, memberPredicate));
            packageExternalClientSubquery.select(packageExternalClientMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"));
            Predicate clientExistPredicate = criteriaBuilder.and(criteriaBuilder.exists(packageMemberSubquery),
                    criteriaBuilder.equal(subscriptionPackageRoot.get("subscriptionPackageId"), packageMemberSubquery.getSelection()));
            Predicate externalClientExistPredicate = criteriaBuilder.and(criteriaBuilder.exists(packageExternalClientSubquery),
                    criteriaBuilder.equal(subscriptionPackageRoot.get("subscriptionPackageId"), packageExternalClientSubquery.getSelection()));
            accessPredicate = criteriaBuilder.or(clientExistPredicate, externalClientExistPredicate, nonRestrictedPredicate);
        }else{
            accessPredicate = nonRestrictedPredicate;
        }
        return accessPredicate;
    }

    /**
     * Get count of instructor packages
     *
     * @param instructorId
     * @param member
     * @return
     */
    public Long getInstructorPackagesCount(Long instructorId, User member, Optional<String> search) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<SubscriptionPackage> subscriptionPackageRoot = criteriaQuery.from(SubscriptionPackage.class);
        Predicate nonRestrictedPredicate = criteriaBuilder.isFalse(subscriptionPackageRoot.get("isRestrictedAccess"));
        Predicate instructorIdPredicate = criteriaBuilder.equal(subscriptionPackageRoot.get("owner").get("userId"), instructorId);
        Predicate statusPredicate = criteriaBuilder.like(subscriptionPackageRoot.get("status"), KeyConstants.KEY_PUBLISH);
        Predicate accessPredicate = constructMemberAccessPredicate(criteriaQuery, criteriaBuilder, subscriptionPackageRoot, member, nonRestrictedPredicate);
        Predicate finalPredicate = criteriaBuilder.and(accessPredicate, instructorIdPredicate, statusPredicate);
        //Search by package title
        if (search.isPresent() && !search.get().isEmpty()) {
            Predicate searchPredicate = criteriaBuilder.like(subscriptionPackageRoot.get("title"), "%" + search.get() + "%");
            finalPredicate = criteriaBuilder.and(finalPredicate, searchPredicate);
        }
        criteriaQuery.where(finalPredicate);
        criteriaQuery.select(criteriaBuilder.count(subscriptionPackageRoot));
        return entityManager.createQuery(criteriaQuery).getSingleResult();
    }

    /**
     * Get no of current available offers for package for member
     *
     * @param subscriptionPackageIds
     * @param member
     * @return
     */
    public Map<Long, Long> getNumberOfCurrentOffersForMemberPackages(List<Long> subscriptionPackageIds, User member) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<OfferDao> criteriaQuery = criteriaBuilder.createQuery(OfferDao.class);
        Root<SubscriptionPackage> subscriptionPackageRoot = criteriaQuery.from(SubscriptionPackage.class);
        Predicate idInPredicate = subscriptionPackageRoot.get("subscriptionPackageId").in(subscriptionPackageIds);
        Date date = new Date();
        //New offer count sub query
        Subquery packageNewOfferSubquery = criteriaQuery.subquery(PackageOfferMapping.class);
        Root<PackageOfferMapping> packageNewOfferMappingRoot = packageNewOfferSubquery.from(PackageOfferMapping.class);
        Predicate subscriptionPackagePredicate = criteriaBuilder.equal(packageNewOfferMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"),
                subscriptionPackageRoot.get("subscriptionPackageId"));
        Predicate isInUsePredicate = criteriaBuilder.isTrue(packageNewOfferMappingRoot.get("offerCodeDetail").get("isInUse"));
        Predicate offerStatusPredicate = criteriaBuilder.like(packageNewOfferMappingRoot.get("offerCodeDetail").get("offerStatus"), DiscountsConstants.OFFER_ACTIVE);
        Predicate startDatePredicate = criteriaBuilder.lessThanOrEqualTo(packageNewOfferMappingRoot.get("offerCodeDetail").get("offerStartingDate"), date);
        Predicate endDatePredicate = criteriaBuilder.greaterThanOrEqualTo(packageNewOfferMappingRoot.get("offerCodeDetail").get("offerEndingDate"), date);
        Predicate isNewPredicate = criteriaBuilder.isTrue(packageNewOfferMappingRoot.get("offerCodeDetail").get("isNewUser"));
        packageNewOfferSubquery.where(criteriaBuilder.and(subscriptionPackagePredicate, isInUsePredicate, offerStatusPredicate,
                startDatePredicate, endDatePredicate, isNewPredicate));
        packageNewOfferSubquery.select(criteriaBuilder.count(packageNewOfferMappingRoot));
        //Existing offer count sub query
        Subquery packageExistingOfferSubquery = criteriaQuery.subquery(PackageOfferMapping.class);
        Root<PackageOfferMapping> packageExistingOfferMappingRoot = packageExistingOfferSubquery.from(PackageOfferMapping.class);
        Predicate subscriptionPackagePredicateExisting = criteriaBuilder.equal(packageExistingOfferMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"),
                subscriptionPackageRoot.get("subscriptionPackageId"));
        Predicate isInUsePredicateForExisting = criteriaBuilder.isTrue(packageExistingOfferMappingRoot.get("offerCodeDetail").get("isInUse"));
        Predicate offerStatusPredicateForExisting = criteriaBuilder.like(packageExistingOfferMappingRoot.get("offerCodeDetail").get("offerStatus"), DiscountsConstants.OFFER_ACTIVE);
        Predicate startDatePredicateForExisting = criteriaBuilder.lessThanOrEqualTo(packageExistingOfferMappingRoot.get("offerCodeDetail").get("offerStartingDate"), date);
        Predicate endDatePredicateForExisting = criteriaBuilder.greaterThanOrEqualTo(packageExistingOfferMappingRoot.get("offerCodeDetail").get("offerEndingDate"), date);
        Predicate isExistingPredicate = criteriaBuilder.isFalse(packageExistingOfferMappingRoot.get("offerCodeDetail").get("isNewUser"));
        packageExistingOfferSubquery.where(criteriaBuilder.and(subscriptionPackagePredicateExisting, isInUsePredicateForExisting, offerStatusPredicateForExisting,
                startDatePredicateForExisting, endDatePredicateForExisting, isExistingPredicate));
        packageExistingOfferSubquery.select(criteriaBuilder.count(packageExistingOfferMappingRoot));
        Subquery packageSubscriptionSubquery = criteriaQuery.subquery(PackageSubscription.class);
        Root<PackageSubscription> packageSubscriptionRoot = packageSubscriptionSubquery.from(PackageSubscription.class);
        Subquery packageSubscriptionStatusSubquery = criteriaQuery.subquery(PackageSubscription.class);
        Root<PackageSubscription> packageSubscriptionStatusRoot = packageSubscriptionStatusSubquery.from(PackageSubscription.class);
        if (member != null) {
            //Criteria to check member has history of subscription
            Predicate packageIdPredicate = criteriaBuilder.equal(packageSubscriptionRoot.get("subscriptionPackage").get("subscriptionPackageId"),
                    subscriptionPackageRoot.get("subscriptionPackageId"));
            Predicate memberIdPredicate = criteriaBuilder.equal(packageSubscriptionRoot.get("user").get("userId"), member.getUserId());
            packageSubscriptionSubquery.where(criteriaBuilder.and(packageIdPredicate, memberIdPredicate));
            packageSubscriptionSubquery.select(criteriaBuilder.count(packageSubscriptionRoot));
            //Criteria to check subscription status
            Predicate packagePredicate = criteriaBuilder.equal(packageSubscriptionStatusRoot.get("subscriptionPackage").get("subscriptionPackageId"),
                    subscriptionPackageRoot.get("subscriptionPackageId"));
            Predicate memberPredicate = criteriaBuilder.equal(packageSubscriptionStatusRoot.get("user").get("userId"), member.getUserId());
            List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
            Predicate statusPredicate = packageSubscriptionStatusRoot.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);
            int bufferMinutes = stripeProperties.getSubscriptionExpiryBufferMinutes();
            LocalDateTime now = LocalDateTime.now();
            now = now.minusMinutes(bufferMinutes);
            Timestamp currentDateWithBuffer = new Timestamp(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).getTime());
            Expression<Date> subscribedDate = packageSubscriptionStatusRoot.get("subscribedDate");
            Expression<Integer> days = packageSubscriptionStatusRoot.get("subscriptionPlan").get("duration");
            Expression<Timestamp> newDate = criteriaBuilder.function("ADDDATE", Timestamp.class, subscribedDate, days);
            Predicate durationPredicate = criteriaBuilder.greaterThanOrEqualTo(newDate, currentDateWithBuffer);
            packageSubscriptionStatusSubquery.where(criteriaBuilder.and(packagePredicate, memberPredicate, statusPredicate));
            Expression<Object> status = criteriaBuilder.selectCase()
                    .when(durationPredicate, "Paid")
                    .otherwise("Expired");
            packageSubscriptionStatusSubquery.select(status);
        }
        criteriaQuery.where(idInPredicate);
        Expression<Long> subscriptionCount;
        Expression<String> subscriptionStatus;
        if (member == null) {
            subscriptionCount = criteriaBuilder.literal(0L);
            subscriptionStatus = criteriaBuilder.literal("notApplicable");
        } else {
            subscriptionCount = packageSubscriptionSubquery.getSelection();
            subscriptionStatus = packageSubscriptionStatusSubquery.getSelection();
        }
        Expression<Long> packageId = subscriptionPackageRoot.get("subscriptionPackageId");
        Expression<Long> newOfferCount = packageNewOfferSubquery.getSelection();
        Expression<Long> existingOfferCount = packageExistingOfferSubquery.getSelection();
        criteriaQuery.multiselect(packageId, subscriptionCount, subscriptionStatus, newOfferCount, existingOfferCount);
        List<OfferDao> offerDaoList = entityManager.createQuery(criteriaQuery).getResultList();
        Map<Long, Long> offerCountMap = new HashMap<>();
        for (OfferDao offerDao : offerDaoList) {
            long offerCount;
            if (member == null || offerDao.getSubscriptionCount() == 0) {
                offerCount = offerDao.getNewOfferCount();
            } else {
                if (offerDao.getStatus().equalsIgnoreCase(KeyConstants.KEY_PAID)) {
                    offerCount = 0;
                } else {
                    offerCount = offerDao.getExistingOfferCount();
                }
            }
            offerCountMap.put(offerDao.getSubscriptionPackageId(), offerCount);
        }
        return offerCountMap;
    }

    /**
     * Get no of current available offers for package for instructor
     *
     * @param subscriptionPackageIdList
     * @return List<OfferCountDao>
     */
    public List<OfferCountDao> getNumberOfCurrentOffersForInstructorPackages(List<Long> subscriptionPackageIdList) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<OfferCountDao> criteriaQuery = criteriaBuilder.createQuery(OfferCountDao.class);
        Root<SubscriptionPackage> subscriptionPackageRoot = criteriaQuery.from(SubscriptionPackage.class);
        Date date = new Date();

        //Sub query to get active offer count
        Subquery<Long> subQuery1 = criteriaQuery.subquery(Long.class);
        Root<PackageOfferMapping> packageOfferMappingRoot = subQuery1.from(PackageOfferMapping.class);

        Predicate subscriptionPackageIdEqualPredicate = criteriaBuilder.equal(packageOfferMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"), subscriptionPackageRoot.get("subscriptionPackageId"));
        Predicate offerCodeDetailsIdInUserPredicate = criteriaBuilder.isTrue(packageOfferMappingRoot.get("offerCodeDetail").get("isInUse"));
        Predicate offerStatusPredicate = criteriaBuilder.like(packageOfferMappingRoot.get("offerCodeDetail").get("offerStatus"), DiscountsConstants.OFFER_ACTIVE);
        Predicate startDatePredicate = criteriaBuilder.lessThanOrEqualTo(packageOfferMappingRoot.get("offerCodeDetail").get("offerStartingDate"), date);
        Predicate endDatePredicate = criteriaBuilder.greaterThanOrEqualTo(packageOfferMappingRoot.get("offerCodeDetail").get("offerEndingDate"), date);

        Predicate finalPredicate = criteriaBuilder.and(subscriptionPackageIdEqualPredicate, offerCodeDetailsIdInUserPredicate, offerStatusPredicate,
                startDatePredicate, endDatePredicate);

        //Result set expression for sub query 1
        Expression<Long> offerCount = criteriaBuilder.count(packageOfferMappingRoot);

        subQuery1.where(finalPredicate);
        subQuery1.select(offerCount);

        Predicate subscriptionPackageIdPredicate = subscriptionPackageRoot.get("subscriptionPackageId").in(subscriptionPackageIdList);

        //Result set expression
        Expression<Long> subscriptionPackageIdExpression = subscriptionPackageRoot.get("subscriptionPackageId");
        Expression<Long> offerCountExpression = subQuery1.getSelection();

        criteriaQuery.where(subscriptionPackageIdPredicate);
        criteriaQuery.multiselect(subscriptionPackageIdExpression, offerCountExpression);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public Integer getBookableSessionCountForPackage(Long subscriptionPackageId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
        Root<PackageKloudlessMapping> packageKloudlessMappingRoot = criteriaQuery.from(PackageKloudlessMapping.class);
        Predicate idPredicate = criteriaBuilder.equal(packageKloudlessMappingRoot.get("subscriptionPackage").get("subscriptionPackageId"), subscriptionPackageId);
        criteriaQuery.where(idPredicate);
        criteriaQuery.select(criteriaBuilder.sum(packageKloudlessMappingRoot.get("totalSessionCount")));

        Integer count = entityManager.createQuery(criteriaQuery).getSingleResult();
        if(count == null){
            return  0;
        }
        return count;
    }
}