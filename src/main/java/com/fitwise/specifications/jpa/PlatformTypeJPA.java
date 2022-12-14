package com.fitwise.specifications.jpa;

import com.fitwise.constants.KeyConstants;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.PlatformType;
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

/*
 * Created by Vignesh.G on 01/07/21
 */
@Component
@RequiredArgsConstructor
public class PlatformTypeJPA {

    private final EntityManager entityManager;

    public List<RevenueByPlatform> getRevenueByPlatform(Long userId, Date startDate, Date endDate) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<RevenueByPlatform> criteriaQuery = criteriaBuilder.createQuery(RevenueByPlatform.class);

        Root<PlatformType> platformTypeRoot = criteriaQuery.from(PlatformType.class);
        Expression<Long> platFormIdExpression = platformTypeRoot.get("platformTypeId");

        Subquery nestedQuery = criteriaQuery.subquery(InstructorPayment.class);
        Root<InstructorPayment> instructorPaymentRoot = nestedQuery.from(InstructorPayment.class);

        Predicate platformCriteria = criteriaBuilder.equal(instructorPaymentRoot.get("orderManagement").get("subscribedViaPlatform").get("platformTypeId"), platFormIdExpression);
        Predicate susbcriptionTypeCriteria = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);
        Predicate instructorIdCriteria = criteriaBuilder.equal(instructorPaymentRoot.get("instructor").get("userId"), userId);

        //Date criteria
        Expression<Date> dateExpression = instructorPaymentRoot.get("orderManagement").get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(susbcriptionTypeCriteria, instructorIdCriteria, platformCriteria, startDateCriteria, endDateCriteria);
        nestedQuery.where(finalPredicate);

        Expression<Double> revenue = criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare"));
        nestedQuery.select(revenue);


        Expression<String> platformExpression = platformTypeRoot.get("platform");
        Expression<Double> instructorShare = nestedQuery.getSelection();

        criteriaQuery.multiselect(platformExpression, instructorShare);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public List<RevenueByPlatform> getRevenueByPlatformAndSubscriptionDateBetween(String subscriptionTypeName, Date startDate, Date endDate) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<RevenueByPlatform> criteriaQuery = criteriaBuilder.createQuery(RevenueByPlatform.class);
        Root<PlatformType> platformTypeRoot = criteriaQuery.from(PlatformType.class);

        Expression<Long> platformIdExpression = platformTypeRoot.get("platformTypeId");

        Subquery subquery = criteriaQuery.subquery(InstructorPayment.class);
        Root<InstructorPayment> instructorPaymentRoot = subquery.from(InstructorPayment.class);

        Predicate platformCriteria = criteriaBuilder.equal(instructorPaymentRoot.get("orderManagement").get("subscribedViaPlatform").get("platformTypeId"), platformIdExpression);
        Predicate subscriptionTypeCriteria = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), subscriptionTypeName);

        Expression<Date> dateExpression = instructorPaymentRoot.get("orderManagement").get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(platformCriteria, subscriptionTypeCriteria, startDateCriteria, endDateCriteria);
        subquery.where(finalPredicate);

        Expression<Double> totalSpent = criteriaBuilder.sum(instructorPaymentRoot.get("totalAmt"));
        subquery.select(totalSpent);

        Expression<String> platformExpression = platformTypeRoot.get("platform");
        Expression<Double> instructorShare = subquery.getSelection();

        criteriaQuery.multiselect(platformExpression, instructorShare);

        return entityManager.createQuery(criteriaQuery).getResultList();

    }

    public List<RevenueByPlatform> getInstructorShareByPlatformAndSubscriptionDateBetween(Date startDate, Date endDate) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<RevenueByPlatform> criteriaQuery = criteriaBuilder.createQuery(RevenueByPlatform.class);
        Root<PlatformType> platformTypeRoot = criteriaQuery.from(PlatformType.class);

        Expression<Long> platformIdExpression = platformTypeRoot.get("platformTypeId");

        Subquery subquery = criteriaQuery.subquery(InstructorPayment.class);
        Root<InstructorPayment> instructorPaymentRoot = subquery.from(InstructorPayment.class);

        Predicate platformCriteria = criteriaBuilder.equal(instructorPaymentRoot.get("orderManagement").get("subscribedViaPlatform").get("platformTypeId"), platformIdExpression);
        Predicate subscriptionTypeCriteria = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);

        Expression<Date> dateExpression = instructorPaymentRoot.get("orderManagement").get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(platformCriteria, subscriptionTypeCriteria, startDateCriteria, endDateCriteria);
        subquery.where(finalPredicate);

        Expression<Double> instructorShare = criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare"));
        subquery.select(instructorShare);

        Expression<String> platformExpression = platformTypeRoot.get("platform");
        Expression<Double> instructorShareExpression = subquery.getSelection();

        criteriaQuery.multiselect(platformExpression, instructorShareExpression);

        return entityManager.createQuery(criteriaQuery).getResultList();

    }

}
