package edu.indiana.dlib.amppd.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.service.AmpUserService;
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

	@Autowired
	private AmpUserService ampUserService;	
	
	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(amppdPropertyConfig.getJwtSecret()).parseClaimsJws(token).getBody();
	}

	private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	private <T> T getClaimFromClaims(Claims claims, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(claims);
	}

	private String getUsernameFromClaims(Claims claims) {
		return getClaimFromClaims(claims, Claims::getSubject);
	}

	private String getIssuerFromClaims(Claims claims) {
		return getClaimFromClaims(claims, Claims::getIssuer);
	}

	private Date getExpirationDateFromClaims(Claims claims) {
		return getClaimFromClaims(claims, Claims::getExpiration);
	}

	private Boolean isClaimsIssuedBy(Claims claims, String issuedBy) {
		final String issuer = getIssuerFromClaims(claims);
		return StringUtils.equals(issuer, issuedBy);
	}

	private String getJwtFromClaims(Claims claims) {
		return (String)claims.get(CLAIM_JWT);
	}

	private String getWorkflowtFromClaims(Claims claims) {
		return (String)claims.get(CLAIM_WORKFLOW);
	}
	
	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

//	public String getIssuerFromToken(String token) {
//		return getClaimFromToken(token, Claims::getIssuer);
//	}
//
//	public Date getExpirationDateFromToken(String token) {
//		return getClaimFromToken(token, Claims::getExpiration);
//	}
//
//	public Boolean isTokenIssuedBy(String token, String issuedBy) {
//		final String issuer = getIssuerFromToken(token);
//		return StringUtils.equals(issuer, issuedBy);
//	}
//		
//	public Boolean isTokenExpired(String token) {
//		final Date expiration = getExpirationDateFromToken(token);
//		return expiration.before(new Date());	
//	}
	
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
		// Note: 
		// For simplicity, both JWT for AMP authentication and workflow edit use the same secret,
		// amppd.workflowEditSecret is currently not in use.
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
	 * @param token the given AMP authentication token
	 * @return AMP user for the JWT if token is valid; null otherwise
	 */
	public AmpUser validateToken(String token) {
		// get all claims to avoid parsing the token for each claim, and also ensure token is a valid JWT
		Claims claims = null;
		try {
			claims = getAllClaimsFromToken(token);
		}
		catch (Exception e) {
			// catch all possible exceptions during JWS parsing, including ExpiredJwtException
			log.error("AMP authentication JWT validation failed: invalid JWT");
			return null;
		}
		
		// username shall not be empty
		String username = this.getUsernameFromClaims(claims);
		if (StringUtils.isEmpty(username)) {
			log.error("AMP authentication JWT validation failed: empty username");
			return null;
		}

		// user by username shall exist in AMP
		AmpUser user = ampUserService.getUser(username);
		if (user == null) {
			log.error("AMP authentication JWT validation failed: non-existing user");
			return null;
		}		

		// issuer shall be AMP Auth
		if (!isClaimsIssuedBy(claims, ISSUER_AMP_AUTH)) {
			log.error("AMP authentication JWT validation failed: invalid issuer");
			return null;
		}

		// no need to check expiration as that would have been caught with ExpiredJwtException
//		if (isTokenExpired(token)) {
//			log.error("AMP authentication JWT validation failed: token has expired.");
//			return null;
//		}

		return user;
	}
		
	/**
	 * Generate a JWT token for editing the given workflow with the given JWT token for the current user session.
	 * @param jwtToken AMP authentication JWT token for the current user session
	 * @param workflowId ID of the given workflow
	 * @return
	 */
	public String generateWorkflowEditToken(String jwtToken, String workflowId) {
		Map<String, Object> claims = new HashMap<>();	
		claims.put(CLAIM_JWT, jwtToken);
		claims.put(CLAIM_WORKFLOW, workflowId);
		String username = getUsernameFromToken(jwtToken);
		log.info("Created workflow edit token: username = " + username + ", workflowId = " + workflowId);
		return generateToken(claims, username, ISSUER_WORKFLOW_EDIT, amppdPropertyConfig.getWorkflowEditMinutes());
	}
	
	/**
	 * Validate the given JWT token for workflow edit session. 
	 * @param wfeToken the given workflow edit token
	 * @return an ImmutablePair of AMP user and workflow ID included in the token if valid; null otherwise.
	 */
	public ImmutablePair<AmpUser, String> validateWorkflowEditToken(String wfeToken) {		
		// get all claims to avoid parsing the token for each claim, and also ensure token is a valid JWT
		Claims claims = null;
		try {
			claims = getAllClaimsFromToken(wfeToken);
		}
		catch (Exception e) {
			// catch all possible exceptions during JWS parsing, including ExpiredJwtException
			log.error("Workflow edit JWT validation failed: invalid JWT");
			return null;
		}		
				
		// AMP authentication JWT shall be valid
		String jwt = getJwtFromClaims(claims);
		AmpUser user = validateToken(jwt);
		if (user == null) {
			log.error("Workflow edit JWT validation failed: invalid AMP authentication JWT");
			return null;
		}
		
		// username from the workflow edit JWT shall be the same as that from the AMP authentication JWT
		String username = getUsernameFromClaims(claims);
		if (!user.getUsername().equals(username)) {
			log.error("Workflow edit JWT validation failed: username from the workflow edit JWT is not the same as that from the AMP authentication JWT");
			return null;
		}
		
		// workflow ID shall not be empty
		String workflowId = getWorkflowtFromClaims(claims);
		if (StringUtils.isEmpty(workflowId)) {
			log.error("Workflow edit JWT validation failed: workflow ID is empty");
			return null;
		}
		
		// issuer shall be Workflow Edit
		if (!isClaimsIssuedBy(claims, ISSUER_WORKFLOW_EDIT)) {
			log.error("Workflow edit JWT validation failed: invalid issuer");
			return null;
		}
		
		return ImmutablePair.of(user,  workflowId);
	}
	
}