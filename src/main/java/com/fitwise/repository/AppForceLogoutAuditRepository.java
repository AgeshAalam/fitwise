package com.fitwise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.AppForceLogoutAudit;
import com.fitwise.entity.AppUpdateInfo;
import com.fitwise.entity.AppVersionInfo;
import com.fitwise.entity.DeviceInfoDetails;

/**
 * The Interface AppForceLogoutAuditRepository.
 */
@Repository
public interface AppForceLogoutAuditRepository extends JpaRepository<AppForceLogoutAudit, Long> {
	
	/**
	 * Find by app version id and device info details id and app update info id.
	 *
	 * @param appVersion the app version
	 * @param deviceInfoDetails the device info details
	 * @param appUpdate the app update
	 * @return the list
	 */
	List<AppForceLogoutAudit> findByAppVersionIdAndDeviceInfoDetailsIdAndAppUpdateInfoId(
			AppVersionInfo appVersion, DeviceInfoDetails deviceInfoDetails, AppUpdateInfo appUpdate);
}
