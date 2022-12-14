package com.fitwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.AppConfigKeyValue;

/**
 * The Interface AppConfigKeyValueRepository.
 */
public interface AppConfigKeyValueRepository extends JpaRepository<AppConfigKeyValue, Long>{
	
	/**
	 * Find by key.
	 * @param keyName the key name
	 * @return the app config key value repository
	 */
	AppConfigKeyValue findByKeyString(String keyName);
}
