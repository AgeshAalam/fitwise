package com.fitwise.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fitwise.authentication.UserNamePasswordAuthenticationToken;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.entity.User;
import com.fitwise.exception.ApplicationException;
import com.fitwise.utils.ValidationUtils;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * The Class TokenService.
 */
@Service
@Slf4j
public class TokenService {

	@Autowired
	private RedisService redisSerive;

	/** The encriptionKey. */
	@Value(Constants.ENCRYPTION_KEY)
	private String encryptionKey;

	/** The date formater. */
	private final SimpleDateFormat dateFormater = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss", Locale.ENGLISH);

	/** The Constant sessionIntervalSeconds. */
	private static final long SESSIONINTERVALSECONDS = 86400;

	/** The Constant canRefreshTokenIntervalDays. */
	// private static final long sessionIntervalSeconds = 10; // for testing
	private static final int CANREFRESHTOKENINTERVALDAYS = 1;

	/**
	 * Cache.
	 * 
	 * @param key the token provider
	 * @param auth          the auth
	 * @throws JsonProcessingException
	 */
	public void cache(final String key, final Authentication auth) throws ApplicationException {
		
		redisSerive.set(key, new Gson().toJson(auth));
	}

	/**
	 * Gets the authentication.
	 * 
	 * @param token the token provider
	 * @return the authentication
	 */
	public UserNamePasswordAuthenticationToken getAuthentication(final String token) throws ApplicationException {

		return redisSerive.tokenCheck(token);
	}
	
	/**
	 * Remove the authentication.
	 * 
	 * @param request the token provider
	 * @return the authentication
	 */
	public void logoutUser(final  HttpServletRequest request) {
		log.info("Logout triggered");
		String token = request.getHeader(Constants.X_AUTHORIZATION);
		redisSerive.del(token);
		//Remove time zone key
		redisSerive.del(token + "_" + KeyConstants.KEY_TIME_ZONE);
		request.getSession().invalidate();
		SecurityContextHolder.clearContext();
		log.info("Logged out");
	}

	/**
	 * Update token status.
	 * 
	 * @param tokenProvider the token provider
	 * @param auth          the auth
	 */
	public void updateTokenStatus(final TokenService.TokenProvider tokenProvider, final UserNamePasswordAuthenticationToken auth)
			throws ApplicationException {
		try {
			if (tokenProvider.getCreatedDateTime() == null)
				throw new ApplicationException(Constants.ERROR_STATUS, "Internal Error", "Invalid token");
			LocalDateTime tokenCreatedDate = new LocalDateTime(dateFormater.parse(tokenProvider.getCreatedDateTime()));
			Period period = new Period(tokenCreatedDate, new LocalDateTime(), PeriodType.seconds());
			long diff = period.getSeconds();
			if (diff >= SESSIONINTERVALSECONDS && diff < (86400 * CANREFRESHTOKENINTERVALDAYS)) {
				auth.setTokenExpired(true);
			} else {
				auth.setTokenExpired(false);
			}
		} catch (ParseException e) {
			auth.setTokenExpired(false);
		}
	}

	/**
	 * Removes the.
	 * 
	 * @param tokenProvider the token provider
	 */
	public void remove(final TokenService.TokenProvider tokenProvider) {

	}

	/**
	 * Can refresh.
	 * 
	 * @param tokenProvider the token provider
	 * @return true, if successful
	 */
	public boolean canRefresh(final TokenService.TokenProvider tokenProvider) {
		boolean canRefresh = true;
		try {
			remove(tokenProvider);
			LocalDateTime tokenCreatedDate = new LocalDateTime(dateFormater.parse(tokenProvider.getCreatedDateTime()));
			Period period = new Period(tokenCreatedDate, new LocalDateTime(), PeriodType.hours());
			if (period.getHours() >= (24 * CANREFRESHTOKENINTERVALDAYS)) {
				canRefresh = false;
			}
		} catch (ParseException e) {
			canRefresh = false;
		}
		return canRefresh;
	}

	/**
	 * The Class TokenProvider.
	 */
	@Getter
	public class TokenProvider {

		/** The user id. */
		private long userId;

		/** The created date time. */
		private String createdDateTime;

		/** The date formater. */
		private final SimpleDateFormat dateFormater = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss", Locale.ENGLISH);

		/** The token. */
		private String token;

		/** The user UUID **/
		private String uuid;

