package com.fitwise.service;

import com.fitwise.constants.KeyConstants;
import com.google.gson.internal.LinkedTreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fitwise.authentication.UserNamePasswordAuthenticationToken;
import com.fitwise.constants.Constants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.utils.ValidationUtils;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;

/**
 * Redis Service to access Redis Database 
 * @author npc
 *
 */
@Service
@Slf4j
public class RedisService {

	private final static int ADMIN_TOKEN_EXPIRY_TIME = 3600;
	
	/**
	 * Jedis Pool Connection
	 */
	private final JedisPool redisPool;
	
	/**
	 * Redis Expiration Time
	 */
	@Value("${redis.expiration-time}")
	private String expirationTime;
	
	/**
	 * Configure Pool Connection
	 */
	@Autowired
	public RedisService(@Value("${redis.host}") final String redisHost) {
		redisPool  = new  JedisPool(new JedisPoolConfig(),redisHost);
	}
	
	/**
	 * Method For Storing key Value
	 * @param key
	 * @param value
	 */
	public void set(final String key, final String value) throws ApplicationException {
		if(ValidationUtils.isEmptyString(key) || ValidationUtils.isEmptyString(value)) {
			throwException("Invalid Arguments in redis service set function", Constants.UNPROCESSABLE_ENTITY);
		}
		Jedis jedis = null;
		try {
			jedis = redisPool.getResource();
			jedis.set(key, value);
			// 15 Minutes
			jedis.expire(key, Integer.parseInt(expirationTime));
		}catch (Exception e) {
			log.error(e.getMessage(), e);
		}finally {
			if(jedis!=null) {
				jedis.close();
			}
		}
		log.debug("Setting key-value Pair in Redis Server");
	}

	/**
	 * For Deleting Key Value
	 * @param key
	 */
	public void del(final String key) {
	    Jedis jedis = null;
		try {
			jedis = redisPool.getResource();
			jedis.del(key);
		}catch (Exception e) {
			log.error(e.getMessage(), e);
		}finally {
			if(jedis!=null) {
				jedis.close();
			}
		}		
	}

	/**
	 * Getting Value Based on Key
	 * @param key
	 * @return Value
	 */
	public String get(final String key) {
		Jedis jedis = null;
		String value = null;
		try {
			jedis = redisPool.getResource();
			value = jedis.get(key);
		}catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		finally {
			if(jedis!=null) {
				jedis.close();
			}
		}
		return value;
	}
	
	/**
	 * Token Check
	 * @param key
	 * @return
	 */
	public UserNamePasswordAuthenticationToken tokenCheck(String key) throws ApplicationException {
		if(ValidationUtils.isEmptyString(key)) {
			throwException("Invalid Argument", Constants.UNAUTHORIZED);
		}
		Jedis jedis = null;
		UserNamePasswordAuthenticationToken authentication = null;
		try {
			jedis = redisPool.getResource();
			String userInfo = jedis.get(key);
			if(userInfo != null){
				int expiryTimeInSeconds = Integer.parseInt(expirationTime);
				try {
					if (KeyConstants.KEY_ADMIN.equalsIgnoreCase(getCurrentRoleFromToken(userInfo))) {
						expiryTimeInSeconds = ADMIN_TOKEN_EXPIRY_TIME;
					}
				} catch (Exception e) {
					log.warn("Exception occurred while getting role during authentication : tokenCheck() : " + e.getMessage());
				}

				jedis.expire(key, expiryTimeInSeconds);
				authentication = new Gson().fromJson(userInfo, UserNamePasswordAuthenticationToken.class);
			}
		}catch (Exception e) {
			log.error(e.getMessage(),e);
		}finally {
			if(jedis!=null) {
				jedis.close();
			}
		}		
		return authentication; 
	}

	/**
	 * Getting current role from token
	 * @param userInfo
	 * @return
	 */
	private String getCurrentRoleFromToken(String userInfo) {
		UserNamePasswordAuthenticationToken authentication = new Gson().fromJson(userInfo, UserNamePasswordAuthenticationToken.class);
		LinkedTreeMap<String, Object> userDetails = (LinkedTreeMap) authentication.getPrincipal();
		ArrayList<LinkedTreeMap<String, Object>> authorities = (ArrayList<LinkedTreeMap<String, Object>>) userDetails.get("authorities");
		LinkedTreeMap<String, Object> authority = authorities.get(0);
		String role = (String) authority.get("role");
		return role;
	}

	/**
	 * The throwException method
	 * @param message
	 * @param status
	 * @throws ApplicationException
	 */
	private void throwException(final String message, final long status) throws ApplicationException {
		throw new ApplicationException(status, Constants.RESPONSE_FAILURE, message);
	}
}
