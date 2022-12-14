package com.fitwise.specifications.jpa;

import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.entity.Gender;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.entity.Programs;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.specifications.jpa.dao.GenderDao;
import com.fitwise.specifications.jpa.dao.InstructorPayout;
import com.fitwise.specifications.jpa.dao.InstructorSubscriptionCount;
import com.fitwise.specifications.jpa.dao.SubscriptionCountByProgramType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/*
 * Created by Vignesh.G on 23/06/21
 */
@Component
public class SubscriptionAuditJPA {

    @Autowired
    private EntityManager entityManager;

    public long getUniqueUsersForProgramSubscriptionBetweenDate(String SubscriptionTypeName, List<String> status, String renewalStatus, Long programId, Date startDate, Date endDate) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<SubscriptionAudit> subscriptionAuditRoot = criteriaQuery.from(SubscriptionAudit.class);

        Expression<Long> countOfUsers = criteriaBuilder.countDistinct(subscriptionAuditRoot.get("user"));
        criteriaQuery.select(countOfUsers);

        Expression<String> subscriptionTypeExpression = subscriptionAuditRoot.get("subscriptionType").get("name");
        Predicate subscriptionTypeCriteria = criteriaBuilder.like(subscriptionTypeExpression, SubscriptionTypeName);

        Expression<String> subscriptionStatusExpression = subscriptionAuditRoot.get("subscriptionStatus").get("subscriptionStatusName");
        Predicate subscriptionStatusCriteria = subscriptionStatusExpression.in(status);

        Expression<String> renewalStatusExpression = subscriptionAuditRoot.get("renewalStatus");
        Predicate renewalStatusCriteria = criteriaBuilder.like(renewalStatusExpression, renewalStatus);

        //Program id criteria
        Expression<Long> programExpression = subscriptionAuditRoot.get("programSubscription").get("program").get("programId");
        Predicate programIdCriteria = criteriaBuilder.equal(programExpression, programId);

        //Date criteria
        Expression<Date> dateExpression = subscriptionAuditRoot.get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(subscriptionTypeCriteria, subscriptionStatusCriteria, renewalStatusCriteria, programIdCriteria, startDateCriteria, endDateCriteria);
        criteriaQuery.where(finalPredicate);

