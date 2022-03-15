package edu.indiana.dlib.amppd.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.indiana.dlib.amppd.model.AmpUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil {

	@Value("${amppd.jwtSecret}")
	private String jwtSecret;

	@Value("${amppd.jwtExpireMinutes}")
	private int jwtExpireMinutes;

	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}


	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
	}

	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());	
	}
	
	public String generateToken(AmpUser userDetails) {
		Map<String, Object> claims = new HashMap<>();	
		return doGenerateToken(claims, userDetails.getUsername());
	}
	
	private String doGenerateToken(Map<String, Object> claims, String subject) {
	
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpireMinutes * 60 * 1000))
				.signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
	
	}
	public Boolean validateToken(String token, AmpUser userDetails) {
		final String username = getUsernameFromToken(token);
		return (userDetails != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
}