package com.fitwise.authentication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * The Class TokenAuthentication.
 */
public class TokenAuthentication implements Authentication{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	

	/** The user object. */
	private final Object userObject;
	
	/** The role list. */
	private final List<CustomAuthority> roleList = new ArrayList<CustomAuthority>();
	
	/** The token. */
	private final String token;
	
	/** The authenticated. */
	private boolean authenticated;
	
	/** The token expired. */
	private boolean tokenExpired;

	/**
	 * Adds the role.
	 *
	 * @param role the role
	 */
	public void addRole(final Set<String> roles){
		for (String role : roles) {
			CustomAuthority customAuthority = new CustomAuthority();
			customAuthority.setAuthority(role);
			roleList.add(customAuthority);
		}
	}
	
	/**
	 * Instantiates a new token authentication.
	 *
	 * @param userObject the user object
	 * @param token the token
	 */
	public TokenAuthentication(final Object userObject, final String token) {
		this.userObject = userObject;
		this.token = token;
	}

	/** (non-Javadoc)
	 * @see java.security.Principal#getName()
	 */
	@Override
	public String getName() {
		return this.token;
	}

	/** (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getAuthorities()
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roleList;
	}

	/** (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getCredentials()
	 */
	@Override
	public Object getCredentials() {
		return null;
	}

	/** (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getDetails()
	 */
	@Override
	public Object getDetails() {
		return this.token;
	}

	/** (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#getPrincipal()
	 */
	@Override
	public Object getPrincipal() {
		return this.userObject;
	}

	/** (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#isAuthenticated()
	 */
	@Override
	public boolean isAuthenticated() {
		return this.authenticated;
	}

	/** (non-Javadoc)
	 * @see org.springframework.security.core.Authentication#setAuthenticated(boolean)
	 */
	@Override
	public void setAuthenticated(final boolean authentication) throws IllegalArgumentException {
		this.authenticated = authentication;
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
	
}
