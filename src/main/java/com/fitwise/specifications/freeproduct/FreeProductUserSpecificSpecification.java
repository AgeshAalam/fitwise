package com.fitwise.specifications.freeproduct;

import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.fitwise.constants.DBFieldConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserActiveInactiveTracker;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.product.FreeAccessPackages;
import com.fitwise.entity.product.FreeAccessProgram;
import com.fitwise.entity.product.FreeProduct;
import com.fitwise.entity.product.FreeProductUserSpecific;

public class FreeProductUserSpecificSpecification {

	/**
	 * Gets the free access programs in id list.
	 *
	 * @param freeAccessProgramsIdList the free access programs id list
	 * @return the free access programs in id list
	 */
	public static Specification<FreeProductUserSpecific> getFreeAccessProgramsInIdList(
			List<Long> freeAccessProgramsIdList) {
		return (root, query, criteriaBuilder) -> root.get("freeAccessProgram").get("freeAccessProgramId")
				.in(freeAccessProgramsIdList);
	}

	/**
	 * Gets the free access package in id list.
	 *
	 * @param freeAccessPackageIdList the free access package id list
	 * @return the free access package in id list
	 */
	public static Specification<FreeProductUserSpecific> getFreeAccessPackageInIdList(
			List<Long> freeAccessPackageIdList) {
		return (root, query, criteriaBuilder) -> root.get("freeAccessSubscriptionPackages").get("freeAccessPackageId")
				.in(freeAccessPackageIdList);
	}

	/**
	 * Order by free product user specific id.
	 *
	 * @param order the order
	 * @return the specification
	 */
	public static Specification<FreeProductUserSpecific> orderByFreeProductUserSpecificId(String order) {
		return (root, query, criteriaBuilder) -> {
			Expression<Long> expression = root.get("freeProductUserSpecificId");
			if (order.equalsIgnoreCase(SearchConstants.ORDER_DSC)) {
				query.orderBy(criteriaBuilder.desc(expression));
			} else {
				query.orderBy(criteriaBuilder.asc(expression));
			}
			return query.getRestriction();
		};
	}
	
	/**
	 * Gets the active free progarms and packages basesd on dates.
	 *
	 * @param date the date
	 * @return the active free progarms and packages basesd on dates
	 */
	public static Specification<FreeProductUserSpecific> getActiveFreeProgarmsAndPackagesBasesdOnDates(Date date) {
		return (root, query, criteriaBuilder) -> {
			Expression<Date> startDateExpression = root.get(DBFieldConstants.FIELD_FREE_PRODUCT)
					.get(DBFieldConstants.FIELD_FREE_ACCESS_START_DATE);
			Predicate startDatePredicate = criteriaBuilder.lessThanOrEqualTo(startDateExpression, date);
			
			Expression<Date> endDateExpression = root.get(DBFieldConstants.FIELD_FREE_PRODUCT)
					.get(DBFieldConstants.FIELD_FREE_ACCESS_END_DATE);
			Predicate endDatePredicate = criteriaBuilder.greaterThanOrEqualTo(endDateExpression, date);
			Predicate endDateEqualNullPredicate = criteriaBuilder.isNull(endDateExpression);
			
			Predicate endDateOrConditionPredicate = criteriaBuilder.or(endDatePredicate, endDateEqualNullPredicate);
			Predicate finalPredicate = criteriaBuilder.and(startDatePredicate, endDateOrConditionPredicate);
			query.where(finalPredicate);
			return query.getRestriction();
		};
	}
	
