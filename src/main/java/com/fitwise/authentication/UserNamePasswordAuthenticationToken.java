package com.fitwise.authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The Class TokenAuthentication.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserNamePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The user object. */
	private final Object userObject;

	/** The role list. */
	private final List<CustomAuthority> roleList = new ArrayList<CustomAuthority>();

	/** The token. */
	private String token;

	private final String credential;

	/** The authenticated. */
	private boolean isAuthenticated;

	/** The token expired. */
	private boolean tokenExpired;

	/** The Social login. */
	private boolean isSocialLogin;

	/**
	 * Sets the.
	 *
	 * @param roles the role
	 */
	public void setRole(final Set<String> roles){
		for (String role : roles) {
			CustomAuthority customAuthority = new CustomAuthority();
			customAuthority.setAuthority(role);
			roleList.add(customAuthority);
		}
	}

	public UserNamePasswordAuthenticationToken(final Object principal, final String credential, final String token, final boolean isSocialLogin) {
		super(principal, credential);
		this.userObject = principal;
		this.credential = credential;
		this.token = token;
		this.isSocialLogin = isSocialLogin;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.security.Principal#getName()
	 */
	@Override
	public String getName() {
		return this.token;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#getAuthorities()
	 */

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#getCredentials()
	 */
	@Override
	public Object getCredentials() {
		return this.credential;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#getDetails()
	 */
	@Override
	public Object getDetails() {
		return this.token;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#getPrincipal()
	 */
	@Override
	public Object getPrincipal() {
		return this.userObject;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#isAuthenticated()
	 */
	@Override
	public boolean isAuthenticated() {
		return this.isAuthenticated;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.Authentication#setAuthenticated(boolean)
	 */
	@Override
	public void setAuthenticated(final boolean authentication) throws IllegalArgumentException {
		this.isAuthenticated = authentication;
	}

	/**
	 * Checks if is token expired.
	 *
	 * @return true, if is token expired
	 */
	public boolean isTokenExpired() {
		return tokenExpired;
	}

	/**
	 * Sets the token expired.
	 *
	 * @param tokenExpired the new token expired
	 */
	public void setTokenExpired(final boolean tokenExpired) {
		this.tokenExpired = tokenExpired;
	}

	public void setToken(final String token) {

		this.token = token;
	}

	public boolean isSocialLogin() {
		return isSocialLogin;
	}

	public void setSocialLogin(boolean isSocialLogin) {
		this.isSocialLogin = isSocialLogin;
	}

	
	
}
