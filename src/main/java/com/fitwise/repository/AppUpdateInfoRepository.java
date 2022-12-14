package com.fitwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.AppUpdateInfo;
import com.fitwise.entity.AppVersionInfo;

/**
 * The Interface AppUpdateInfoRepository.
 */
@Repository
public interface AppUpdateInfoRepository extends JpaRepository<AppUpdateInfo, Long>{
	
	/**
	 * Find top by app version id order by app update info id desc.
	 *
	 * @param appVersionInfo the app version info
	 * @return the app update info
	 */
	AppUpdateInfo findTopByAppVersionIdOrderByAppUpdateInfoIdDesc(AppVersionInfo appVersionInfo);
	
}
