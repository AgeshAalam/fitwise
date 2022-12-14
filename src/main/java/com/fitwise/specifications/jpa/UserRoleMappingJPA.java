package com.fitwise.specifications.jpa;

import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.User;
import com.fitwise.entity.UserActiveInactiveTracker;
import com.fitwise.entity.UserRoleMapping;
import com.fitwise.repository.UserActiveInactiveTrackerRepository;
import com.fitwise.specifications.jpa.dao.AdminUserDao;
import com.fitwise.specifications.jpa.dao.InstructorRevenue;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Created by Vignesh.G on 25/06/21
 */
@Component
public class UserRoleMappingJPA {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserActiveInactiveTrackerRepository userActiveInactiveTrackerRepository;

    public List<InstructorRevenue> getAllInstructorProgramRevenue(Date startDate, Date endDate) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<InstructorRevenue> criteriaQuery = criteriaBuilder.createQuery(InstructorRevenue.class);

        Root<UserRoleMapping> userRoleMappingRoot = criteriaQuery.from(UserRoleMapping.class);

        Subquery subquery = criteriaQuery.subquery(InstructorPayment.class);
        Root<InstructorPayment> instructorPaymentRoot = subquery.from(InstructorPayment.class);

        Predicate susbcriptionTypeCriteria = criteriaBuilder.like(instructorPaymentRoot.get("orderManagement").get("subscriptionType").get("name"), KeyConstants.KEY_PROGRAM);
        Predicate instructorIdCriteria = criteriaBuilder.equal(instructorPaymentRoot.get("instructor").get("userId"), userRoleMappingRoot.get("user").get("userId"));

        //Date criteria
        Expression<Date> dateExpression = instructorPaymentRoot.get("orderManagement").get("createdDate");
        Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
        Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

        Predicate finalPredicate = criteriaBuilder.and(susbcriptionTypeCriteria, instructorIdCriteria, startDateCriteria, endDateCriteria);
        subquery.where(finalPredicate);

        Expression<Double> revenue = criteriaBuilder.sum(instructorPaymentRoot.get("instructorShare"));
        subquery.select(revenue);

        Expression<Long> instructorId = userRoleMappingRoot.get("user").get("userId");
        Expression<Double> instructorShare = subquery.getSelection();

        criteriaQuery.multiselect(instructorId, instructorShare);

        Predicate roleCriteria = criteriaBuilder.like(userRoleMappingRoot.get("userRole").get("name"), KeyConstants.KEY_INSTRUCTOR);
        criteriaQuery.where(roleCriteria);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    /**
     * Get List of admin users
     *
     * @param superAdminEmailAddresses
     * @return
     */
    public List<AdminUserDao> getAdminUsers(List<String> superAdminEmailAddresses, int pageNo, int pageSize, String sortOrder, String sortBy, Optional<String> search) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AdminUserDao> criteriaQuery = criteriaBuilder.createQuery(AdminUserDao.class);

        Root<UserRoleMapping> userRoleMappingRoot = criteriaQuery.from(UserRoleMapping.class);
        Root<UserActiveInactiveTracker> userActiveInactiveTrackerRoot = criteriaQuery.from(UserActiveInactiveTracker.class);
        Predicate fieldEqualCriteria = criteriaBuilder.equal(userActiveInactiveTrackerRoot.get("user").get("userId"), userRoleMappingRoot.get("user").get("userId"));
        Predicate roleEqualCriteria = criteriaBuilder.equal(userActiveInactiveTrackerRoot.get("userRole").get("roleId"), userRoleMappingRoot.get("userRole").get("roleId"));

        Predicate rolePredicate = criteriaBuilder.like(userRoleMappingRoot.get("userRole").get("name"), KeyConstants.KEY_ADMIN);
        Predicate finalPredicate = criteriaBuilder.and(rolePredicate, roleEqualCriteria, fieldEqualCriteria);

