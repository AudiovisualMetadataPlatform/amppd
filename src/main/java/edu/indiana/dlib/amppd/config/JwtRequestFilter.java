package edu.indiana.dlib.amppd.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.AuthService;
import io.jsonwebtoken.ExpiredJwtException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	@Autowired
	private AmpUserService jwtUserDetailsService;
	
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private AmppdUiPropertyConfig amppdUIConfig;
	

	@Autowired
	private AuthService authService;
	
	
	private void CreateAnonymousAuth(HttpServletRequest request) {
		AmpUser userDetails = new AmpUser();
		userDetails.setEmail("none");
		userDetails.setUsername("");
	
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				userDetails, null, null);

		usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
	}
	private boolean ValidRefUrl(String referer) {
		String cleanedRef = referer.replace("https://", "").replace("#/", "").replace("#", "").replace("localhost", "127.0.0.1");
		String cleanedUiUrl = amppdUIConfig.getUrl().replace("https://", "").replace("#/", "").replace("#", "").replace("localhost", "127.0.0.1") + "timeliner.html";
		logger.debug(cleanedUiUrl + " starts with " + cleanedRef + " : " + cleanedUiUrl.startsWith(cleanedRef));
		return cleanedRef.startsWith(cleanedUiUrl);
	}
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		final String requestTokenHeader = request.getHeader("authorization");
			
		String username = null;
		String jwtToken = null;

		String authToken = null;
		
		
		if(ValidRefUrl(request.getHeader("referer"))) {
			CreateAnonymousAuth(request);
		}
		else if (requestTokenHeader != null && requestTokenHeader.startsWith("AMPPD ")) {
			authToken = requestTokenHeader.substring(6);
			String[] parts = authToken.split(";;;;");
			String editorInput = parts[0];
			String userToken = parts[1];
			String authString = parts[2];
			
			if(authService.compareAuthStrings(authString, userToken, editorInput)){
				CreateAnonymousAuth(request);
			}
			
		}
		else {
			if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
				jwtToken = requestTokenHeader.substring(7);
				try {
					username = jwtTokenUtil.getUsernameFromToken(jwtToken);
				} catch (IllegalArgumentException e) {
					System.out.println("Unable to get JWT Token");
				} catch (ExpiredJwtException e) {
					System.out.println("JWT Token has expired");
				}
			} else {
				logger.warn("JWT Token does not begin with Bearer String");
			}
		
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				AmpUser userDetails = this.jwtUserDetailsService.getUser(username);
			
				if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
							userDetails, null, null);/*userDetails.getAuthorities()*/
			
					usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			
					SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
				}
		
			}
		}
	
		chain.doFilter(request, response);
	}

}