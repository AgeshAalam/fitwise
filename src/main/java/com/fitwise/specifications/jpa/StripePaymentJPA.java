package com.fitwise.specifications.jpa;


import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.specifications.jpa.dao.StripePaymentDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.Date;
import java.util.List;

@Component
public class StripePaymentJPA {

    @Autowired
    private EntityManager entityManager;

    /**
     * Get Stripe Payment DAO for member
     * @param orderManagementIdList
     * @return List<StripePaymentDAO>
     */
    public List<StripePaymentDAO> getStripeTransactionStatusByOrderManagementIdList(List<Integer> orderManagementIdList){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<StripePaymentDAO> criteriaQuery = criteriaBuilder.createQuery(StripePaymentDAO.class);
        Root<StripePayment> stripePaymentRoot = criteriaQuery.from(StripePayment.class);

        Predicate orderManagementIdListInPredicate = stripePaymentRoot.get("orderManagement").get("id").in(orderManagementIdList);

        //Result set expression
        Expression<Integer> orderManagementIdExpression = stripePaymentRoot.get("orderManagement").get("id");
        Expression<String> stripeTransactionStatusExpression = stripePaymentRoot.get("transactionStatus");
        Expression<Date> modifiedDateExpression = stripePaymentRoot.get("modifiedDate");

        criteriaQuery.where(orderManagementIdListInPredicate);
        criteriaQuery.multiselect(orderManagementIdExpression, stripeTransactionStatusExpression, modifiedDateExpression);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
