package com.fitwise.specifications.jpa;

import com.fitwise.entity.qbo.QboVendorBillPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;

/*
 * Created by Vignesh.G on 07/07/21
 */
@Component
@RequiredArgsConstructor
public class QboVendorBillPaymentJPA {

    private final EntityManager entityManager;

    public BigDecimal getSettledAmountForInstructor(Long instructorId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> criteriaQuery = criteriaBuilder.createQuery(BigDecimal.class);

        Root<QboVendorBillPayment> qboVendorBillPaymentRoot = criteriaQuery.from(QboVendorBillPayment.class);

        Expression<Long> instructorIdExpression = qboVendorBillPaymentRoot.get("qboBill").get("instructorPayment").get("instructor").get("userId");
        Predicate instructorIdPredicate = criteriaBuilder.equal(instructorIdExpression, instructorId);
        criteriaQuery.where(instructorIdPredicate);

        criteriaQuery.select(criteriaBuilder.sum(qboVendorBillPaymentRoot.get("settlementAmt")));

        BigDecimal settledAmount = entityManager.createQuery(criteriaQuery).getSingleResult();
        if (settledAmount == null) {
            settledAmount = BigDecimal.valueOf(0.0);
        }
        return settledAmount;
    }
}
