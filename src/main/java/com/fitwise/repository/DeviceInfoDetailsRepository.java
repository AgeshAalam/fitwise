package com.fitwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.DeviceInfoDetails;
import com.fitwise.entity.User;

public interface DeviceInfoDetailsRepository extends JpaRepository<DeviceInfoDetails, Long> {
	DeviceInfoDetails findByDeviceNameAndDeviceModelNameAndDevicePlatformAndDevicePlatformVersionAndDeviceUuidAndAppVersion(String deviceName,String deviceModelName, 
			String devicePlatform, String devicePlatformVersion, String deviceUuid, String appVersion);
	
	DeviceInfoDetails findByDeviceNameAndDeviceModelNameAndDevicePlatformAndDevicePlatformVersionAndDeviceUuidAndAppVersionAndUserId(String deviceName,String deviceModelName, 
			String devicePlatform, String devicePlatformVersion, String deviceUuid, String appVersion, User user);
}
