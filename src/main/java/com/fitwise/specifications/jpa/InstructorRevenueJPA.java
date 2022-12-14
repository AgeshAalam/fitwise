package com.fitwise.specifications.jpa;

import com.fitwise.constants.KeyConstants;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.subscription.SubscriptionAudit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InstructorRevenueJPA {

    private final EntityManager entityManager;

    public double getInstructorRevenue(Date startDate, Date endDate, long userId, String renewalStatus, List<String> statusList) throws ParseException {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);

        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);
        Root<SubscriptionAudit> subscriptionAuditRoot = criteriaQuery.from(SubscriptionAudit.class);


        Predicate programCriteria = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);
        Predicate subscriptionRenewalCriteria = criteriaBuilder.like(subscriptionAuditRoot.get("renewalStatus"), renewalStatus);
        Predicate instructorCriteria = criteriaBuilder.equal(subscriptionAuditRoot.get("programSubscription").get("program").get("owner").get("userId"), userId);

        //Date criteria
        Expression<Date> dateExpression = instructorPaymentRoot.get("orderManagement").get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);


        Predicate fieldEqualCriteria = criteriaBuilder.equal(subscriptionAuditRoot.get("programSubscriptionPaymentHistory").get("orderManagement").get("id"), instructorPaymentRoot.get("orderManagement").get("id"));

        Predicate finalPredicate = criteriaBuilder.and(startDateCriteria, endDateCriteria, fieldEqualCriteria, instructorCriteria, subscriptionRenewalCriteria, programCriteria);
        criteriaQuery.where(finalPredicate);

        criteriaQuery.select(criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare")));
        Double instructorShare = entityManager.createQuery(criteriaQuery).getSingleResult();
        if (instructorShare == null) {
            instructorShare = 0.0;
        }
        return instructorShare;
    }


    public Double getNetRevenue(List<String> subscriptionType, long instructorId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);

        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);
        Predicate instructorCriteria = criteriaBuilder.equal(instructorPaymentRoot.get("instructor").get("userId"), instructorId);
        Predicate subscriptionTypeCriteria = instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name").in(subscriptionType);

        Predicate finalPredicate = criteriaBuilder.and(instructorCriteria, subscriptionTypeCriteria);
        Expression<Double> revenue = criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare"));

        criteriaQuery.select(revenue).where(finalPredicate);
        Double netRevenue = entityManager.createQuery(criteriaQuery).getSingleResult();
        return netRevenue == null ? 0.0 : netRevenue.doubleValue();

    }

}
