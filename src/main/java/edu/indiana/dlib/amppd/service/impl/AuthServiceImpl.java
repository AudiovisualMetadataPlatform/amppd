package edu.indiana.dlib.amppd.service.impl;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.config.AmppdUiPropertyConfig;
import edu.indiana.dlib.amppd.service.AuthService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AuthService.
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

	@Autowired
	private AmppdUiPropertyConfig amppdUiPropertyConfig;
	
	@Override
	public String validateAuthStrings(String editorInput, String userToken, String authString) {
		/* The original method returns true/false on validation instead of the token, and requires the frontend to generate the token.
		 * This is not best practice. We should eliminate/minimize mutual dependencies between frontend/backend code,
		 * and leave business logic to the backend only. This is especially necessary in the case of authentication.
		 */
		String token = null;
		try {
			String hash = hashValues(userToken, editorInput);
			if (hash.equals(authString)) {
				token = editorInput + AUTH_SEPARATOR + userToken + AUTH_SEPARATOR + authString;
				log.info("Authentication succeeded for HMGM editor input file " + editorInput);
			}
			else {
				log.error(hash);
				log.info("Authentication failed for HMGM editor input file " + editorInput);
			}
		}
		catch(Exception ex) {
			log.error("Exception while comparing auth string for HMGM editor input file " + editorInput, ex);
		}
		return token;
	}
	
	private String hashValues(String userToken, String editorInput) throws NoSuchAlgorithmException {
		String originalString = userToken + editorInput + amppdUiPropertyConfig.getHmgmSecretKey();
		return org.apache.commons.codec.digest.DigestUtils.sha256Hex(originalString);
	}
}
