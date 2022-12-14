package com.fitwise.repository.qbo;

import com.fitwise.entity.User;
import com.fitwise.entity.qbo.QboVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing vendor qbo entity in fitwise
 */
@Repository
public interface QboVendorRepository extends JpaRepository<QboVendor, Long> {
    List<QboVendor> findByNeedUpdate(final Boolean isNeedUpdate);

    List<QboVendor> findByUser(final User user);

    List<QboVendor> findByDisplayName(String displayName);
}
