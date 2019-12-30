package edu.indiana.dlib.amppd.util;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.github.jmchilton.blend4j.Config;
import lombok.extern.java.Log;
import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.service.impl.FileStorageServiceImpl;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for encryption.
 * @author vinitab
 *
 */
@ConfigurationProperties(prefix="md5")
@Validated
@Getter
@Setter
@Log
public class MD5Encryption {

    @NotNull static String cookie;
    //private AmppdPropertyConfig config;
    
	@Autowired
	public MD5Encryption(AmppdPropertyConfig amppdconfig) {
		// initialize Amppd file system 
		//config = amppdconfig;
		cookie = amppdconfig.getEncryptionCookie();
		log.info("Cookie fetched ");
				
	}

    
    /**
     * Get the base URL of Galaxy application.
     * @return
     */
    public static String getMd5(String input) 
    { 
    	input = cookie+input;
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
