package edu.indiana.dlib.amppd.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

import lombok.extern.java.Log;
import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for encryption.
 * @author vinitab
 *
 */

@Validated
@Getter
@Setter
public class MD5Encryption {

    private static String secret;
    
	@Autowired
	public MD5Encryption(AmppdPropertyConfig amppdconfig) {
		secret = amppdconfig.getEncryptionSecret();		
	}

    
    /**
     * Get the Md5 encryption for input string.
     * @return 
     */
    public static String getMd5(String input) 
    { 
    	input = secret+input;
        try { 
  
            MessageDigest md = MessageDigest.getInstance("MD5"); 
            byte[] messageDigest = md.digest(input.getBytes()); 
  
            // Convert byte array into signum representation 
            BigInteger no = new BigInteger(1, messageDigest); 
  
            // Convert message digest into hex value 
            String hashtext = no.toString(16); 
            while (hashtext.length() < 32) { 
                hashtext = "0" + hashtext; 
            } 
            return hashtext; 
        }  
  
        catch (NoSuchAlgorithmException e) { 
            throw new RuntimeException(e); 
        } 
    } 
        
}
