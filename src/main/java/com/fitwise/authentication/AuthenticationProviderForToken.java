package com.fitwise.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fitwise.exception.ApplicationException;
import com.fitwise.service.TokenService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthenticationProviderForToken implements AuthenticationProvider {

	/** The token service. */
	@Autowired
	private TokenService tokenService;
	
	/**
	 * authenticate.
	 *
	 * @see org.springframework.security.authentication.AuthenticationProvider
	 *      #authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(final Authentication auth) throws AuthenticationException {
		Authentication authentication = auth;
		String token = (String) ((TokenAuthentication) authentication).getDetails();
		if (token == null || token.trim().length() == 0) {
			authentication.setAuthenticated(false);
		}else {
		
			UserNamePasswordAuthenticationToken tokenAuthentication = null;
			try {
				tokenAuthentication = tokenService.getAuthentication(token);
				
			
			} catch (ApplicationException e) {
				log.error(e.getMessage());
				log.error(e.getError());
			}
			if (tokenAuthentication == null || !tokenAuthentication.isAuthenticated()) {
				authentication.setAuthenticated(false);
			} else {
				SecurityContextHolder.getContext().setAuthentication(tokenAuthentication);
				authentication = tokenAuthentication;
				// log.debug("Valid Token");
			}
		}
		return authentication;
	}

	/**
	 * Which authentication class should support for this authentication provider
	 */
	@Override
	public boolean supports(final Class<?> authentication) {
		return authentication.equals(TokenAuthentication.class);
	}
}
