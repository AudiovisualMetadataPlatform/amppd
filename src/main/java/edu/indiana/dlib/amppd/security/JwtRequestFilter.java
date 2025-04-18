package edu.indiana.dlib.amppd.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import edu.indiana.dlib.amppd.config.AmppdUiPropertyConfig;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.service.HmgmAuthService;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private AmppdUiPropertyConfig amppdUIConfig;
	
	@Autowired
	private HmgmAuthService hmgmAuthService;
	
	
	private void createAnonymousAuth(HttpServletRequest request) {
		AmpUser userDetails = new AmpUser();
		userDetails.setEmail("none");
		userDetails.setUsername("");
	
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
				new UsernamePasswordAuthenticationToken(userDetails, null, null);

		usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
	}
	
	/*
	 * Determine whether referrer and url in the given request match "whitelisted" values. 
	 * This is a temporary solution until the NER editor uses its own auth.  
	 */
	private boolean validRefUrl(HttpServletRequest request) {
		// Get the referrer and URI
		String referer = request.getHeader("referer");
		String uri = request.getRequestURI();
		logger.trace("referer: " + referer + ", uri: " + uri);	
		
		if (referer==null) return false;
		
		// Only continue if it's the NER editor
		if (!uri.equals("/rest/hmgm/ner-editor")) {
			return false;
		}
		
		// Standardize cleaning URLs to avoid oddities
		String cleanedRef = referer.replace("https://", "").replace("http://", "").replace("#/", "").replace("#", "").replace("localhost", "127.0.0.1");
		String cleanedUiUrl = amppdUIConfig.getUrl().replace("https://", "").replace("http://", "").replace("#/", "").replace("#", "").replace("localhost", "127.0.0.1");
		
		boolean valid = cleanedRef.startsWith(cleanedUiUrl);
		logger.trace("cleanedRef: " + cleanedRef + ", cleanedUiUrl: " + cleanedUiUrl + ", validRefUrl: " + valid);		
		return valid;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
		// authorization header
		final String requestTokenHeader = request.getHeader("authorization");
				
		// If it is for the HMGM NER editor with a valid referrer, create anonymous auth
		// TODO this is incomplete solution; better provide NER editor an API which returns a symlink to the input file
		if (validRefUrl(request)) {
			createAnonymousAuth(request);
			logger.debug("Anonymous authentication created for HMGM NER editor with valid referer.");
		}
		// otherwise, for HMGM related requests
		else if (requestTokenHeader != null && requestTokenHeader.startsWith(JwtTokenUtil.HMGM_AUTH_PREFIX)) {
			logger.debug("Request token starts with " + JwtTokenUtil.HMGM_AUTH_PREFIX + ", authenticating via HMGM token ... ");			
			String hmgmToken = requestTokenHeader.substring(JwtTokenUtil.HMGM_AUTH_PREFIX.length());			
			if (hmgmAuthService.validateHmgmToken(hmgmToken) != null) {
				createAnonymousAuth(request);
				logger.debug("Authentication succeeded with valid HMGM token.");
			}
			else {
				logger.error("Authentication failed with invalid HMGM token.");
			}			
		}
		// otherwise, for AMP user authentication with JWT token
		else if (!StringUtils.isEmpty(requestTokenHeader) && SecurityContextHolder.getContext().getAuthentication() == null) {
			// validate JWT token if auth is turned on 
			String jwtToken = jwtTokenUtil.retrieveToken(requestTokenHeader);			
			AmpUser user = jwtTokenUtil.validateToken(jwtToken);					
			if (user != null) {
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
						new UsernamePasswordAuthenticationToken(user, null, null);	
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
				logger.debug("Authentication succeeded with valid JWT for user " + user.getUsername());
			}
			else {
				logger.error("Authentication failed with invalid JWT.");
			}		
		}	

		chain.doFilter(request, response);
	}

}