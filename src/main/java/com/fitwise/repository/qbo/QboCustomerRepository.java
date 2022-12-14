package com.fitwise.repository.qbo;

import com.fitwise.entity.User;
import com.fitwise.entity.qbo.QboCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing member qbo entity in fitwise
 */
@Repository
public interface QboCustomerRepository extends JpaRepository<QboCustomer, Long> {

    List<QboCustomer> findByNeedUpdate(final Boolean isNeedUpadte);

    List<QboCustomer> findByUser(final User user);

    List<QboCustomer> findByDisplayName(final String displayName);
}

