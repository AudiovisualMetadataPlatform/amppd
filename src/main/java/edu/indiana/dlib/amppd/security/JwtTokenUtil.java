package edu.indiana.dlib.amppd.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.controller.WorkflowEditController;
import edu.indiana.dlib.amppd.model.AmpUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenUtil {
	
	// issuer for AMP user authentication token and workflow edit token
	public static final String ISSUER_AMP_AUTH = "amppd";
	public static final String ISSUER_WORKFLOW_EDIT = "ampwf";	

	// claim fields for workflow edit token
	public static final String CLAIM_JWT = "jwt";
	public static final String CLAIM_WORKFLOW = "workflow";	
	
	@Autowired
	private AmppdPropertyConfig amppdPropertyConfig;	

	public Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(amppdPropertyConfig.getJwtSecret()).parseClaimsJws(token).getBody();
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	public String getIssuerFromToken(String token) {
		return getClaimFromToken(token, Claims::getIssuer);
	}

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());	
	}
	
	/**
	 * Generate a JWT with the given claims, subject, issuer, expiration period, and the predefined encryption secret.
	 * @param claims
	 * @param subject
	 * @param issuer
	 * @param expireMinutes
	 * @return
	 */
	public String generateToken(Map<String, Object> claims, String subject, String issuer, int expireMinutes) {	
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuer(issuer)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000))
				.signWith(SignatureAlgorithm.HS512, amppdPropertyConfig.getJwtSecret()).compact();	
	}
	
	/**
	 * Generate a JWT token for AMP authentication for the given user.
	 * @param username
	 * @return
	 */
	public String generateToken(String username) {
		Map<String, Object> claims = new HashMap<>();	
		return generateToken(claims, username, ISSUER_AMP_AUTH, amppdPropertyConfig.getJwtExpireMinutes());
	}
		
	/**
	 * Retrieve token from the given Authorization header, or null if header is invalid.
	 * @param authHeader the given Authorization header
	 */
	public String retrieveToken(String authHeader) {
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7);
		}
		else {
			return null;
		}
	}

	/**
	 * Validate the given JWT token for AMP user authentication. 
	 * @param token
	 * @param userDetails
	 * @return
	 */
	public Boolean validateToken(String token, AmpUser userDetails) {
		final String username = getUsernameFromToken(token);
		final String issuer = this.getIssuerFromToken(token);
		boolean valid = userDetails != null && username.equals(userDetails.getUsername()) && 
				ISSUER_AMP_AUTH.equals(issuer) &&
				!isTokenExpired(token);
		return valid;
	}
		
	/**
	 * Generate a JWT token for editing the given workflow with the given JWT token for the current user session.
	 * @param jwtToken AMP authentication JWT token for the current user session
	 * @param workflowId ID of the given workflow
	 * @return
	 */
	public String generateWorkflowEditToken(String jwtToken, String workflowId) {
		Map<String, Object> claims = new HashMap<>();	
		claims.put("jwt", jwtToken);
		claims.put("workflow", workflowId);
		String username = getUsernameFromToken(jwtToken);
		log.info("Created workflow edit token: username = " + username + ", workflowId = " + workflowId);
		return generateToken(claims, username, ISSUER_WORKFLOW_EDIT, amppdPropertyConfig.getWorkflowEditMinutes());
	}
	
}