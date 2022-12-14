package com.fitwise.specifications.jpa;

import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.PaymentConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.payments.stripe.connect.StripeAccountAndUserMapping;
import com.fitwise.entity.payments.stripe.connect.StripeTransferErrorLog;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.specifications.jpa.dao.PayoutDao;
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
import java.util.Optional;

/*
 * Created by Vignesh.G on 28/06/21
 */
@Component
@RequiredArgsConstructor
public class InstructorPaymentJPA {

    private final EntityManager entityManager;

    /**
     * Get filter based predicates
     *
     * @param criteriaBuilder
     * @param filterType
     * @param instructorPaymentRoot
     * @return
     */
    private Predicate getFilterPredicate(CriteriaBuilder criteriaBuilder, String filterType, Root<InstructorPayment> instructorPaymentRoot) {
        Predicate filterPredicate = null;

        if (filterType.equalsIgnoreCase(KeyConstants.KEY_PAID)) {
            Expression<Boolean> isTransferDoneExp = instructorPaymentRoot.get("isTransferDone");
            filterPredicate = criteriaBuilder.isTrue(isTransferDoneExp);
        } else if (filterType.equalsIgnoreCase(KeyConstants.KEY_FAILURE)) {
            Expression<Boolean> isTransferFailedExp = instructorPaymentRoot.get("isTransferFailed");
            filterPredicate = criteriaBuilder.isTrue(isTransferFailedExp);
        } else if (filterType.equalsIgnoreCase(SearchConstants.NOT_PAID)) {
            Expression<Boolean> isTransferDoneExp = instructorPaymentRoot.get("isTransferDone");
            Predicate isTransferDonePredicate = criteriaBuilder.isFalse(isTransferDoneExp);

            Expression<Boolean> isTopUpInitiatedExp = instructorPaymentRoot.get("isTopUpInitiated");
            Predicate isTopUpInitiated = criteriaBuilder.isFalse(isTopUpInitiatedExp);

            filterPredicate = criteriaBuilder.and(isTransferDonePredicate, isTopUpInitiated);

        } else if (filterType.equalsIgnoreCase(KeyConstants.KEY_PROCESSING)) {
            Expression<Boolean> isTransferDoneExp = instructorPaymentRoot.get("isTransferDone");
            Predicate isTransferDonePredicate = criteriaBuilder.isFalse(isTransferDoneExp);

            Expression<Boolean> isTopUpInitiatedExp = instructorPaymentRoot.get("isTopUpInitiated");
            Predicate isTopUpInitiated = criteriaBuilder.isTrue(isTopUpInitiatedExp);

            Expression<Boolean> isTransferFailedExp = instructorPaymentRoot.get("isTransferFailed");
            Predicate isTransferFailedPredicate = criteriaBuilder.isFalse(isTransferFailedExp);

            filterPredicate = criteriaBuilder.and(isTransferDonePredicate, isTopUpInitiated, isTransferFailedPredicate);
        } else {
            filterPredicate = null;
        }
        return filterPredicate;
    }

