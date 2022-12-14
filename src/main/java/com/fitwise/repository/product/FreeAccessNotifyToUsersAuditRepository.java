package com.fitwise.repository.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.User;
import com.fitwise.entity.product.FreeAccessNotifyToUsersAudit;

@Repository
public interface FreeAccessNotifyToUsersAuditRepository extends JpaRepository<FreeAccessNotifyToUsersAudit, Long>{
	
	/**
	 * Find by user and type.
	 *
	 * @param user the user
	 * @param type the type
	 * @return the list
	 */
	List<FreeAccessNotifyToUsersAudit> findByUserAndType(final User user, final String type);

}
