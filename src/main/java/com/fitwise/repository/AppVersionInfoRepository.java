package com.fitwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.AppVersionInfo;

/**
 * The Interface AppVersionInfoRepository.
 */
public interface AppVersionInfoRepository extends JpaRepository<AppVersionInfo, Long>{
	
	/**
	 * Find by app version and app platform.
	 *
	 * @param appVersion the app version
	 * @param appPlatform the app platform
	 * @return the app version info
	 */
	AppVersionInfo findByAppVersionAndAppPlatformAndApplication(String appVersion,String appPlatform, String role);
	
	/**
	 * Find by is latest version and app platform.
	 *
	 * @param isLatestVersion the is latest version
	 * @param appPlatform the app platform
	 * @return the app version info
	 */
	AppVersionInfo findByIsLatestVersionAndAppPlatform(boolean isLatestVersion,String appPlatform);
	AppVersionInfo findByIsLatestVersionAndAppPlatformAndApplication(boolean isLatestVersion,String appPlatform,String application);
}
