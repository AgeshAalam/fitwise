package com.fitwise.authentication;

import org.springframework.security.core.GrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 
 * @author npc
 *
 */
@NoArgsConstructor
@AllArgsConstructor
public class CustomAuthority implements GrantedAuthority {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Role 
	 */
	private String role;
	
	/**
	 * Setter for role
	 * @param role
	 */
	public void setAuthority(final String role) {
		this.role = role;
	}
	
	/**
	 * to get role
	 */
	@Override
	public String getAuthority() {
		return role;
	}

}
