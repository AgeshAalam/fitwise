package com.fitwise.authentication;

import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.UserRole;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.UserRepository;
import com.fitwise.service.TokenService;
import com.fitwise.utils.AppUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The Class AuthProvider.
 */
@Component
@Slf4j
public class AuthenticationProvider
		implements org.springframework.security.authentication.AuthenticationProvider, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Autowired
	private UserRepository userRepo;

	/** The token service. */
	@Autowired
	private TokenService tokenService;

	/**
	 * authenticate
	 * 
	 * @see org.springframework.security.authentication.AuthenticationProvider
	 *      #authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(final Authentication auth) throws AuthenticationException {
		boolean goFurther = true;
		Authentication authentication = auth;
		UserNamePasswordAuthenticationToken usernamePasswordAuthentication = (UserNamePasswordAuthenticationToken) authentication;

		UserDetails userDetails = (UserDetails) usernamePasswordAuthentication.getPrincipal();
		String password = (String) usernamePasswordAuthentication.getCredentials();
		boolean isSocialLogin = usernamePasswordAuthentication.isSocialLogin();
		try {
			User user = userRepo.findByEmail(userDetails.getUsername());
			if (user == null) {
				usernamePasswordAuthentication.setAuthenticated(false);
				goFurther = false;
			}
			boolean checkPassword = goFurther && BCrypt.checkpw(password, userDetails.getPassword());
			if (checkPassword) {
				usernamePasswordAuthentication.setAuthenticated(true);
				// Generating Token with UserName and TimeStamp
				TokenService.TokenProvider tokenProvider = tokenService.new TokenProvider(
						String.valueOf(userDetails.getUsername()));
				usernamePasswordAuthentication.setToken(tokenProvider.getToken());
				Set<String> roles = new HashSet<>();
				Set<GrantedAuthority> grantedAuthorities = (Set<GrantedAuthority>) userDetails.getAuthorities();
				for (GrantedAuthority authority : grantedAuthorities) {
					roles.add(authority.getAuthority());
				}
				usernamePasswordAuthentication.setRole(roles);
				// Storing Token in Redis for further authentication puropose
				tokenService.cache(tokenProvider.getToken(), usernamePasswordAuthentication);
				authentication = usernamePasswordAuthentication;
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else if (isSocialLogin) {
				usernamePasswordAuthentication.setAuthenticated(true);
				// Generating Token with UserName and TimeStamp
				TokenService.TokenProvider tokenProvider = tokenService.new TokenProvider(
						String.valueOf(userDetails.getUsername()));
				usernamePasswordAuthentication.setToken(tokenProvider.getToken());
				Set<String> roles = new HashSet<>();
				for (UserRole role : AppUtils.getUserRoles(user)) { //AKHIL //user.getRole()) {
					roles.add(role.getName());
				}
				usernamePasswordAuthentication.setRole(roles);
				// Storing Token in Redis for further authentication puropose
				tokenService.cache(tokenProvider.getToken(), usernamePasswordAuthentication);
				authentication = usernamePasswordAuthentication;
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else if (goFurther) {
				UsernamePasswordAuthenticationToken passwordWrong = new UsernamePasswordAuthenticationToken(
						userDetails, userDetails.getPassword());
				passwordWrong.setAuthenticated(false);
			}
		} catch (AuthenticationException exception) {
			usernamePasswordAuthentication.setAuthenticated(false);
		} catch (ApplicationException exception) {
			log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
		}
		return usernamePasswordAuthentication;
	}

	/**
	 * supports.
	 *
	 * @param authentication the auth
	 * @return user authentication
	 * @see org.springframework.security.authentication.AuthenticationProvider
	 *      #supports(java.lang.Class)
	 */
	@Override
	public boolean supports(final Class<?> authentication) {
		return authentication.equals(UserNamePasswordAuthenticationToken.class);
	}

}
