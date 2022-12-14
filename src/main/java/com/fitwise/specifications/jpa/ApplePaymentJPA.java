package com.fitwise.specifications.jpa;


import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.specifications.jpa.dao.ApplePaymentDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.Date;
import java.util.List;

@Component
public class ApplePaymentJPA {

    @Autowired
    private EntityManager entityManager;

    /**
     * Get Apple Payment DAO for member
     * @param orderManagementIdList
     * @return List<ApplePaymentDAO>
     */
    public List<ApplePaymentDAO> getAppleExpiryDateByOrderManagementIdList(List<Integer> orderManagementIdList){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ApplePaymentDAO> criteriaQuery = criteriaBuilder.createQuery(ApplePaymentDAO.class);
        Root<ApplePayment> applePaymentRoot = criteriaQuery.from(ApplePayment.class);

        Predicate orderManagementIdListInPredicate = applePaymentRoot.get("orderManagement").get("id").in(orderManagementIdList);

        //Result set expression
        Expression<Integer> orderManagementIdExpression = applePaymentRoot.get("orderManagement").get("id");
        Expression<Date> expiryDateExpression = applePaymentRoot.get("expiryDate");
        Expression<Date> createdDateExpression = applePaymentRoot.get("createdDate");

        criteriaQuery.where(orderManagementIdListInPredicate);
        criteriaQuery.multiselect(orderManagementIdExpression, expiryDateExpression, createdDateExpression);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