		/**
		 * Instantiates a new token provider.
		 *
		 * @param uuid the username
		 * @throws Exception the exception
		 */
		public TokenProvider(final String uuid) throws ApplicationException {
			this.uuid = uuid;
			this.createdDateTime = dateFormater.format(new Date());
			log.info("Token Provide .....");
			encription();
		}

		/**
		 * Encryption.
		 *
		 * @throws Exception the exception
		 */
		private void encription() throws ApplicationException {
			log.info("Encryption......");
			if (ValidationUtils.isEmptyString(uuid)) {
				throwException("Uuid should't be Null");
			}
			log.debug("Encryption Key Verification is Done");
			Key ekey = generateKey();
			try {
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.ENCRYPT_MODE, ekey);
				byte[] encodeData = null;
				encodeData = cipher.doFinal((this.uuid + ";" + this.createdDateTime).getBytes());
				this.token = Base64.encodeBase64String(encodeData).replaceAll("\n\r", "");
			} catch (NoSuchPaddingException noSuchPaddingException) {
				throwException("No such padding exception");
			} catch (NoSuchAlgorithmException noSuchAlgorithmException) {
				throwException("No such algorithm exception");
			} catch (InvalidKeyException invalidKeyException) {
				throwException("Invalid key exception");
			} catch (IllegalBlockSizeException illegalBlockSizeException) {
				throwException("Illegal block exception");
			} catch (BadPaddingException badPaddingException) {
				throwException("Bad padding exception");
			}
		}

		/**
		 * Decription.
		 *
		 * @throws Exception the exception
		 */
		private void decription() throws ApplicationException {
			byte[] decodeDate = Base64.decodeBase64((this.token + "\n\r").getBytes());
			Key ekey = generateKey();
			Cipher cipher;
			try {
				cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.DECRYPT_MODE, ekey);
				decodeDate = cipher.doFinal(decodeDate);
				String tokenData = new String(decodeDate);
				String[] dataArray = tokenData.split(";");
				if (dataArray == null || dataArray.length < 2)
					throwException("Invalid token data");
				this.uuid = dataArray[0];
				this.createdDateTime = dataArray[1];
			} catch (NoSuchPaddingException noSuchPaddingException) {
				throwException("No such padding exception");
			} catch (NoSuchAlgorithmException noSuchAlgorithmException) {
				throwException("No such algorithm exception");
			} catch (InvalidKeyException invalidKeyException) {
				throwException("Invalid key exception");
			} catch (IllegalBlockSizeException illegalBlockSizeException) {
				throwException("Illegal block exception");
			} catch (BadPaddingException badPaddingException) {
				throwException("Bad padding exception");
			}
		}

		/**
		 * Generate key.
		 *
		 * @return the key
		 * @throws ApplicationException
		 */
		private Key generateKey() throws ApplicationException {
			//String encryptionKey = configReader.getEncryptionKey();
			if(encryptionKey == null || encryptionKey.isEmpty()) {
				log.error("Encryption Key should not be null or Empty");
				throwException("Encryption Key should not be null or Empty");
			}
			byte[] key = Arrays.copyOf(encryptionKey.getBytes(), 32);
			return new SecretKeySpec(key, "AES");

		}

		/**
		 * Gets the user id.
		 *
		 * @return the user id
		 */
		public long getUserId() {
			return userId;
		}

		/**
		 * Sets the user id.
		 *
		 * @param userId the new user id
		 */
		public void setUserId(final long userId) {
			this.userId = userId;
		}

		/**
		 * Gets the token.
		 *
		 * @return the token
		 */
		public String getToken() {
			return token;
		}

		/**
		 * Sets the token.
		 *
		 * @param token the new token
		 */
		public void setToken(final String token) {
			this.token = token;
		}

		/**
		 * Gets the created date time.
		 *
		 * @return the created date time
		 */
		public String getCreatedDateTime() {
			return createdDateTime;
		}

		/**
		 * API to throw new Application Exception
		 *
		 * @param message
		 * @throws ApplicationException
		 */
		private void throwException(String message) throws ApplicationException {
			throw new ApplicationException(Constants.ERROR_STATUS, "Internal Error", message);
		}

	}

	/**
	 * Cache.
	 * 
	 * @param token    Generated token for Google login
	 * @param user     object of user class
	 * @throws JsonProcessingException
	 */
	public void cache(String token, User user) throws ApplicationException {
		// TODO Auto-generated method stub
		redisSerive.set(token, new Gson().toJson(user));
	}
}
