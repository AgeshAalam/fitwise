package com.fitwise.repository.qbo;

import com.fitwise.entity.qbo.QboDeposit;
import com.fitwise.entity.qbo.QboPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing payment qbo entity in fitwise
 */
@Repository
public interface QboDepositRepository extends JpaRepository<QboDeposit, Long> {

    List<QboDeposit> findByQboPayment(final QboPayment qboPayment);

    List<QboDeposit> findByNeedUpdate(final Boolean status);

}