	/**
	 * Gets the active and future free progarms and packages basesd on dates.
	 *
	 * @param date the date
	 * @return the active and future free progarms and packages basesd on dates
	 */
	public static Specification<FreeProductUserSpecific> getActiveAndFutureFreeProgarmsAndPackagesBasesdOnDates(
			Date date) {
		return (root, query, criteriaBuilder) -> {
			Expression<Date> startDateExpression = root.get(DBFieldConstants.FIELD_FREE_PRODUCT)
					.get(DBFieldConstants.FIELD_FREE_ACCESS_START_DATE);
			Predicate startDateGreaterThanEqualPredicate = criteriaBuilder.greaterThanOrEqualTo(startDateExpression,
					date);
			Predicate startDateLessThanEqualPredicate = criteriaBuilder.lessThanOrEqualTo(startDateExpression, date);
			Expression<Date> endDateExpression = root.get(DBFieldConstants.FIELD_FREE_PRODUCT)
					.get(DBFieldConstants.FIELD_FREE_ACCESS_END_DATE);
			Predicate endDateGreaterThanEqualPredicate = criteriaBuilder.greaterThanOrEqualTo(endDateExpression, date);
			Predicate endDateEqualNullPredicate = criteriaBuilder.isNull(endDateExpression);
			Predicate endDateNullOrGreatherPredicate = criteriaBuilder.or(endDateGreaterThanEqualPredicate,
					endDateEqualNullPredicate);
			Predicate case1Predicate = criteriaBuilder.and(startDateLessThanEqualPredicate, endDateEqualNullPredicate);
			Predicate case2Predicate = criteriaBuilder.and(startDateLessThanEqualPredicate,
					endDateGreaterThanEqualPredicate);
			Predicate case3Predicate = criteriaBuilder.and(startDateGreaterThanEqualPredicate,
					endDateNullOrGreatherPredicate);
			Predicate finalPredicate = criteriaBuilder.or(case1Predicate, case2Predicate, case3Predicate);
			query.where(finalPredicate);
			return query.getRestriction();
		};
	}

	/**
	 * Gets the user specific free access programs.
	 *
	 * @param type the type
	 * @param user the user
	 * @param date the date
	 * @param program the program
	 * @return the user specific free access programs
	 */
	public static Specification<FreeProductUserSpecific> getUserSpecificFreeAccessPrograms(String type, User user,
			Date date, Programs program) {
		return (root, query, criteriaBuilder) -> {

			CriteriaQuery<FreeProductUserSpecific> criteriaQuery = (CriteriaQuery<FreeProductUserSpecific>) query;
			Root<FreeProduct> freeProductRoot = criteriaQuery.from(FreeProduct.class);
			criteriaQuery.select(root);

			Expression<String> freeProductTypeExpression = freeProductRoot.get(DBFieldConstants.FIELD_FREE_PRODUCT_TYPE);
			Predicate freeProductTypePredicate = criteriaBuilder.equal(freeProductTypeExpression, type);

			Expression<User> userIdExpression = root.get(DBFieldConstants.FIELD_USER);
			Predicate userIdPredicate = criteriaBuilder.equal(userIdExpression, user);

			Predicate fieldEquals = criteriaBuilder.equal(root.get(DBFieldConstants.FIELD_FREE_PRODUCT).get(DBFieldConstants.FIELD_FREE_PRODUCT_ID),
					freeProductRoot.get(DBFieldConstants.FIELD_FREE_PRODUCT_ID));

			Predicate programPredicate = null;
			if (program != null) {
				Expression<Programs> programExpression = root.get(DBFieldConstants.FIELD_FREE_ACCESS_PROGRAM).get(DBFieldConstants.FIELD_PROGRAM);
				programPredicate = criteriaBuilder.equal(programExpression, program);
			}
			// date criteria
			Expression<Date> startDateExpression = root.get(DBFieldConstants.FIELD_FREE_PRODUCT)
					.get(DBFieldConstants.FIELD_FREE_ACCESS_START_DATE);
			Predicate startDatePredicate = criteriaBuilder.lessThanOrEqualTo(startDateExpression, date);
			Expression<Date> endDateExpression = root.get(DBFieldConstants.FIELD_FREE_PRODUCT)
					.get(DBFieldConstants.FIELD_FREE_ACCESS_END_DATE);
			Predicate endDatePredicate = criteriaBuilder.greaterThanOrEqualTo(endDateExpression, date);
			Predicate endDateEqualNullPredicate = criteriaBuilder.isNull(endDateExpression);
			Predicate endDateOrConditionPredicate = criteriaBuilder.or(endDatePredicate, endDateEqualNullPredicate);
			Predicate datePredicate = criteriaBuilder.and(startDatePredicate, endDateOrConditionPredicate);
			
			Predicate finalPredicate = criteriaBuilder.and(userIdPredicate, datePredicate, freeProductTypePredicate,
					fieldEquals);
			if (programPredicate != null) {
				finalPredicate = criteriaBuilder.and(userIdPredicate, datePredicate, freeProductTypePredicate,
						programPredicate, fieldEquals);
			}
			criteriaQuery.where(finalPredicate);
			return query.getRestriction();
		};
	}
	