    /**
     * Query to get payouts data in a single query
     *
     * @param pageNo
     * @param pageSize
     * @param platformTypeIdList
     * @param filterType
     * @param search
     * @param sortBy
     * @param sortOrder
     * @return
     */
    public List<PayoutDao> getPayouts(int pageNo, int pageSize, List<Long> platformTypeIdList, String filterType, Optional<String> search, String sortBy, String sortOrder) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PayoutDao> criteriaQuery = criteriaBuilder.createQuery(PayoutDao.class);

        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);

        Expression<Long> instructorPaymentId = instructorPaymentRoot.get("instructorPaymentId");

        Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);
        Predicate profileEquals = criteriaBuilder.equal(instructorPaymentRoot.get("instructor").get("userId"), userProfileRoot.get("user").get("userId"));
        Expression<String> instructorName = criteriaBuilder.concat(userProfileRoot.get("firstName"), " ");
        instructorName = criteriaBuilder.concat(instructorName, userProfileRoot.get("lastName"));

        Expression<Double> instructorShare = instructorPaymentRoot.get("instructorShare");
        Expression<String> subscriptionType = instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name");
        Expression<Long> instructorId = instructorPaymentRoot.get("instructor").get("userId");
        Expression<Date> dueDate = instructorPaymentRoot.get("dueDate");
        Expression<String> stripeTransferStatus = instructorPaymentRoot.get("stripeTransferStatus");
        Expression<String> stripeTransferId = instructorPaymentRoot.get("stripeTransferId");
        Expression<Boolean> isTransferDone = instructorPaymentRoot.get("isTransferDone");
        Expression<Boolean> isTopUpInitiated = instructorPaymentRoot.get("isTopUpInitiated");
        Expression<Boolean> isTransferFailed = instructorPaymentRoot.get("isTransferFailed");
        Expression<String> orderStatus = instructorPaymentRoot.get("orderManagement").get("orderStatus");
        Expression<String> orderId = instructorPaymentRoot.get("orderManagement").get("orderId");

        //Instructor onboarding data
        Subquery nestedQuery1 = criteriaQuery.subquery(InstructorPayment.class);
        Root<StripeAccountAndUserMapping> stripeAccountAndUserMappingRoot = nestedQuery1.from(StripeAccountAndUserMapping.class);
        Predicate stripeAccountEquals = criteriaBuilder.equal(instructorPaymentRoot.get("instructor").get("userId"), stripeAccountAndUserMappingRoot.get("user").get("userId"));
        nestedQuery1.where(stripeAccountEquals);
        Expression<Long> stripeAccountIdNestedQuery = stripeAccountAndUserMappingRoot.get("id");
        nestedQuery1.select(stripeAccountIdNestedQuery);

        Expression<Long> stripeAccountId = nestedQuery1.getSelection();

        Expression<String> platform = instructorPaymentRoot.get("orderManagement").get("subscribedViaPlatform").get("platform");
        Expression<Date> transferDate = instructorPaymentRoot.get("transferDate");

        //Transfer Error Log
        Subquery nestQuery2 = criteriaQuery.subquery(InstructorPayment.class);
        Root<StripeTransferErrorLog> transferErrorLogRoot = nestQuery2.from(StripeTransferErrorLog.class);

        //Subquery for the nested query 2
        Subquery<Long> subQueryInner = nestQuery2.subquery(Long.class);
        Root<StripeTransferErrorLog> transferErrorLogRootInner = subQueryInner.from(StripeTransferErrorLog.class);
        Expression<Long> maxStripeTransferErrorLogId = criteriaBuilder.max(transferErrorLogRootInner.get("id"));
        subQueryInner.select(maxStripeTransferErrorLogId);
        Predicate transferErrorEquals = criteriaBuilder.equal(transferErrorLogRootInner.get("instructorPayment").get("instructorPaymentId"), instructorPaymentRoot.get("instructorPaymentId"));
        subQueryInner.where(transferErrorEquals);

        Predicate transferErrorIdEquals = criteriaBuilder.equal(transferErrorLogRoot.get("id"), subQueryInner.getSelection());
        nestQuery2.where(transferErrorIdEquals);
        Expression<String> transferErrorNestedQuery = transferErrorLogRoot.get("errorMessage");
        nestQuery2.select(transferErrorNestedQuery);

        Expression<String> transferError = nestQuery2.getSelection();

        Expression<String> transferMode = instructorPaymentRoot.get("transferMode");
        Expression<String> transferBillNumber = instructorPaymentRoot.get("billNumber");
        Expression<String> emailExpression = instructorPaymentRoot.get("instructor").get("email");

        criteriaQuery.multiselect(instructorPaymentId, instructorName, instructorShare, subscriptionType, instructorId, dueDate, stripeTransferStatus, stripeTransferId, isTransferDone, isTopUpInitiated, isTransferFailed, orderStatus, orderId, stripeAccountId, platform, transferDate, transferError, transferMode, transferBillNumber, emailExpression);

        Predicate platformIdCriteria = instructorPaymentRoot.get("orderManagement").get("subscribedViaPlatform").get("platformTypeId").in(platformTypeIdList);

        Predicate finalPredicate = criteriaBuilder.and(profileEquals, platformIdCriteria);

        Predicate filterPredicate = getFilterPredicate(criteriaBuilder, filterType, instructorPaymentRoot);
        if (filterPredicate != null) {
            finalPredicate = criteriaBuilder.and(finalPredicate, filterPredicate);
        }

        if (search.isPresent() && !search.get().isEmpty()) {
            Predicate nameSearch = criteriaBuilder.like(instructorName, "%" + search.get() + "%");
            Predicate emailSearch = criteriaBuilder.like(emailExpression, "%" + search.get() + "%");
            Predicate searchPredicate = criteriaBuilder.or(nameSearch, emailSearch);
            finalPredicate = criteriaBuilder.and(finalPredicate, searchPredicate);
        }
        criteriaQuery.where(finalPredicate);

        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            if (sortBy.equalsIgnoreCase(SearchConstants.DUE_DATE)) {
                criteriaQuery.orderBy(criteriaBuilder.desc(dueDate), criteriaBuilder.desc(instructorPaymentId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.PAID_DATE)) {
                criteriaQuery.orderBy(criteriaBuilder.desc(transferDate), criteriaBuilder.desc(instructorPaymentId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.PLATFORM)) {
                criteriaQuery.orderBy(criteriaBuilder.desc(platform), criteriaBuilder.desc(instructorPaymentId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.USER_EMAIL)) {
                criteriaQuery.orderBy(criteriaBuilder.desc(emailExpression), criteriaBuilder.desc(instructorPaymentId));
            } else {
                criteriaQuery.orderBy(criteriaBuilder.desc(instructorShare), criteriaBuilder.desc(instructorPaymentId));
            }
        } else {
            if (sortBy.equalsIgnoreCase(SearchConstants.DUE_DATE)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(dueDate), criteriaBuilder.asc(instructorPaymentId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.PAID_DATE)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(transferDate), criteriaBuilder.asc(instructorPaymentId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.PLATFORM)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(platform), criteriaBuilder.asc(instructorPaymentId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.USER_EMAIL)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(emailExpression), criteriaBuilder.asc(instructorPaymentId));
            } else {
                criteriaQuery.orderBy(criteriaBuilder.asc(instructorShare), criteriaBuilder.asc(instructorPaymentId));
            }
        }

        return entityManager.createQuery(criteriaQuery).setFirstResult(pageNo * pageSize).setMaxResults(pageSize).getResultList();
    }

    public long countOfPayouts(List<Long> platformTypeIdList, String filterType, Optional<String> search) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);

        criteriaQuery.select(criteriaBuilder.count(instructorPaymentRoot.get("instructorPaymentId")));

        Predicate platformIdCriteria = instructorPaymentRoot.get("orderManagement").get("subscribedViaPlatform").get("platformTypeId").in(platformTypeIdList);
        Predicate finalPredicate = platformIdCriteria;

        Predicate filterPredicate = getFilterPredicate(criteriaBuilder, filterType, instructorPaymentRoot);
        if (filterPredicate != null) {
            finalPredicate = criteriaBuilder.and(finalPredicate, filterPredicate);
        }

        if (search.isPresent() && !search.get().isEmpty()) {
            Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);
            Predicate profileEquals = criteriaBuilder.equal(instructorPaymentRoot.get("instructor").get("userId"), userProfileRoot.get("user").get("userId"));
            finalPredicate = criteriaBuilder.and(finalPredicate, profileEquals);

            Expression<String> instructorName = criteriaBuilder.concat(userProfileRoot.get("firstName"), " ");
            instructorName = criteriaBuilder.concat(instructorName, userProfileRoot.get("lastName"));

            Predicate nameSearch = criteriaBuilder.like(instructorName, "%" + search.get() + "%");

            Expression<String> emailExpression = instructorPaymentRoot.get("instructor").get("email");
            Predicate emailSearch = criteriaBuilder.like(emailExpression, "%" + search.get() + "%");
            Predicate searchPredicate = criteriaBuilder.or(nameSearch, emailSearch);

            finalPredicate = criteriaBuilder.and(finalPredicate, searchPredicate);
        }

        criteriaQuery.where(finalPredicate);

        Long countResult = entityManager.createQuery(criteriaQuery).getSingleResult();
        return countResult == null ? 0L : countResult.longValue();
    }

    /**
     * Get instructor share based on renewal status
     *
     * @param instructorId
     * @param renewalStatus
     * @param subscriptionType
     * @param startDate
     * @param endDate
     * @return
     */
    public Double getInstructorRevenueBasedOnRenewalStatus(Long instructorId, String renewalStatus, String subscriptionType, Date startDate, Date endDate) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);
        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);

        Root<SubscriptionAudit> subscriptionAuditRoot = criteriaQuery.from(SubscriptionAudit.class);
        criteriaQuery.distinct(true);

        //Renewal status criteria
        Predicate renewalStatusCriteria = criteriaBuilder.like(subscriptionAuditRoot.get("renewalStatus"), renewalStatus);

        //Order management id equal criteria
        Predicate fieldEqualCriteria = criteriaBuilder.equal(subscriptionAuditRoot.get("programSubscriptionPaymentHistory").get("orderManagement").get("id"), instructorPaymentRoot.get("orderManagement").get("id"));

        //Subscription type criteria
        Predicate susbcriptionTypeCriteria = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), subscriptionType);

        //Instructor id

        Predicate instructorIdCriteria = criteriaBuilder.equal(instructorPaymentRoot.get("instructor").get("userId"), instructorId);
        //Date criteria
        Expression<Date> dateExpression = instructorPaymentRoot.get("orderManagement").get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(susbcriptionTypeCriteria, instructorIdCriteria, startDateCriteria, endDateCriteria, renewalStatusCriteria, fieldEqualCriteria);
        criteriaQuery.where(finalPredicate);
        criteriaQuery.select(criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare")));
        Double instructorShare = entityManager.createQuery(criteriaQuery).getSingleResult();
        if (instructorShare == null) {
            instructorShare = 0.0;
        }
        return instructorShare;
    }


    /**
     * Query to get all payouts data in a single query
     *
     * @param platformTypeIdList
     * @param filterType
     * @param search
     * @param sortBy
     * @param sortOrder
     * @return
     */
    public List<PayoutDao> getAllPayouts(List<Long> platformTypeIdList, String filterType, Optional<String> search, String sortBy, String sortOrder) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PayoutDao> criteriaQuery = criteriaBuilder.createQuery(PayoutDao.class);

        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);

        Expression<Long> instructorPaymentId = instructorPaymentRoot.get("instructorPaymentId");

        Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);
        Predicate profileEquals = criteriaBuilder.equal(instructorPaymentRoot.get("instructor").get("userId"), userProfileRoot.get("user").get("userId"));
        Expression<String> instructorName = criteriaBuilder.concat(userProfileRoot.get("firstName"), " ");
        instructorName = criteriaBuilder.concat(instructorName, userProfileRoot.get("lastName"));

        Expression<Double> instructorShare = instructorPaymentRoot.get("instructorShare");
        Expression<String> subscriptionType = instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name");
        Expression<Long> instructorId = instructorPaymentRoot.get("instructor").get("userId");
        Expression<Date> dueDate = instructorPaymentRoot.get("dueDate");
        Expression<String> stripeTransferStatus = instructorPaymentRoot.get("stripeTransferStatus");
        Expression<String> stripeTransferId = instructorPaymentRoot.get("stripeTransferId");
        Expression<Boolean> isTransferDone = instructorPaymentRoot.get("isTransferDone");
        Expression<Boolean> isTopUpInitiated = instructorPaymentRoot.get("isTopUpInitiated");
        Expression<Boolean> isTransferFailed = instructorPaymentRoot.get("isTransferFailed");
        Expression<String> orderStatus = instructorPaymentRoot.get("orderManagement").get("orderStatus");
        Expression<String> orderId = instructorPaymentRoot.get("orderManagement").get("orderId");

        //Instructor onboarding data
        Subquery nestedQuery1 = criteriaQuery.subquery(InstructorPayment.class);
        Root<StripeAccountAndUserMapping> stripeAccountAndUserMappingRoot = nestedQuery1.from(StripeAccountAndUserMapping.class);
        Predicate stripeAccountEquals = criteriaBuilder.equal(instructorPaymentRoot.get("instructor").get("userId"), stripeAccountAndUserMappingRoot.get("user").get("userId"));
        nestedQuery1.where(stripeAccountEquals);
        Expression<Long> stripeAccountIdNestedQuery = stripeAccountAndUserMappingRoot.get("id");
        nestedQuery1.select(stripeAccountIdNestedQuery);

        Expression<Long> stripeAccountId = nestedQuery1.getSelection();

        Expression<String> platform = instructorPaymentRoot.get("orderManagement").get("subscribedViaPlatform").get("platform");
        Expression<Date> transferDate = instructorPaymentRoot.get("transferDate");

        //Transfer Error Log
        Subquery nestQuery2 = criteriaQuery.subquery(InstructorPayment.class);
        Root<StripeTransferErrorLog> transferErrorLogRoot = nestQuery2.from(StripeTransferErrorLog.class);

        //Subquery for the nested query 2
        Subquery<Long> subQueryInner = nestQuery2.subquery(Long.class);
        Root<StripeTransferErrorLog> transferErrorLogRootInner = subQueryInner.from(StripeTransferErrorLog.class);
        Expression<Long> maxStripeTransferErrorLogId = criteriaBuilder.max(transferErrorLogRootInner.get("id"));
        subQueryInner.select(maxStripeTransferErrorLogId);
        Predicate transferErrorEquals = criteriaBuilder.equal(transferErrorLogRootInner.get("instructorPayment").get("instructorPaymentId"), instructorPaymentRoot.get("instructorPaymentId"));
        subQueryInner.where(transferErrorEquals);

        Predicate transferErrorIdEquals = criteriaBuilder.equal(transferErrorLogRoot.get("id"), subQueryInner.getSelection());
        nestQuery2.where(transferErrorIdEquals);
        Expression<String> transferErrorNestedQuery = transferErrorLogRoot.get("errorMessage");
        nestQuery2.select(transferErrorNestedQuery);

        Expression<String> transferError = nestQuery2.getSelection();

        Expression<String> transferMode = instructorPaymentRoot.get("transferMode");
        Expression<String> transferBillNumber = instructorPaymentRoot.get("billNumber");
        Expression<String> emailExpression = instructorPaymentRoot.get("instructor").get("email");

        criteriaQuery.multiselect(instructorPaymentId, instructorName, instructorShare, subscriptionType, instructorId, dueDate, stripeTransferStatus, stripeTransferId, isTransferDone, isTopUpInitiated, isTransferFailed, orderStatus, orderId, stripeAccountId, platform, transferDate, transferError, transferMode, transferBillNumber, emailExpression);

        Predicate platformIdCriteria = instructorPaymentRoot.get("orderManagement").get("subscribedViaPlatform").get("platformTypeId").in(platformTypeIdList);

        Predicate finalPredicate = criteriaBuilder.and(profileEquals, platformIdCriteria);

        Predicate filterPredicate = getFilterPredicate(criteriaBuilder, filterType, instructorPaymentRoot);
        if (filterPredicate != null) {
            finalPredicate = criteriaBuilder.and(finalPredicate, filterPredicate);
        }

        if (search.isPresent() && !search.get().isEmpty()) {
            Predicate nameSearch = criteriaBuilder.like(instructorName, "%" + search.get() + "%");
            Predicate emailSearch = criteriaBuilder.like(emailExpression, "%" + search.get() + "%");
            Predicate searchPredicate = criteriaBuilder.or(nameSearch, emailSearch);
            finalPredicate = criteriaBuilder.and(finalPredicate, searchPredicate);
        }
        criteriaQuery.where(finalPredicate);

        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            if (sortBy.equalsIgnoreCase(SearchConstants.DUE_DATE)) {
                criteriaQuery.orderBy(criteriaBuilder.desc(dueDate), criteriaBuilder.desc(instructorPaymentId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.PAID_DATE)) {
                criteriaQuery.orderBy(criteriaBuilder.desc(transferDate), criteriaBuilder.desc(instructorPaymentId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.PLATFORM)) {
                criteriaQuery.orderBy(criteriaBuilder.desc(platform), criteriaBuilder.desc(instructorPaymentId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.USER_EMAIL)) {
                criteriaQuery.orderBy(criteriaBuilder.desc(emailExpression), criteriaBuilder.desc(instructorPaymentId));
            } else {
                criteriaQuery.orderBy(criteriaBuilder.desc(instructorShare), criteriaBuilder.desc(instructorPaymentId));
            }
        } else {
            if (sortBy.equalsIgnoreCase(SearchConstants.DUE_DATE)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(dueDate), criteriaBuilder.asc(instructorPaymentId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.PAID_DATE)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(transferDate), criteriaBuilder.asc(instructorPaymentId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.PLATFORM)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(platform), criteriaBuilder.asc(instructorPaymentId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.USER_EMAIL)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(emailExpression), criteriaBuilder.asc(instructorPaymentId));
            } else {
                criteriaQuery.orderBy(criteriaBuilder.asc(instructorShare), criteriaBuilder.asc(instructorPaymentId));
            }
        }

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public Double getInstructorPendingRevenue(Long instructorId, boolean isTransferDone, boolean isTopUpInitiated, List<Long> platformTypeIdList, String refundStatus) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);

        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);

        Expression<Long> instructorIdExpression = instructorPaymentRoot.get("instructor").get("userId");
        Predicate instructorIdPredicate = criteriaBuilder.equal(instructorIdExpression, instructorId);

        Expression<Boolean> isTransferDoneExpression = instructorPaymentRoot.get("isTransferDone");
        Predicate isTransferDonePredicate = isTransferDone ? criteriaBuilder.isTrue(isTransferDoneExpression) : criteriaBuilder.isFalse(isTransferDoneExpression);
        Expression<Boolean> isTopUpInitiatedExpression = instructorPaymentRoot.get("isTopUpInitiated");
        Predicate isTopUpInitiatedPredicate = isTopUpInitiated ? criteriaBuilder.isTrue(isTopUpInitiatedExpression) : criteriaBuilder.isFalse(isTopUpInitiatedExpression);

        Predicate platformIdCriteria = instructorPaymentRoot.get("orderManagement").get("subscribedViaPlatform").get("platformTypeId").in(platformTypeIdList);

        Expression<String> orderStatusExpression = instructorPaymentRoot.get("orderManagement").get("orderStatus");
        Predicate orderStatusPredicate = criteriaBuilder.notLike(orderStatusExpression, refundStatus);

        Predicate finalPredicate = criteriaBuilder.and(instructorIdPredicate, isTransferDonePredicate, isTopUpInitiatedPredicate, platformIdCriteria, orderStatusPredicate);
        criteriaQuery.where(finalPredicate);

        criteriaQuery.select(criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare")));

        Double applePendingRevenue = entityManager.createQuery(criteriaQuery).getSingleResult();
        return applePendingRevenue == null ? 0.0 : applePendingRevenue.doubleValue();
    }

    public Double getInstructorTotalRevenue(Long instructorId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);

        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);

        Expression<Long> instructorIdExpression = instructorPaymentRoot.get("instructor").get("userId");
        Predicate instructorIdPredicate = criteriaBuilder.equal(instructorIdExpression, instructorId);

        criteriaQuery.where(instructorIdPredicate);

        criteriaQuery.select(criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare")));

        Double instructorShare = entityManager.createQuery(criteriaQuery).getSingleResult();
        if (instructorShare == null) {
            instructorShare = 0.0;
        }
        return instructorShare;
    }

    public Double getInstructorIOSRefundRevenue(Long instructorId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);

        Root<InstructorPayment> instructorPaymentRoot = criteriaQuery.from(InstructorPayment.class);

        Expression<Long> instructorIdExpression = instructorPaymentRoot.get("instructor").get("userId");
        Predicate instructorIdPredicate = criteriaBuilder.equal(instructorIdExpression, instructorId);

        Expression<String> orderStatusExpression = instructorPaymentRoot.get("orderManagement").get("orderStatus");
        Predicate orderStatusPredicate = criteriaBuilder.like(orderStatusExpression, KeyConstants.KEY_REFUNDED);

        Expression<String> modeOfPaymentExpression = instructorPaymentRoot.get("orderManagement").get("modeOfPayment");
        Predicate modeOfPaymentPredicate = criteriaBuilder.notLike(orderStatusExpression, PaymentConstants.MODE_OF_PAYMENT_APPLE);

        Predicate finalPredicate = criteriaBuilder.and(instructorIdPredicate, orderStatusPredicate, modeOfPaymentPredicate);
        criteriaQuery.where(finalPredicate);

        criteriaQuery.select(criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare")));

        Double instructorShare = entityManager.createQuery(criteriaQuery).getSingleResult();
        if (instructorShare == null) {
            instructorShare = 0.0;
        }
        return instructorShare;
    }

}
