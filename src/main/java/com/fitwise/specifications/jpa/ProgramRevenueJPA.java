package com.fitwise.specifications.jpa;

import com.fitwise.constants.KeyConstants;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.specifications.jpa.dao.RevenueByPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProgramRevenueJPA {
    private final EntityManager entityManager;

    public List<RevenueByPlatform> getProgramRevenue(long programId, Date startDate, Date endDate) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<RevenueByPlatform> criteriaQuery = criteriaBuilder.createQuery(RevenueByPlatform.class);

        Root<PlatformType> platformTypeRoot = criteriaQuery.from(PlatformType.class);
        Expression<Long> platFormIdExpression = platformTypeRoot.get("platformTypeId");

        Subquery nestedQuery = criteriaQuery.subquery(InstructorPayment.class);
        Root<InstructorPayment> instructorPaymentRoot = nestedQuery.from(InstructorPayment.class);
        Root<SubscriptionAudit> subscriptionAuditRoot = nestedQuery.from(SubscriptionAudit.class);

        Predicate platformCriteria = criteriaBuilder.equal(instructorPaymentRoot.get("orderManagement").get("subscribedViaPlatform").get("platformTypeId"), platFormIdExpression);
        Predicate susbcriptionTypeCriteria = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);
        Predicate programSubsciptionCriteria = criteriaBuilder.equal(subscriptionAuditRoot.get("programSubscription").get("program").get("programId"), programId);


        //Date criteria
        Expression<Date> dateExpression = instructorPaymentRoot.get("orderManagement").get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);
        Predicate fieldEqualCriteria = criteriaBuilder.equal(subscriptionAuditRoot.get("programSubscriptionPaymentHistory").get("orderManagement").get("id"), instructorPaymentRoot.get("orderManagement").get("id"));

        Predicate finalPredicate = criteriaBuilder.and(platformCriteria, susbcriptionTypeCriteria, programSubsciptionCriteria, startDateCriteria, endDateCriteria, fieldEqualCriteria);
        nestedQuery.where(finalPredicate);

        Expression<Double> revenue = criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare"));
        nestedQuery.select(revenue);

        Expression<String> platformExpression = platformTypeRoot.get("platform");
        Expression<Double> instructorShare = nestedQuery.getSelection();

        criteriaQuery.multiselect(platformExpression, instructorShare);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }


    public double getAnalyticsProgramRevenue(long programId) {

        Double instructorShare;
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);

        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);
        Root<SubscriptionAudit> subscriptionAuditRoot = criteriaQuery.from(SubscriptionAudit.class);


        Predicate programCriteria = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);
        ;
        Predicate programSubsciptionCriteria = criteriaBuilder.equal(subscriptionAuditRoot.get("programSubscription").get("program").get("programId"), programId);


        Predicate fieldEqualCriteria = criteriaBuilder.equal(subscriptionAuditRoot.get("programSubscriptionPaymentHistory").get("orderManagement").get("id"), instructorPaymentRoot.get("orderManagement").get("id"));

        Predicate finalPredicate = criteriaBuilder.and(programCriteria, programSubsciptionCriteria, fieldEqualCriteria);
        criteriaQuery.where(finalPredicate);

        criteriaQuery.select(criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare")));
        instructorShare = entityManager.createQuery(criteriaQuery).getSingleResult();
        if (instructorShare == null) {
            instructorShare = 0.0;
        }
        return instructorShare;
    }

    public double getAnalyticsProgramRevenue(long programId, String renewalStatus, Date startTime, Date endTime) {

        Double instructorShare;
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);

        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);
        Root<SubscriptionAudit> subscriptionAuditRoot = criteriaQuery.from(SubscriptionAudit.class);


        Predicate programCriteria = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);
        Predicate programSubsciptionCriteria = criteriaBuilder.equal(subscriptionAuditRoot.get("programSubscription").get("program").get("programId"), programId);

        Predicate subscriptionRenewalCriteria = criteriaBuilder.like(subscriptionAuditRoot.get("renewalStatus"), renewalStatus);

        //Date criteria
        Expression<Date> dateExpression = instructorPaymentRoot.get("orderManagement").get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startTime);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endTime);

        Predicate fieldEqualCriteria = criteriaBuilder.equal(subscriptionAuditRoot.get("programSubscriptionPaymentHistory").get("orderManagement").get("id"), instructorPaymentRoot.get("orderManagement").get("id"));

        Predicate finalPredicate = criteriaBuilder.and(programCriteria, programSubsciptionCriteria, fieldEqualCriteria, subscriptionRenewalCriteria, startDateCriteria, endDateCriteria);
        criteriaQuery.where(finalPredicate);

        criteriaQuery.select(criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare")));
        instructorShare = entityManager.createQuery(criteriaQuery).getSingleResult();
        if (instructorShare == null) {
            instructorShare = 0.0;
        }
        return instructorShare;
    }

}
