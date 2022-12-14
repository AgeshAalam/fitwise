package com.fitwise.specifications.jpa.runnable;

import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.entity.view.ViewMember;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class MemberDataJpaRunnable implements Runnable {

    private EntityManager entityManager;
    private String sortBy;
    private String sortOrder;
    private Optional<String> name;
    private String status;
    private int pageNo;
    private int pageSize;
    private Map<String, Object> response;

    public MemberDataJpaRunnable(EntityManager entityManager, String sortBy, String sortOrder, Optional<String> name, String status, int pageNo, int pageSize, Map<String, Object> response) {
        this.entityManager = entityManager;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
        this.name = name;
        this.status = status;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.response = response;
    }

    @Override
    public void run() {
        long temp = System.currentTimeMillis();
        log.info("Member data Collection started");
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<ViewMember> query = criteriaBuilder.createQuery(ViewMember.class);
        Root<ViewMember> root = query.from(ViewMember.class);
        Predicate finalPredicate = null;
        Expression<Long> blockedExpression = root.get("blocked");
        if(KeyConstants.KEY_BLOCKED.equalsIgnoreCase(status)){
            finalPredicate = criteriaBuilder.equal(blockedExpression, true);
        }else if(KeyConstants.KEY_OPEN.equalsIgnoreCase(status)){
            finalPredicate = criteriaBuilder.equal(blockedExpression, false);
        }
        if(name.isPresent() && !name.get().isEmpty()){
            Expression<String> nameExpression = root.get("name");
            Expression<String> emailExpression = root.get("email");
            if(finalPredicate != null){
                finalPredicate = criteriaBuilder.and(finalPredicate, criteriaBuilder.or(criteriaBuilder.like(nameExpression, "%" + name.get() + "%"), criteriaBuilder.like(emailExpression, "%" + name.get() + "%")));
            }else {
                finalPredicate = criteriaBuilder.or(criteriaBuilder.like(nameExpression, "%" + name.get() + "%"), criteriaBuilder.like(emailExpression, "%" + name.get() + "%"));
            }
        }
        if(finalPredicate != null){
            query.where(finalPredicate);
        }
        //Sort Query
        Expression<Object> sortExpression = null;
        if(SearchConstants.MEMBER_NAME.equalsIgnoreCase(sortBy)){
            sortExpression = root.get("name");
        }else if(SearchConstants.AMOUNT_SPENT.equalsIgnoreCase(sortBy)){
            sortExpression = root.get("totalSpent");
        }else if(SearchConstants.TOTAL_SUBSCRIPTION.equalsIgnoreCase(sortBy)){
            sortExpression = root.get("activeProgramSubscriptions");
        }else if(SearchConstants.COMPLETED_PROGRAM.equalsIgnoreCase(sortBy)){
            sortExpression = root.get("completedPrograms");
        }else if(SearchConstants.PACKAGE_SUBSCRIPTION_COUNT.equalsIgnoreCase(sortBy)){
            sortExpression = root.get("activePackageSubscriptions");
        }else if(SearchConstants.USER_LAST_ACCESS_DATE.equalsIgnoreCase(sortBy)){
            sortExpression = root.get("lastUserAccess");
        }else if(SearchConstants.ONBOARDED_DATE.equalsIgnoreCase(sortBy)){
            sortExpression = root.get("onboardedOn");
        }else if(SearchConstants.USER_EMAIL.equalsIgnoreCase(sortBy)){
            sortExpression = root.get("email");
        }
        Expression<Long> userIdExpression = root.get("userId");
        if(SearchConstants.ORDER_ASC.equalsIgnoreCase(sortOrder)){
            query.orderBy(criteriaBuilder.asc(sortExpression), criteriaBuilder.asc(userIdExpression));
        }else{
            query.orderBy(criteriaBuilder.desc(sortExpression), criteriaBuilder.desc(userIdExpression));
        }
        List<ViewMember> viewMembers = entityManager.createQuery(query).setFirstResult(pageNo * pageSize).setMaxResults(pageSize).getResultList();
        response.put("data", viewMembers);
        log.info("Member data collection completed : " + (System.currentTimeMillis() - temp));
    }
}
