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

import com.google.common.net.HttpHeaders;

import edu.indiana.dlib.amppd.config.AmppdUiPropertyConfig;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.service.HmgmAuthService;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
	
	public static String NER_EDITOR_PATH = "/rest/hmgm/ner-editor";
	public static String NER_EDITOR_REFERER = "timeliner.html";
	
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
		String uri = request.getRequestURI();
		String referer = request.getHeader(HttpHeaders.REFERER);
		String nerReferer = amppdUIConfig.getUrl().replace("#", "") + NER_EDITOR_REFERER;
		boolean valid = uri.equals(NER_EDITOR_PATH) && StringUtils.equals(referer, nerReferer);
		
		// in local env, the refer URL does not include Timeliner path for some reason, just hostname. 
//		boolean valid = uri.equals(NER_EDITOR_PATH) && StringUtils.startsWith(nerReferer, referer);
		
		if (valid) {
			logger.debug("Valid NER editor request URI and referer: URI: " + uri + ", Referer: " + referer);
		}		
		return valid;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
		// authorization header
		final String requestTokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
				
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