package com.fitwise.specifications;

import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.fitwise.constants.DBFieldConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.entity.BlockedUser;
import com.fitwise.entity.FlaggedExercise;
import com.fitwise.entity.InstructorProgramExperience;
import com.fitwise.entity.ProgramPromoViews;
import com.fitwise.entity.ProgramViewsAudit;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserRoleMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.subscription.SubscriptionAudit;

/*
 * Created by Vignesh G on 04/07/20
 */
public class UserProfileSpecifications {

    public static Specification<UserProfile> getUserProfileByFlaggedVideoAndReasonId(Long exerciseId, Long reasonId) {

        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<UserProfile> criteriaQuery = (CriteriaQuery<UserProfile>) query;
            Root<FlaggedExercise> flaggedExerciseRoot = criteriaQuery.from(FlaggedExercise.class);

            criteriaQuery.select(root);

            //exercise id criteria
            Expression<Long> exerciseIdExpression = flaggedExerciseRoot.get("exercise").get("exerciseId");
            Predicate exerciseIdEquals = criteriaBuilder.equal(exerciseIdExpression, exerciseId);

            //reason id criteria
            Expression<Long> reasonIdExpression = flaggedExerciseRoot.get("flaggedVideoReason").get("feedbackId");
            Predicate reasonIdEquals = criteriaBuilder.equal(reasonIdExpression, reasonId);

            //User, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("user").get("userId"), flaggedExerciseRoot.get("user").get("userId"));


            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, exerciseIdEquals, reasonIdEquals);

