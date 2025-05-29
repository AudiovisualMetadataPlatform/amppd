package edu.indiana.dlib.amppd.service.impl;

import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.config.AmppdUiPropertyConfig;
import edu.indiana.dlib.amppd.service.HmgmAuthService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of HmgmAuthService.
 */
@Service
@Slf4j
public class HmgmAuthServiceImpl implements HmgmAuthService {

	@Autowired
	private AmppdUiPropertyConfig amppdUiPropertyConfig;
	
	/**
	 * @see edu.indiana.dlib.amppd.service.HmgmAuthService.validateAuthString(String, String, String)
	 */
	@Override
	public String validateAuthString(String editorInput, String userPass, String authString) {
		/* The original method returns boolean on validation instead of the HMGM token, so the frontend has to generate the token.
		 * This is not best practice. We should eliminate/minimize mutual dependencies between frontend/backend code,
		 * and leave business logic to the backend only. This is especially desirable in the case of authentication.
		 */

		if (!checkHash(editorInput, userPass, authString)) {
			log.error("HMGM authentication failed with invalid auth string for editor input file " + editorInput);
			return null;
		}

		log.info("HMGM authentication succeeded with valid auth string for editor input file " + editorInput);
		return toHmgmToken(editorInput, userPass, authString);
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.HmgmAuthService.validateAuthString(String)
	 */
	@Override
	public String validateHmgmToken(String hmgmToken) {	
		String[] fields = toAuthFields(hmgmToken);
		String editorInput = fields[0];
		String userPass = fields[1];
		String authString = fields[2];	
		
		if (!checkHash(editorInput, userPass, authString)) {
			log.error("HMGM authentication failed with invalid HMGM token for editor input file " + editorInput);
			return null;
		}		

		log.info("HMGM authentication succeeded with valid HMGM token for editor input file " + editorInput);
		return hmgmToken;					
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.HmgmAuthService.validateHmgmTokenAuthString(String, String, String)
	 */
	@Override
	public String validateHmgmTokenAuthString(String hmgmToken, String editorInput, String authString) {
		String[] fields = toAuthFields(hmgmToken);		
		String userPass = fields[1];

		if (!editorInput.equals(fields[0]) || !authString.equals(fields[2]) || !checkHash(editorInput, userPass, authString)) {
			log.error("HMGM authentication failed with invalid HMGM token or unmatching auth string for editor input file " + editorInput);
			return null;		
		}		
				
		log.info("HMGM authentication succeeded with valid HMGM token and matching auth string for editor input file " + editorInput);
		return hmgmToken;					
	}
		
	private String shaHhash(String userPass, String editorInput) throws NoSuchAlgorithmException {
		String originalString = userPass + editorInput + amppdUiPropertyConfig.getHmgmSecretKey();
		return DigestUtils.sha256Hex(originalString);
	}
	
	private boolean checkHash(String editorInput, String userPass, String authString) {
		try {
			String hash = shaHhash(userPass, editorInput);
			return authString.equals(hash);
		}
		catch(Exception ex) {
			log.error("Exception while comparing auth string for HMGM editor input file " + editorInput, ex);
			return false;			
		}		
	}
	
	private String toHmgmToken(String editorInput, String userPass, String authString) {
		return editorInput + AUTH_SEPARATOR + userPass + AUTH_SEPARATOR + authString;
	}
	
	private String[] toAuthFields(String hmgmToken) {
		return hmgmToken.split(HmgmAuthService.AUTH_SEPARATOR);
	}
	
}