        if (search.isPresent() && !search.get().isEmpty()) {
            Predicate emailSearch = criteriaBuilder.like(userRoleMappingRoot.get("user").get("email"), "%" + search.get() + "%");
            finalPredicate = criteriaBuilder.and(finalPredicate, emailSearch);
        }
        criteriaQuery.where(finalPredicate);
        criteriaQuery.groupBy(userRoleMappingRoot.get("user").get("userId"), userRoleMappingRoot.get("userRole").get("roleId"));


        Expression<Long> userId = userRoleMappingRoot.get("user").get("userId");
        Expression<String> email = userRoleMappingRoot.get("user").get("email");
        Expression<Date> onboardedDate = userRoleMappingRoot.get("createdDate");
        Expression<Date> lastLoginDate = userActiveInactiveTrackerRoot.get("modifiedDate");
        lastLoginDate = criteriaBuilder.greatest(lastLoginDate);
        Expression<Object> userRole = criteriaBuilder.selectCase()
                .when(userRoleMappingRoot.get("user").get("email").in(superAdminEmailAddresses), "Super Admin")
                .otherwise(userRoleMappingRoot.get("userRole").get("name"));

        criteriaQuery.multiselect(userId, email, userRole, onboardedDate, lastLoginDate);

        if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
            if (sortBy.equalsIgnoreCase(SearchConstants.USER_EMAIL)) {
                criteriaQuery.orderBy(criteriaBuilder.desc(email), criteriaBuilder.desc(userId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.USER_ROLE)) {
                criteriaQuery.orderBy(criteriaBuilder.desc(userRole), criteriaBuilder.desc(userId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.ONBOARDED_DATE)) {
                criteriaQuery.orderBy(criteriaBuilder.desc(onboardedDate), criteriaBuilder.desc(userId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.USER_LAST_ACCESS_DATE)) {
                criteriaQuery.orderBy(criteriaBuilder.desc(lastLoginDate), criteriaBuilder.desc(userId));
            }
        } else {
            if (sortBy.equalsIgnoreCase(SearchConstants.USER_EMAIL)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(email), criteriaBuilder.asc(userId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.USER_ROLE)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(userRole), criteriaBuilder.asc(userId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.ONBOARDED_DATE)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(onboardedDate), criteriaBuilder.asc(userId));
            } else if (sortBy.equalsIgnoreCase(SearchConstants.USER_LAST_ACCESS_DATE)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(lastLoginDate), criteriaBuilder.asc(userId));
            }
        }
        return entityManager.createQuery(criteriaQuery).setFirstResult(pageNo * pageSize).setMaxResults(pageSize).getResultList();

    }

    /**
     * Get Count of admin users
     *
     * @param search
     * @return
     */
    public Long getCountOfAdminUsers(Optional<String> search) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

        Root<UserRoleMapping> userRoleMappingRoot = criteriaQuery.from(UserRoleMapping.class);

        Predicate rolePredicate = criteriaBuilder.like(userRoleMappingRoot.get("userRole").get("name"), KeyConstants.KEY_ADMIN);
        Predicate finalPredicate = rolePredicate;

        if (search.isPresent() && !search.get().isEmpty()) {
            Predicate emailSearch = criteriaBuilder.like(userRoleMappingRoot.get("user").get("email"), "%" + search.get() + "%");
            finalPredicate = criteriaBuilder.and(finalPredicate, emailSearch);
        }
        criteriaQuery.select(criteriaBuilder.count(userRoleMappingRoot));
        criteriaQuery.where(finalPredicate);

        Long count = entityManager.createQuery(criteriaQuery).getSingleResult();
        return count == null ? 0L : count.longValue();
    }

    /**
     * @param roleName
     * @return
     */
    public List<User> getUsersByRoleName(String roleName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);

        Root<UserRoleMapping> userRoleMappingRoot = criteriaQuery.from(UserRoleMapping.class);

        Predicate rolePredicate = criteriaBuilder.like(userRoleMappingRoot.get("userRole").get("name"), roleName);

        criteriaQuery.select(userRoleMappingRoot.get("user"));
        criteriaQuery.where(rolePredicate);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

}
