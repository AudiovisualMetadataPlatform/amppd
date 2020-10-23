package edu.indiana.dlib.amppd.service.impl;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.config.AmppdUiPropertyConfig;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.AuthService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

	@Autowired
	private AmppdUiPropertyConfig amppdUiPropertyConfig;
	
	@Autowired
	private AmpUserService ampUserService;
	
	@Override
	public boolean compareAuthStrings(String authString, String userToken, String editorInput) {
		try {
			String hash = hashValues(userToken, editorInput);
			return hash.equals(authString);
		}
		catch(Exception ex) {
			log.error("Error comparing auth string: " + ex);
		}
		return false;
	}
	
	private String hashValues(String userToken, String editorInput) throws NoSuchAlgorithmException {
		String originalString = userToken + editorInput + amppdUiPropertyConfig.getHmgmSecretKey();
		return org.apache.commons.codec.digest.DigestUtils.sha256Hex(originalString);
	}
}
