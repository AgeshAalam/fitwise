package com.fitwise.repository.payments.appleiap;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.payments.appleiap.AppInformation;

public interface AppInformationRepository extends JpaRepository<AppInformation, Long>{

	AppInformation findByVendorId(String vendorId);

}
