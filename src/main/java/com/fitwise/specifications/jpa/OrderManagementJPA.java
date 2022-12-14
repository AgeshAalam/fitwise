package com.fitwise.specifications.jpa;

import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.specifications.jpa.dao.StripePaymentDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.Date;
import java.util.List;

@Component
public class OrderManagementJPA {

    @Autowired
    private EntityManager entityManager;

    //Getting programId id list
    public List<Long> getActiveProgramSubscriptionsProgramIdListByUserIdForIOS(Long memberId, boolean isPaid){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<OrderManagement> orderManagementRoot = criteriaQuery.from(OrderManagement.class);
        Date date = new Date();

        Subquery orderSubquery = criteriaQuery.subquery(OrderManagement.class);
        Root<OrderManagement> orderManagementRoot1 = orderSubquery.from(OrderManagement.class);

        Subquery subquery = criteriaQuery.subquery(ApplePayment.class);
        Root<ApplePayment> applePaymentRoot = subquery.from(ApplePayment.class);



        Subquery innerSubquery = subquery.subquery(ApplePayment.class);
        Root<ApplePayment> applePaymentInnerRoot = innerSubquery.from(ApplePayment.class);
        Predicate orderPredicate = criteriaBuilder.equal(applePaymentInnerRoot.get("orderManagement").get("id"),orderManagementRoot.get("id"));
        criteriaBuilder.max(applePaymentInnerRoot.get("id"));
        Predicate startDateCriteria;
        //Deciding whether the result program should be paid or expired
        if (isPaid){
            startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(applePaymentInnerRoot.get("expiryDate"), date);
        } else {
            startDateCriteria = criteriaBuilder.lessThanOrEqualTo(applePaymentInnerRoot.get("expiryDate"), date);
        }
        innerSubquery.where(criteriaBuilder.and(orderPredicate,startDateCriteria));
        innerSubquery.select(applePaymentInnerRoot.get("id"));


        Predicate idPredicate = criteriaBuilder.equal(applePaymentRoot.get("id"),innerSubquery.getSelection());
        subquery.where(idPredicate);
        subquery.select(applePaymentRoot.get("orderManagement").get("id"));

        Predicate fieldEqualPredicate = criteriaBuilder.equal(orderManagementRoot1.get("id"),subquery.getSelection());
        Predicate memberPredicate = criteriaBuilder.equal(orderManagementRoot1.get("user").get("userId"), memberId);
        orderSubquery.where(criteriaBuilder.and(fieldEqualPredicate, memberPredicate))
                .groupBy(orderManagementRoot.get("program").get("programId"),orderManagementRoot.get("user").get("userId"));
        orderSubquery.select(criteriaBuilder.max(orderManagementRoot1.get("id")));

        Predicate idPred = criteriaBuilder.equal(orderManagementRoot.get("id"),orderSubquery.getSelection());
        criteriaQuery.where(idPred);

        //Result set expression
        Expression<Long> programIdExpression = orderManagementRoot.get("program").get("programId");

        criteriaQuery.select(programIdExpression);
        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