            criteriaQuery.where(finalPredicate);
            return criteriaQuery.getRestriction();
        };
    }

    public static Specification<UserProfile> getInstructorProgramExperienceByProgramTypeIn(List<Long> programTypeIdList) {
        return (root, query, criteriaBuilder) -> {
            Root<InstructorProgramExperience> instructorProgramExperienceRoot = query.from(InstructorProgramExperience.class);
            instructorProgramExperienceRoot.alias("ipe");
            query.distinct(true);
            query.where(criteriaBuilder.and(criteriaBuilder.equal(root.get("user").get("userId"), instructorProgramExperienceRoot.get("user").get("userId")), instructorProgramExperienceRoot.get(StringConstants.JSON_PROPERTY_KEY_PROGRAM_TYPE).get("programTypeId").in(programTypeIdList)));
            return query.getRestriction();
        };
    }

    /**
     * Get user profiles with Id not in user id list
     * @param userIdList
     * @return
     */
    public static Specification<UserProfile> getUserProfilesNotInUserIdList(List<Long> userIdList) {
        return (root, query, criteriaBuilder) -> root.get("user").get("userId").in(userIdList).not();
    }

    /**
     * Get users profiles with Id in user id list
     * @param idList
     * @return
     */
    public static Specification<UserProfile> getUserProfilesInUserIdList(List<Long> idList) {
        return (root, query, criteriaBuilder) -> root.get("user").get("userId").in(idList);
    }

    public static Specification<UserProfile> getInstructorsByPublishPrograms() {
        return (root, query, criteriaBuilder) -> {
            CriteriaQuery<UserProfile> criteriaQuery = (CriteriaQuery<UserProfile>) query;
            Root<Programs> programRoot = criteriaQuery.from(Programs.class);
            root.alias("u");
            programRoot.alias("p");
            Predicate fieldEquals = criteriaBuilder.equal(root.get("user").get("userId"), programRoot.get("owner").get("userId"));
            Expression<String> expression = programRoot.get("status");
            Predicate nameSearch = criteriaBuilder.equal(expression, KeyConstants.KEY_PUBLISH);
			Predicate finalPredicate = criteriaBuilder.and(fieldEquals, nameSearch);
            criteriaQuery.where(finalPredicate);
            return criteriaQuery.getRestriction();
        };
    }

    /**
     * Get user profiles with name search
     * @param username
     * @return
     */
    public static Specification<UserProfile> getUserProfileByName(String username) {
        return (root, query, criteriaBuilder) -> {
            //Name search criteria
            Expression<String> expression = criteriaBuilder.concat(root.get("firstName"), " ");
            expression = criteriaBuilder.concat(expression, root.get("lastName"));
            return criteriaBuilder.like(expression, "%" + username + "%");
        };
    }

    /**
     * Get user profiles based on role id
     * @param roleId
     * @return
     */
    public static Specification<UserProfile> getUserProfilesByRoleId(Long roleId) {

        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<UserProfile> criteriaQuery = (CriteriaQuery<UserProfile>) query;
            Root<UserRoleMapping> userRoleRoot = criteriaQuery.from(UserRoleMapping.class);

            criteriaQuery.select(root);
            criteriaQuery.distinct(true);

            //UserProfile and user role inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("user").get("userId"), userRoleRoot.get("user").get("userId"));

            //role id criteria
            Expression<Long> expression = userRoleRoot.get("userRole").get("roleId");
            Predicate roleEquals = criteriaBuilder.equal(expression, roleId);

            Predicate finalPredicate = criteriaBuilder.and(fieldEquals, roleEquals);

            criteriaQuery.where(finalPredicate);
            return criteriaQuery.getRestriction();
        };
    }

    public static Specification<UserProfile> getUserProfilesOrderByName(String sortOrder) {
        return (root, query, criteriaBuilder) -> {
            Expression<Long> idExpression = root.get("profileId");

            Expression<String> nameExpression = criteriaBuilder.concat(root.get("firstName"), " ");
            nameExpression = criteriaBuilder.concat(nameExpression, root.get("lastName"));

            if (sortOrder.equalsIgnoreCase(SearchConstants.ORDER_ASC)) {
                query.orderBy(criteriaBuilder.asc(nameExpression), criteriaBuilder.asc(idExpression));
            } else {
                query.orderBy(criteriaBuilder.desc(nameExpression), criteriaBuilder.desc(idExpression));
            }

            return query.getRestriction();
        };
    }

    /**
     * Get unique program views
     * @param programId
     * @param startDate
     * @param endDate
     * @return
     */
    public static Specification<UserProfile> getProgramViewsGroupByUsers(Long programId, Date startDate, Date endDate) {
        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<UserProfile> criteriaQuery = (CriteriaQuery<UserProfile>) query;
            Root<ProgramViewsAudit> programViewsAuditRoot = criteriaQuery.from(ProgramViewsAudit.class);
            criteriaQuery.distinct(true);

            criteriaQuery.select(root);

            Expression<Long> programIdExpression = programViewsAuditRoot.get("program").get("programId");
            Predicate programIdPredicate = criteriaBuilder.equal(programIdExpression,programId);


            //ProgramViewAudits, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("user"), programViewsAuditRoot.get("user"));
            Expression<Date> dateExpression = programViewsAuditRoot.get("date");

            //Date criteria
            Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
            Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);
            Predicate finalPred = criteriaBuilder.and(startDateCriteria,endDateCriteria,programIdPredicate,fieldEquals);
            criteriaQuery.where(finalPred);


            return criteriaQuery.getRestriction();
        };
    }


    /**
     * Get unique promo views
     * @param programId
     * @param startDate
     * @param endDate
     * @return
     */
    public static Specification<UserProfile> getPromoViewsGroupByUsers(Long programId, Date startDate, Date endDate) {
        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<UserProfile> criteriaQuery = (CriteriaQuery<UserProfile>) query;
            Root<ProgramPromoViews> promoViewsRoot = criteriaQuery.from(ProgramPromoViews.class);
            criteriaQuery.distinct(true);

            criteriaQuery.select(root);

            Expression<Long> programIdExpression = promoViewsRoot.get("program").get("programId");
            Predicate programIdPredicate = criteriaBuilder.equal(programIdExpression,programId);


            //ProgramPromoViews, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("user"), promoViewsRoot.get("user"));
            Expression<Date> dateExpression = promoViewsRoot.get("date");

            //Date criteria
            Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
            Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);
            Predicate finalPred = criteriaBuilder.and(startDateCriteria,endDateCriteria,programIdPredicate,fieldEquals);
            criteriaQuery.where(finalPred);


            return criteriaQuery.getRestriction();
        };
    }

    /**
     * Get unique subscriptions
     * @param programId
     * @param type
     * @param renewalStatus
     * @param statusList
     * @param startDate
     * @param endDate
     * @return
     */
    public static Specification<UserProfile> geSubscriptionAuditsForProgramByRenewalStatusAndBetweenAndGroupByUsers(Long programId,String type,String renewalStatus,List<String> statusList, Date startDate, Date endDate) {
        return (root, query, criteriaBuilder) -> {

            CriteriaQuery<UserProfile> criteriaQuery = (CriteriaQuery<UserProfile>) query;
            Root<SubscriptionAudit> subscriptionAuditRoot = criteriaQuery.from(SubscriptionAudit.class);
            criteriaQuery.distinct(true);

            criteriaQuery.select(root);


            //Subscription Audit, UserProfile inner Join criteria
            Predicate fieldEquals = criteriaBuilder.equal(root.get("user"), subscriptionAuditRoot.get("user"));

            //ProgramId criteria
            Expression<Long> programIdExpression = subscriptionAuditRoot.get("programSubscription").get("program").get("programId");
            Predicate programIdPredicate = criteriaBuilder.equal(programIdExpression,programId);

            //type criteria
            Expression<String> typeExpression = subscriptionAuditRoot.get("subscriptionType").get("name");
            Predicate typePredicate = criteriaBuilder.like(typeExpression,type);

            //Renewal status criteria
            Expression<String> renewalStatusExpression = subscriptionAuditRoot.get("renewalStatus");
            Predicate renewalStatusPredicate = criteriaBuilder.like(renewalStatusExpression,renewalStatus);

            //Subscription status in criteria
            Predicate subscriptionStatusPredicate = subscriptionAuditRoot.get("subscriptionStatus").get("subscriptionStatusName").in(statusList);


            //Date criteria
            Expression<Date> dateExpression = subscriptionAuditRoot.get("createdDate");
            Predicate startDateCriteria = criteriaBuilder.greaterThanOrEqualTo(dateExpression, startDate);
            Predicate endDateCriteria = criteriaBuilder.lessThanOrEqualTo(dateExpression, endDate);

            Predicate finalPred = criteriaBuilder.and(fieldEquals,programIdPredicate,typePredicate,renewalStatusPredicate,subscriptionStatusPredicate,startDateCriteria,endDateCriteria);
            criteriaQuery.where(finalPred);


            return criteriaQuery.getRestriction();
        };
    }
    
	/**
	 * Gets the users profile by details search.
	 *
	 * @param search the search
	 * @return the users profile by details search
	 */
	public static Specification<UserProfile> getUsersProfileByDetailsSearch(final String search, final long roleId) {

		return (root, query, criteriaBuilder) -> {
			CriteriaQuery<UserProfile> criteriaQuery = (CriteriaQuery<UserProfile>) query;
			criteriaQuery.distinct(true);
			criteriaQuery.select(root);
			Root<User> userRoot = criteriaQuery.from(User.class);
			Root<UserRoleMapping> userRoleMappingRoot = criteriaQuery.from(UserRoleMapping.class);
			Root<BlockedUser> blockedUserRoot = criteriaQuery.from(BlockedUser.class);
			// User, UserProfile inner Join criteria
			Predicate fieldEqualsUserAndUserProfile = criteriaBuilder.equal(
					root.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID),
					userRoot.get(DBFieldConstants.FIELD_USER_ID));
			// User Role Mapping, UserProfile inner Join criteria
			Predicate fieldEqualsUserAndUserRole = criteriaBuilder.equal(
					root.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID),
					userRoleMappingRoot.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID));
			// Role equal criteria
			Expression<Long> roleEqualExpression = userRoleMappingRoot.get(DBFieldConstants.FIELD_USER_ROLE)
					.get(DBFieldConstants.FIELD_USER_ROLE_ID);
			Predicate roleEquals = criteriaBuilder.equal(roleEqualExpression, roleId);
			// Name and email search criteria
			Expression<String> expression = criteriaBuilder
					.concat(root.get(DBFieldConstants.FIELD_USER_PROFILE_FIRST_NAME), " ");
			expression = criteriaBuilder.concat(expression, root.get(DBFieldConstants.FIELD_USER_PROFILE_LAST_NAME));
			expression = criteriaBuilder.concat(expression, userRoot.get(DBFieldConstants.FIELD_USER_EMAIL));
			Predicate nameSearch = criteriaBuilder.like(expression, "%" + search + "%");

			// Blocked User Id, User profile not equal join criteria
			//Join<UserProfile, BlockedUser> blockedUserRootJoin = root.join(DBFieldConstants.FIELD_USER, JoinType.LEFT);
			Predicate fieldNotEqualsUserProfileAndBlockedUserId = criteriaBuilder.notEqual(
					root.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID),
					blockedUserRoot.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID));

			Predicate finalPredicate = criteriaBuilder.and(fieldEqualsUserAndUserProfile, fieldEqualsUserAndUserRole, roleEquals, nameSearch, fieldNotEqualsUserProfileAndBlockedUserId);
			criteriaQuery.where(finalPredicate);
			return criteriaQuery.getRestriction();
		};
	}
	
	/**
	 * Gets the instructors by published subriction packages.
	 *
	 * @return the instructors by published subriction packages
	 */
	public static Specification<UserProfile> getInstructorsByPublishedSubrictionPackages() {
		return (root, query, criteriaBuilder) -> {
			CriteriaQuery<UserProfile> criteriaQuery = (CriteriaQuery<UserProfile>) query;
			Root<SubscriptionPackage> packageRoot = criteriaQuery.from(SubscriptionPackage.class);
			criteriaQuery.select(root);
			criteriaQuery.distinct(true);
			Predicate fieldEquals = criteriaBuilder.equal(
					root.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID),
					packageRoot.get(DBFieldConstants.FIELD_OWNER).get(DBFieldConstants.FIELD_USER_ID));
			Expression<String> expression = packageRoot.get(SearchConstants.STATUS);
			Predicate statusSearch = criteriaBuilder.equal(expression, KeyConstants.KEY_PUBLISH);
			Predicate finalPredicate = criteriaBuilder.and(fieldEquals, statusSearch);
			criteriaQuery.where(finalPredicate);
			return criteriaQuery.getRestriction();
		};
	}
}

