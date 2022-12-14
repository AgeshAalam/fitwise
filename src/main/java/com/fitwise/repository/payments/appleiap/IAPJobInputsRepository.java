package com.fitwise.repository.payments.appleiap;

import com.fitwise.entity.Programs;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.payments.appleiap.IapJobInputs;

import java.util.Date;
import java.util.List;

public interface IAPJobInputsRepository extends JpaRepository<IapJobInputs, Long> {	

	IapJobInputs findTop1ByStatusOrderByIdDesc(String status);

	IapJobInputs findTop1ByProgram(final Programs program);

	List<IapJobInputs> findByModifiedDateLessThanOrModifiedDateIsNull(final Date date);
}
