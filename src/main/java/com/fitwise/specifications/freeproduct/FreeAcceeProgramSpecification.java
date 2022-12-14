package com.fitwise.specifications.freeproduct;

import javax.persistence.criteria.Expression;

import org.springframework.data.jpa.domain.Specification;

import com.fitwise.entity.product.FreeAccessProgram;

public class FreeAcceeProgramSpecification {
	
	/**
	 * Gets the free access program is active.
	 *
	 * @param isActive the is active
	 * @return the free access program is active
	 */
	public static Specification<FreeAccessProgram> getFreeAccessProgramIsActive(boolean isActive) {
		return (root, query, criteriaBuilder) -> {
			Expression<Long> expression = root.get("isActive");
			return criteriaBuilder.equal(expression, isActive);
		};
	}

}
