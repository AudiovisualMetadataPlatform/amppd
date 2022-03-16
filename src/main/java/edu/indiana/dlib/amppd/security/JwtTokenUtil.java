package edu.indiana.dlib.amppd.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.AmpUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil {
	
	// issuer for AMP user authentication token and workflow edit token
	public static final String ISSUER_AMP_AUTH = "amppd";
	public static final String ISSUER_WORKFLOW_EDIT = "ampwf";	

	// claim fields for workflow edit token
	public static final String CLAIM_JWT = "jwt";
	public static final String CLAIM_WORKFLOW = "workflow";	
	
	@Autowired
	private AmppdPropertyConfig amppdPropertyConfig;	

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
	
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver, String secret) {
		final Claims claims = getAllClaimsFromToken(token, secret);
		return claimsResolver.apply(claims);
	}

	public Claims getAllClaimsFromToken(String token, String secret) {
		return Jwts.parser().setSigningKey(amppdPropertyConfig.getJwtSecret()).parseClaimsJws(token).getBody();
	}

	/**
	 * Retrieve token from the given Authorization header, or null if header is invalid.
	 * @param authHeader 
	 */
	public String retrieveToken(String authHeader) {
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7);
		}
		else {
			return null;
		}
	}

	public String generateToken(Map<String, Object> claims, String subject, String issuer, int expireMinutes, String secret) {	
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuer(issuer)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000))
				.signWith(SignatureAlgorithm.HS512, secret).compact();
	
	}
	
	public String generateAmpAuthToken(String username) {
		Map<String, Object> claims = new HashMap<>();	
		return generateToken(claims, username, ISSUER_AMP_AUTH, amppdPropertyConfig.getJwtExpireMinutes(), amppdPropertyConfig.getJwtSecret());
	}
		
	public Boolean validateAmpAuthToken(String token, AmpUser userDetails) {
		final String username = getUsernameFromToken(token);
		final String issuer = this.getIssuerFromToken(token);
		boolean valid = userDetails != null && username.equals(userDetails.getUsername()) && 
				ISSUER_AMP_AUTH.equals(issuer) &&
				!isTokenExpired(token);
		return valid;
	}
		
	public String generateWorkflowEditToken(String jwtToken, String workflowId) {
		Map<String, Object> claims = new HashMap<>();	
		claims.put("jwt", jwtToken);
		claims.put("workflow", workflowId);
		String username = getUsernameFromToken(jwtToken);
		return generateToken(claims, username, ISSUER_WORKFLOW_EDIT, amppdPropertyConfig.getWorkflowEditMinutes(), amppdPropertyConfig.getWorkflowEditSecret());
	}
	
}