        Long count = entityManager.createQuery(criteriaQuery).getSingleResult();
        return count == null ? 0L : count.longValue();
    }

    public InstructorPayout getInstructorPayoutBySubscriptionType(Date startDate, Date endDate) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<InstructorPayout> criteriaQuery = criteriaBuilder.createQuery(InstructorPayout.class);

        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);

        Predicate subscriptionTypePredicate = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);

        Expression<Date> dateExpression = instructorPaymentRoot.get("orderManagement").get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(subscriptionTypePredicate, startDateCriteria, endDateCriteria);

        Expression<Double> totalRevenueExpression = criteriaBuilder.sum(instructorPaymentRoot.get("totalAmt"));
        Expression<Double> trainnrRevenueExpression = criteriaBuilder.sum(instructorPaymentRoot.get("fitwiseShare"));
        Expression<Double> taxExpression = criteriaBuilder.sum(instructorPaymentRoot.get("providerCharge"));
        Expression<Double> instructorShareExpression = criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare"));

        criteriaQuery.where(finalPredicate);
        criteriaQuery.multiselect(totalRevenueExpression, trainnrRevenueExpression, taxExpression, instructorShareExpression);

        return entityManager.createQuery(criteriaQuery).getSingleResult();

    }

    public Double getInstructorShareBySubscriptionType(Date startDate, Date endDate) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);

        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);

        Predicate subscriptionTypePredicate = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);

        Expression<Date> dateExpression = instructorPaymentRoot.get("orderManagement").get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(subscriptionTypePredicate, startDateCriteria, endDateCriteria);

        Expression<Double> instructorShareExpression = criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare"));

        criteriaQuery.where(finalPredicate);
        criteriaQuery.select(instructorShareExpression);

        Double share = entityManager.createQuery(criteriaQuery).getSingleResult();
        return share == null ? 0.0 : share.doubleValue();

    }

    public Double getTotalRevenueBySubscriptionType(Date startDate, Date endDate) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);

        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);

        Predicate subscriptionTypePredicate = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);

        Expression<Date> dateExpression = instructorPaymentRoot.get("orderManagement").get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(subscriptionTypePredicate, startDateCriteria, endDateCriteria);

        Expression<Double> instructorShareExpression = criteriaBuilder.sum(instructorPaymentRoot.get("totalAmt"));

        criteriaQuery.where(finalPredicate);
        criteriaQuery.select(instructorShareExpression);

        Double revenue = entityManager.createQuery(criteriaQuery).getSingleResult();
        return revenue == null ? 0.0 : revenue.doubleValue();

    }

    public Double getTotalSpentBySubscriptionType(String subscriptionTypeName, List<String> status, Date startDate, Date endDate) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);

        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);

        Predicate subscriptionTypePredicate = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), subscriptionTypeName);

        Expression<Date> dateExpression = instructorPaymentRoot.get("orderManagement").get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(subscriptionTypePredicate, startDateCriteria, endDateCriteria);

        Expression<Double> totalSpent = criteriaBuilder.sum(instructorPaymentRoot.get("totalAmt"));

        criteriaQuery.where(finalPredicate);
        criteriaQuery.select(totalSpent);

        Double spent = entityManager.createQuery(criteriaQuery).getSingleResult();
        return spent == null ? 0.0 : spent.doubleValue();
    }


    public Double getInstructorShareByOwnerUserId(String subscriptionTypeName, List<String> status, Long programOwnerUserId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);
        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);

        Predicate subscriptionTypePredicate = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), subscriptionTypeName);

        Predicate programOwnerUserIdPredicate = criteriaBuilder.equal(instructorPaymentRoot.get("instructor").get("userId"), programOwnerUserId);

        Predicate finalPredicate = criteriaBuilder.and(subscriptionTypePredicate, programOwnerUserIdPredicate);

        Expression<Double> instructorShareExpression = criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare"));

        criteriaQuery.where(finalPredicate);
        criteriaQuery.select(instructorShareExpression);

        Double share = entityManager.createQuery(criteriaQuery).getSingleResult();
        return share == null ? 0.0 : share.doubleValue();
    }

    public List<InstructorSubscriptionCount> getTotalRevenueAndSubscriptionCountByProgramTypeId(Date startDate, Date endDate) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<InstructorSubscriptionCount> criteriaQuery = criteriaBuilder.createQuery(InstructorSubscriptionCount.class);
        Root<ProgramTypes> programTypeRoot = criteriaQuery.from(ProgramTypes.class);

        Expression<Long> programTypeIdExpression = programTypeRoot.get("programTypeId");

        Subquery subquery = criteriaQuery.subquery(InstructorPayment.class);
        Root<InstructorPayment> instructorPaymentRoot = subquery.from(InstructorPayment.class);

        Predicate platformCriteria = criteriaBuilder.equal(instructorPaymentRoot.get("orderManagement").get("program").get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_TYPE).get("programTypeId"), programTypeIdExpression);
        Predicate subscriptionTypeCriteria = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);

        Expression<Date> dateExpression = instructorPaymentRoot.get("orderManagement").get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(platformCriteria, subscriptionTypeCriteria, startDateCriteria, endDateCriteria);

        Expression<Double> totalSpent = criteriaBuilder.sum(instructorPaymentRoot.get("totalAmt"));

        subquery.where(finalPredicate);
        subquery.select(totalSpent);

        //Subquery for program count
        Subquery subquery1 = criteriaQuery.subquery(Programs.class);
        Root<Programs> programsRoot = subquery1.from(Programs.class);

        Predicate programTypeExpression = criteriaBuilder.equal(programsRoot.get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_TYPE).get("programTypeId"), programTypeIdExpression);
        Predicate programStatusExpression = criteriaBuilder.like(programsRoot.get("status"), KeyConstants.KEY_PUBLISH);

        Expression<Date> dateExpressionForPrograms = programsRoot.get("createdDate");
        Predicate startDateCriteriaForPrograms = criteriaBuilder.greaterThanOrEqualTo(dateExpressionForPrograms, startDate);
        Predicate endDateCriteriaForPrograms = criteriaBuilder.lessThanOrEqualTo(dateExpressionForPrograms, endDate);

        Predicate finalPredicateForPrograms = criteriaBuilder.and(programTypeExpression, programStatusExpression, startDateCriteriaForPrograms, endDateCriteriaForPrograms);
        Expression<Long> programCount = criteriaBuilder.count(programsRoot.get("programId"));
        subquery1.where(finalPredicateForPrograms);
        subquery1.select(programCount);

        Expression<String> platformExpression = programTypeRoot.get("programTypeName");
        Expression<Double> instructorShare = subquery.getSelection();
        Expression<Long> programCountExpression = subquery1.getSelection();

        //Subquery to get instructor payment record count
        Subquery subquery2 = criteriaQuery.subquery(InstructorPayment.class);
        Root<InstructorPayment> instructorPaymentRootForSubCount = subquery2.from(InstructorPayment.class);

        Expression<Long> subscriptionCount = criteriaBuilder.count(instructorPaymentRootForSubCount.get("instructorPaymentId"));

        Predicate platformCriteriaForSubCount = criteriaBuilder.equal(instructorPaymentRootForSubCount.get("orderManagement").get("program").get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_TYPE).get("programTypeId"), programTypeIdExpression);
        Predicate subscriptionTypeCriteriaForSubCount = criteriaBuilder.like(instructorPaymentRootForSubCount.get("orderManagement").get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);

        Expression<Date> dateExpressionForSubCount = instructorPaymentRootForSubCount.get("orderManagement").get("createdDate");
        Predicate startDateCriteriaForSubCount = criteriaBuilder.greaterThanOrEqualTo(dateExpressionForSubCount, startDate);
        Predicate endDateCriteriaForSubCount = criteriaBuilder.lessThanOrEqualTo(dateExpressionForSubCount, endDate);

        Predicate finalPredicateForSubCount = criteriaBuilder.and(platformCriteriaForSubCount, subscriptionTypeCriteriaForSubCount, startDateCriteriaForSubCount, endDateCriteriaForSubCount);

        subquery2.where(finalPredicateForSubCount);
        subquery2.select(subscriptionCount);

        Expression<Long> subscriptionCountExpression = subquery2.getSelection();

        criteriaQuery.multiselect(platformExpression, programCountExpression, subscriptionCountExpression, instructorShare);
        return entityManager.createQuery(criteriaQuery).getResultList();
    }


    //For ClientDemographicsByGender ClientDemographicsByAge
    public List<Long> getUserIdListByInstructorId(List<String> status, Long programOwnerUserId, Date startDate, Date endDate) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        criteriaQuery.distinct(true);

        Root<SubscriptionAudit> subscriptionAuditRoot = criteriaQuery.from(SubscriptionAudit.class);
        Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);

        Predicate programOwnerIdPredicate = criteriaBuilder.equal(subscriptionAuditRoot.get("programSubscription").get("program").get("owner").get("userId"), programOwnerUserId);

        Predicate subscriptionTypeNamePredicate = criteriaBuilder.like(subscriptionAuditRoot.get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);

        Expression<String> subscriptionStatusExpression = subscriptionAuditRoot.get("subscriptionStatus").get("subscriptionStatusName");

        Predicate subscriptionStatusNameInPredicate = subscriptionStatusExpression.in(status);

        Expression<Date> dateExpression = subscriptionAuditRoot.get("subscriptionDate");
        Predicate startDatePredicate = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDatePredicate = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate userIdEqualPredicate = criteriaBuilder.equal(subscriptionAuditRoot.get("user").get("userId"), userProfileRoot.get("user").get("userId"));

        Predicate finalPredicate = criteriaBuilder.and(userIdEqualPredicate, programOwnerIdPredicate, subscriptionTypeNamePredicate, subscriptionStatusNameInPredicate, startDatePredicate, endDatePredicate);

        //Result set expression
        Expression<Long> userIdExpression = userProfileRoot.get("user").get("userId");

        criteriaQuery.where(finalPredicate);
        criteriaQuery.select(userIdExpression);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    //For ClientDemographicsByGender
    public List<GenderDao> getGenderByUserIdList(List<Long> userIdList){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GenderDao> criteriaQuery = criteriaBuilder.createQuery(GenderDao.class);
        Root<Gender> genderRoot = criteriaQuery.from(Gender.class);

        Subquery subquery = criteriaQuery.subquery(UserProfile.class);
        Root<UserProfile> userProfileRoot = subquery.from(UserProfile.class);

        Predicate genderIdEqualPredicate = criteriaBuilder.equal(userProfileRoot.get("gender").get("genderId"), genderRoot.get("genderId"));
        Expression<Long> userIdExpression = userProfileRoot.get("user").get("userId");
        Predicate userIdEqualPredicate = userIdExpression.in(userIdList);
        Predicate genderNotNullPredicate = criteriaBuilder.isNotNull(userProfileRoot.get("gender"));
        Predicate finalPredicate = criteriaBuilder.and(genderIdEqualPredicate, userIdEqualPredicate, genderNotNullPredicate);
        subquery.where(finalPredicate);
        subquery.select(criteriaBuilder.count(userProfileRoot));

        Expression<String> genderType = genderRoot.get("genderType");
        Expression<Long> count = subquery.getSelection();
        criteriaQuery.multiselect(genderType, count);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    //For ClientDemographicsByGender
    public Long getCountWhereGenderIsNullByUserIdList(List<Long> userIdList){
        Long count;
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

        Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);

        Expression<Long> userIdExpression = userProfileRoot.get("user").get("userId");
        Predicate userIdEqualPredicate = userIdExpression.in(userIdList);

        Predicate genderIdEqualPredicate = userProfileRoot.get("gender").isNull();

        Predicate finalPredicate = criteriaBuilder.and(genderIdEqualPredicate, userIdEqualPredicate);

        Expression<Long> userProfileCountForGenderNull = criteriaBuilder.count(userProfileRoot.get("profileId"));
        criteriaQuery.where(finalPredicate);
        criteriaQuery.select(userProfileCountForGenderNull);

        count =  entityManager.createQuery(criteriaQuery).getSingleResult();
        if(count == null){
            count = 0L;
        }
        return count;
    }

    //For ClientDemographicsByAge
    public List<String> getDOBByUserIdList(List<Long> userIdList) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);

        Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);
        Expression<Long> userIdExpression = userProfileRoot.get("user").get("userId");
        Predicate userIdEqualPredicate = userIdExpression.in(userIdList);

        Predicate finalPredicate = criteriaBuilder.and(userIdEqualPredicate);

        criteriaQuery.where(finalPredicate);
        criteriaQuery.select(userProfileRoot.get("dob"));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
