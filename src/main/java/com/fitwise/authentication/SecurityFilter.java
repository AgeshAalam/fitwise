package com.fitwise.authentication;

import com.fitwise.constants.Constants;
import com.fitwise.constants.SecurityFilterConstants;
import com.google.api.client.http.HttpMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The Class SecurityFilter.
 */
public class SecurityFilter extends OncePerRequestFilter {

    /**
     * The auth manager.
     */
    @Autowired
    final private AuthenticationManager authManager;

    /**
     * Instantiates a new security filter.
     *
     * @param authManager the auth manager
     */
    public SecurityFilter(final AuthenticationManager authManager) {
        super();
        this.authManager = authManager;
    }

	/**
	 * doFilterInternal
	 *
	 * @see org.springframework.web.filter.OncePerRequestFilter
	 *      #doFilterInternal(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain filterChain) throws ServletException, IOException {
		response.setHeader("Access-Control-Allow-Origin"  , "*"                               );
		response.setHeader("Access-Control-Allow-Methods" , "POST, PUT, GET, OPTIONS, DELETE" );
		response.setHeader("Access-Control-Allow-Headers" , "x-authorization, Content-Type"     );
		boolean isContinue = true;
		String extlToken = request.getHeader(Constants.X_AUTHORIZATION);
		if(!request.getMethod().equalsIgnoreCase(HttpMethods.OPTIONS) && (extlToken == null || extlToken.isEmpty()) && !SecurityFilterConstants.URL_NO_AUTH.contains(request.getRequestURI()) && !request.getRequestURI().startsWith("/webjars") && !request.getRequestURI().startsWith("/images") && !request.getRequestURI().startsWith("/configuration") && !request.getRequestURI().startsWith("/v2") && !request.getRequestURI().startsWith("/swagger-resources")) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			isContinue = false;
		}
		if (extlToken != null && extlToken.trim().length() > 0 && !request.getMethod().equalsIgnoreCase(HttpMethods.OPTIONS)) {
			TokenAuthentication tokenAuthentication = new TokenAuthentication(null, extlToken);
			Authentication authentication = authManager.authenticate(tokenAuthentication);
			if (authentication == null || !authentication.isAuthenticated()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Your session has been expired due to inactivity");
				isContinue = false;
			} else {
				UserNamePasswordAuthenticationToken tokenAuth = (UserNamePasswordAuthenticationToken) authentication;
				if (tokenAuth.isTokenExpired()) {
					response.sendError(498);
					isContinue = false;
				}
			}
		}
		if(request.getMethod().equalsIgnoreCase(HttpMethods.OPTIONS)){
			response.setStatus(HttpServletResponse.SC_OK);
		}else if(isContinue) {
			filterChain.doFilter(request, response);
		}
	}

}
