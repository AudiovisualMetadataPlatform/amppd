package edu.indiana.dlib.amppd.controller;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.config.JwtTokenUtil;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.web.AuthResponse;
import edu.indiana.dlib.amppd.web.JwtRequest;
import edu.indiana.dlib.amppd.web.JwtResponse;


@RestController
@CrossOrigin
public class JwtAuthenticationController {
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private AmpUserService userDetailsService;

	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
		AuthResponse response = userDetailsService.validate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
		
		if(!response.isSuccess()) {
			throw new Exception("INVALID_CREDENTIALS");
		}
		
		final AmpUser userDetails = userDetailsService.getUser(authenticationRequest.getUsername());
		final String token = jwtTokenUtil.generateToken(userDetails);
		return ResponseEntity.ok(new JwtResponse(token));
	}

}
