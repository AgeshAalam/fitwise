package com.fitwise.specifications;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.fitwise.constants.DBFieldConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.entity.admin.InviteMemberDetails;

public class InviteMemberSpecification {

	/**
	 * Gets the invite member sorting specification.
	 *
	 * @param sortOrder the sort order
	 * @param sortBy    the sort by
	 * @return the invite member sorting specification
	 */
	public static Specification<InviteMemberDetails> getInviteMemberSortingSpecification(final String sortOrder,
			final String sortBy) {
		return (root, query, criteriaBuilder) -> {
			CriteriaQuery<InviteMemberDetails> criteriaQuery = (CriteriaQuery<InviteMemberDetails>) query;
			criteriaQuery.select(root);
			criteriaQuery.distinct(true);
			Expression<Object> sortExpression = null;
			if (sortOrder != null && sortBy != null) {
				if (DBFieldConstants.FIELD_USER_EMAIL.equalsIgnoreCase(sortBy)) {
					sortExpression = root.get(DBFieldConstants.FIELD_USER_EMAIL);
				} else if (DBFieldConstants.FIELD_USER_PROFILE_FIRST_NAME.equalsIgnoreCase(sortBy)) {
					sortExpression = root.get(DBFieldConstants.FIELD_USER_PROFILE_FIRST_NAME);
				} else if (DBFieldConstants.FIELD_USER_PROFILE_LAST_NAME.equalsIgnoreCase(sortBy)) {
					sortExpression = root.get(DBFieldConstants.FIELD_USER_PROFILE_LAST_NAME);
				} else if (SearchConstants.MEMBER_INVITE_DATE.equalsIgnoreCase(sortBy)) {
					sortExpression = root.get(DBFieldConstants.FIELD_USER_CREATED_DATE);
				}
				if (SearchConstants.ORDER_ASC.equalsIgnoreCase(sortOrder)) {
					query.orderBy(criteriaBuilder.asc(sortExpression));
				} else {
					query.orderBy(criteriaBuilder.desc(sortExpression));
				}
			} else {
				sortExpression = root.get(DBFieldConstants.FIELD_INVITE_MEMBER_ID);
				query.orderBy(criteriaBuilder.desc(sortExpression));
			}
			return query.getRestriction();
		};
	}

	/**
	 * Gets the invite member search specification.
	 *
	 * @param search the search
	 * @return the invite member search specification
	 */
	public static Specification<InviteMemberDetails> getInviteMemberSearchSpecification(final String search) {
		return (root, query, criteriaBuilder) -> {
			CriteriaQuery<InviteMemberDetails> criteriaQuery = (CriteriaQuery<InviteMemberDetails>) query;
			criteriaQuery.select(root);
			criteriaQuery.distinct(true);
			Expression<String> searchExpression = criteriaBuilder
					.concat(root.get(DBFieldConstants.FIELD_USER_PROFILE_FIRST_NAME), " ");
			searchExpression = criteriaBuilder.concat(searchExpression,
					root.get(DBFieldConstants.FIELD_USER_PROFILE_LAST_NAME));
			searchExpression = criteriaBuilder.concat(searchExpression, root.get(DBFieldConstants.FIELD_USER_EMAIL));
			Predicate searchPredicate = criteriaBuilder.like(searchExpression, "%" + search + "%");
			criteriaQuery.where(searchPredicate);
			return query.getRestriction();
		};
	}

}