	/**
	 * Gets the free access programs by search.
	 *
	 * @param search the search
	 * @return the free access programs by search
	 */
	public static Specification<FreeProductUserSpecific> getFreeAccessProgramsBySearch(String search) {
		return (root, query, criteriaBuilder) -> {
			CriteriaQuery<FreeProductUserSpecific> criteriaQuery = (CriteriaQuery<FreeProductUserSpecific>) query;
			Root<FreeAccessProgram> freeAccessProgramRoot = criteriaQuery.from(FreeAccessProgram.class);
			Root<User> userRoot = criteriaQuery.from(User.class);
			Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);
			criteriaQuery.select(root);
			criteriaQuery.distinct(true);
			Predicate fieldEqualsUserAndFreeProduct = criteriaBuilder.equal(root.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID),
					userRoot.get(DBFieldConstants.FIELD_USER_ID));
			Predicate fieldEqualsProfileAndFreeProduct = criteriaBuilder.equal(root.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID),
					userProfileRoot.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID));
			Predicate fieldEqualsFreeProductAndFreeAccessProram = criteriaBuilder.equal(
					root.get(DBFieldConstants.FIELD_FREE_ACCESS_PROGRAM).get(DBFieldConstants.FIELD_FREE_ACCESS_PROGRAM_ID),
					freeAccessProgramRoot.get(DBFieldConstants.FIELD_FREE_ACCESS_PROGRAM_ID));
			Expression<String> searchExpression = criteriaBuilder.concat(userProfileRoot.get(DBFieldConstants.FIELD_USER_PROFILE_FIRST_NAME), " ");
			searchExpression = criteriaBuilder.concat(searchExpression, userProfileRoot.get(DBFieldConstants.FIELD_USER_PROFILE_LAST_NAME));
			searchExpression = criteriaBuilder.concat(searchExpression, userRoot.get(DBFieldConstants.FIELD_USER_EMAIL));
			searchExpression = criteriaBuilder.concat(searchExpression,
					freeAccessProgramRoot.get(DBFieldConstants.FIELD_PROGRAM).get(DBFieldConstants.FIELD_PROGRAM_TITLE));
			Predicate searchPredicate = criteriaBuilder.like(searchExpression, "%" + search + "%");
			Predicate finalPredicate = criteriaBuilder.and(searchPredicate, fieldEqualsUserAndFreeProduct,
					fieldEqualsProfileAndFreeProduct, fieldEqualsFreeProductAndFreeAccessProram);
			criteriaQuery.where(finalPredicate);
			return query.getRestriction();
		};
	}
	
	/**
	 * Gets the free access package by search.
	 *
	 * @param search the search
	 * @return the free access package by search
	 */
	public static Specification<FreeProductUserSpecific> getFreeAccessPackageBySearch(String search) {
		return (root, query, criteriaBuilder) -> {
			CriteriaQuery<FreeProductUserSpecific> criteriaQuery = (CriteriaQuery<FreeProductUserSpecific>) query;
			Root<FreeAccessPackages> freeAccessPackageRoot = criteriaQuery.from(FreeAccessPackages.class);
			Root<User> userRoot = criteriaQuery.from(User.class);
			Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);
			criteriaQuery.select(root);
			criteriaQuery.distinct(true);
			Predicate fieldEqualsUserAndFreeProduct = criteriaBuilder.equal(root.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID),
					userRoot.get(DBFieldConstants.FIELD_USER_ID));
			Predicate fieldEqualsProfileAndFreeProduct = criteriaBuilder.equal(root.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID),
					userProfileRoot.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID));
			Predicate fieldEqualsFreeProductAndFreeAcceePackage = criteriaBuilder.equal(
					root.get(DBFieldConstants.FIELD_FREE_ACCESS_PACKAGE).get(DBFieldConstants.FIELD_FREE_ACCESS_PACKAGE_ID),
					freeAccessPackageRoot.get(DBFieldConstants.FIELD_FREE_ACCESS_PACKAGE_ID));
			Expression<String> searchExpression = criteriaBuilder.concat(userProfileRoot.get(DBFieldConstants.FIELD_USER_PROFILE_FIRST_NAME), " ");
			searchExpression = criteriaBuilder.concat(searchExpression, userProfileRoot.get(DBFieldConstants.FIELD_USER_PROFILE_LAST_NAME));
			searchExpression = criteriaBuilder.concat(searchExpression, userRoot.get(DBFieldConstants.FIELD_USER_EMAIL));
			searchExpression = criteriaBuilder.concat(searchExpression,
					freeAccessPackageRoot.get(DBFieldConstants.FIELD_PACKAGE).get(DBFieldConstants.FIELD_PACKAGE_TITLE));
			Predicate searchPredicate = criteriaBuilder.like(searchExpression, "%" + search + "%");
			Predicate finalPredicate = criteriaBuilder.and(searchPredicate, fieldEqualsUserAndFreeProduct,
					fieldEqualsProfileAndFreeProduct, fieldEqualsFreeProductAndFreeAcceePackage);
			criteriaQuery.where(finalPredicate);
			return query.getRestriction();
		};
	}
	
	/**
	 * Gets the free access sort specification.
	 *
	 * @param sortOrder the sort order
	 * @param sortBy the sort by
	 * @return the free access sort specification
	 */
	public static Specification<FreeProductUserSpecific> getFreeAccessSortSpecification(final String sortOrder, final String sortBy
	) {
		return (root, query, criteriaBuilder) -> {
			CriteriaQuery<FreeProductUserSpecific> criteriaQuery = (CriteriaQuery<FreeProductUserSpecific>) query;
			criteriaQuery.select(root);
			criteriaQuery.distinct(true);
			Root<UserProfile> userProfileRoot = criteriaQuery.from(UserProfile.class);
			Root<UserActiveInactiveTracker> userActivityRoot = criteriaQuery.from(UserActiveInactiveTracker.class);
			// UserProile and FreeProductUserSpecifc Join
			Predicate fieldEqualsProfileAndFreeProduct = criteriaBuilder.equal(
					root.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID),
					userProfileRoot.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID));
			// UserActiveTracker and FreeProductUserSpecifc Join
			Predicate fieldEqualsActiveTrackerAndFreeProduct = criteriaBuilder.equal(
					root.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID),
					userActivityRoot.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_ID));
			Expression<Object> sortExpression = null;
			if (SearchConstants.MEMBER_NAME.equalsIgnoreCase(sortBy)) {
				sortExpression = userProfileRoot.get(DBFieldConstants.FIELD_USER_PROFILE_FIRST_NAME);
			} else if (SearchConstants.USER_EMAIL.equalsIgnoreCase(sortBy)) {
				sortExpression = root.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_EMAIL);
			} else if (SearchConstants.ONBOARDED_DATE.equalsIgnoreCase(sortBy)) {
				sortExpression = root.get(DBFieldConstants.FIELD_USER).get(DBFieldConstants.FIELD_USER_CREATED_DATE);
			} else if (SearchConstants.PROGRAM_NAME.equalsIgnoreCase(sortBy)) {
				sortExpression = root.get(DBFieldConstants.FIELD_FREE_ACCESS_PROGRAM)
						.get(DBFieldConstants.FIELD_PROGRAM).get(DBFieldConstants.FIELD_PROGRAM_TITLE);
			} else if (SearchConstants.PACKAGE_NAME.equalsIgnoreCase(sortBy)) {
				sortExpression = root.get(DBFieldConstants.FIELD_FREE_ACCESS_PACKAGE)
						.get(DBFieldConstants.FIELD_PACKAGE).get(DBFieldConstants.FIELD_PACKAGE_TITLE);
			} else if (SearchConstants.FREE_ACCESS_START_DATE.equalsIgnoreCase(sortBy)) {
				sortExpression = root.get(DBFieldConstants.FIELD_FREE_PRODUCT)
						.get(DBFieldConstants.FIELD_FREE_ACCESS_START_DATE);
			} else if (SearchConstants.FREE_ACCESS_END_DATE.equalsIgnoreCase(sortBy)) {
				sortExpression = root.get(DBFieldConstants.FIELD_FREE_PRODUCT)
						.get(DBFieldConstants.FIELD_FREE_ACCESS_END_DATE);
			} else if (SearchConstants.USER_LAST_ACCESS_DATE.equalsIgnoreCase(sortBy)) {
				sortExpression = userActivityRoot.get(DBFieldConstants.FIELD_USER_MODIFIED_DATE);
			}
			Predicate finalPred = criteriaBuilder.and(fieldEqualsProfileAndFreeProduct,
					fieldEqualsActiveTrackerAndFreeProduct);
			criteriaQuery.where(finalPred);
			if (SearchConstants.ORDER_ASC.equalsIgnoreCase(sortOrder)) {
				query.orderBy(criteriaBuilder.asc(sortExpression));
			} else {
				query.orderBy(criteriaBuilder.desc(sortExpression));
			}
			return query.getRestriction();
		};
	}
	
	
	/**
	 * Gets the user specific free access packages.
	 *
	 * @param type the type
	 * @param user the user
	 * @param date the date
	 * @param subscriptionPackage the subscription package
	 * @return the user specific free access packages
	 */
	public static Specification<FreeProductUserSpecific> getUserSpecificFreeAccessPackages(String type, User user,
			Date date, SubscriptionPackage subscriptionPackage) {
		return (root, query, criteriaBuilder) -> {

			CriteriaQuery<FreeProductUserSpecific> criteriaQuery = (CriteriaQuery<FreeProductUserSpecific>) query;
			Root<FreeProduct> freeProductRoot = criteriaQuery.from(FreeProduct.class);
			criteriaQuery.select(root);
			
			// free product type criteria
			Expression<String> freeProductTypeExpression = freeProductRoot.get(DBFieldConstants.FIELD_FREE_PRODUCT_TYPE);
			Predicate freeProductTypePredicate = criteriaBuilder.equal(freeProductTypeExpression, type);

			// user criteria
			Expression<User> userIdExpression = root.get(DBFieldConstants.FIELD_USER);
			Predicate userIdPredicate = criteriaBuilder.equal(userIdExpression, user);

			Predicate fieldEquals = criteriaBuilder.equal(
					root.get(DBFieldConstants.FIELD_FREE_PRODUCT).get(DBFieldConstants.FIELD_FREE_PRODUCT_ID),
					freeProductRoot.get(DBFieldConstants.FIELD_FREE_PRODUCT_ID));

			Predicate packagePredicate = null;
			if (subscriptionPackage != null) {
				Expression<SubscriptionPackage> packageExpression = root.get(DBFieldConstants.FIELD_FREE_ACCESS_PACKAGE)
						.get(DBFieldConstants.FIELD_PACKAGE);
				packagePredicate = criteriaBuilder.equal(packageExpression, subscriptionPackage);
			}

			// date criteria
			Expression<Date> startDateExpression = root.get(DBFieldConstants.FIELD_FREE_PRODUCT)
					.get(DBFieldConstants.FIELD_FREE_ACCESS_START_DATE);
			Predicate startDatePredicate = criteriaBuilder.lessThanOrEqualTo(startDateExpression, date);
			Expression<Date> endDateExpression = root.get(DBFieldConstants.FIELD_FREE_PRODUCT)
					.get(DBFieldConstants.FIELD_FREE_ACCESS_END_DATE);
			Predicate endDatePredicate = criteriaBuilder.greaterThanOrEqualTo(endDateExpression, date);
			Predicate endDateEqualNullPredicate = criteriaBuilder.isNull(endDateExpression);
			Predicate endDateOrConditionPredicate = criteriaBuilder.or(endDatePredicate, endDateEqualNullPredicate);
			Predicate datePredicate = criteriaBuilder.and(startDatePredicate, endDateOrConditionPredicate);
			
			Predicate finalPredicate = criteriaBuilder.and(userIdPredicate, datePredicate, freeProductTypePredicate,
					fieldEquals);
			if (packagePredicate != null) {
				finalPredicate = criteriaBuilder.and(userIdPredicate, datePredicate, freeProductTypePredicate,
						packagePredicate, fieldEquals);
			}
			criteriaQuery.where(finalPredicate);

			return query.getRestriction();
		};
	}
	
	
	/**
	 * Gets the user specific free access by user and package id.
	 *
	 * @param user the user
	 * @param packageOrProgramId the package or program id
	 * @param isProgramType the is program type
	 * @return the user specific free access by user and package id
	 */
	public static Specification<FreeProductUserSpecific> getUserSpecificFreeAccessByUserAndPackageId(User user,
			Long packageOrProgramId, boolean isProgramType) {
		return (root, query, criteriaBuilder) -> {
			CriteriaQuery<FreeProductUserSpecific> criteriaQuery = (CriteriaQuery<FreeProductUserSpecific>) query;
			criteriaQuery.select(root);
			Expression<User> userIdExpression = root.get("user");
			Predicate userIdPredicate = criteriaBuilder.equal(userIdExpression, user);
			Expression<Long> idExpression = null;
			Expression<Boolean> isActiveExpression = null;
			if (isProgramType) {
				idExpression = root.get("freeAccessProgram").get("program").get("programId");
				isActiveExpression = root.get("freeAccessProgram").get("isActive");
			} else {
				idExpression = root.get("freeAccessSubscriptionPackages").get("subscriptionPackage")
						.get("subscriptionPackageId");
				isActiveExpression = root.get("freeAccessSubscriptionPackages").get("isActive");
			}
			
			Predicate packageOrProgramIdPredicate = criteriaBuilder.equal(idExpression, packageOrProgramId);
			Predicate activePredicate = criteriaBuilder.equal(isActiveExpression, true);
			// Date criteria
			Predicate finalPredicate = criteriaBuilder.and(userIdPredicate, packageOrProgramIdPredicate,
					activePredicate);
			criteriaQuery.where(finalPredicate);
			return query.getRestriction();
		};
	}
	
	
	/**
	 * Gets the user free program between dates.
	 *
	 * @param user the user
	 * @param program the program
	 * @param date the date
	 * @param checkEndDate the check end date
	 * @return the user free program between dates
	 */
	public static Specification<FreeProductUserSpecific> getUserFreeProgramBetweenDates(User user, Programs program,
			Date date, boolean checkEndDate) {
		return (root, query, criteriaBuilder) -> {
			CriteriaQuery<FreeProductUserSpecific> criteriaQuery = (CriteriaQuery<FreeProductUserSpecific>) query;
			criteriaQuery.select(root);
			Expression<User> userIdExpression = root.get("user");
			Predicate userPredicate = criteriaBuilder.equal(userIdExpression, user);
			Expression<Programs> programExpression = root.get("freeAccessProgram").get("program");
			Predicate programPredicate = criteriaBuilder.equal(programExpression, program);

			Expression<Boolean> isActiveExpression = root.get("freeAccessProgram").get("isActive");
			Predicate activePredicate = criteriaBuilder.equal(isActiveExpression, true);

			Expression<Date> startDateExpression = root.get("freeProduct").get("freeAccessStartDate");
			Predicate startDatePredicate = criteriaBuilder.lessThanOrEqualTo(startDateExpression, date);

			Predicate finalPredicate = criteriaBuilder.and(userPredicate, activePredicate, programPredicate,
					startDatePredicate);
			if (checkEndDate) {
				Expression<Date> endDateExpression = root.get("freeProduct")
						.get("freeAccessEndDate");
				Predicate endDatePredicate = criteriaBuilder.greaterThanOrEqualTo(endDateExpression, date);
				finalPredicate = criteriaBuilder.and(userPredicate, activePredicate, programPredicate,
						startDatePredicate, endDatePredicate);
			}
			// Date criteria
			criteriaQuery.where(finalPredicate);
			return query.getRestriction();
		};
	}
	
	/**
	 * Gets the user free packages between dates.
	 *
	 * @param user the user
	 * @param subscriptionPackage the subscription package
	 * @param date the date
	 * @param checkEndDate the check end date
	 * @return the user free packages between dates
	 */
	public static Specification<FreeProductUserSpecific> getUserFreePackagesBetweenDates(User user, SubscriptionPackage subscriptionPackage,
			Date date, boolean checkEndDate) {
		return (root, query, criteriaBuilder) -> {
			CriteriaQuery<FreeProductUserSpecific> criteriaQuery = (CriteriaQuery<FreeProductUserSpecific>) query;
			criteriaQuery.select(root);
			Expression<User> userIdExpression = root.get("user");
			Predicate userPredicate = criteriaBuilder.equal(userIdExpression, user);
			Expression<SubscriptionPackage> packageExpression = root.get("freeAccessSubscriptionPackages").get("subscriptionPackage");
			Predicate packagePredicate = criteriaBuilder.equal(packageExpression, subscriptionPackage);

			Expression<Boolean> isActiveExpression = root.get("freeAccessSubscriptionPackages").get("isActive");
			Predicate activePredicate = criteriaBuilder.equal(isActiveExpression, true);

			Expression<Date> startDateExpression = root.get("freeProduct").get("freeAccessStartDate");
			Predicate startDatePredicate = criteriaBuilder.lessThanOrEqualTo(startDateExpression, date);

			Predicate finalPredicate = criteriaBuilder.and(userPredicate, activePredicate, packagePredicate,
					startDatePredicate);
			if (checkEndDate) {
				Expression<Date> endDateExpression = root.get("freeProduct")
						.get("freeAccessEndDate");
				Predicate endDatePredicate = criteriaBuilder.greaterThanOrEqualTo(endDateExpression, date);
				finalPredicate = criteriaBuilder.and(userPredicate, activePredicate, packagePredicate,
						startDatePredicate, endDatePredicate);
			}
			// Date criteria
			criteriaQuery.where(finalPredicate);
			return query.getRestriction();
		};
	}

	
	/**
	 * Gets the free product not in specification.
	 *
	 * @param freeProductIdList the free product id list
	 * @return the free product not in specification
	 */
	public static Specification<FreeProductUserSpecific> getFreeProductNotInSpecification(
			List<Long> freeProductIdList) {
		return (root, query, criteriaBuilder) -> root.get(DBFieldConstants.FIELD_FREE_PRODUCT)
				.get(DBFieldConstants.FIELD_FREE_PRODUCT_ID).in(freeProductIdList).not();
	}

}
