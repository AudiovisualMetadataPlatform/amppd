package edu.indiana.dlib.amppd.config;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for persistence layer.
 *
 */
@ConfigurationProperties(prefix="spring.mail")
@Validated
@Getter
@Setter

public class MailConfig {	
	    @NotNull private String host;
	    @NotNull private String port;  
	    @NotNull private String username;
	    @NotNull private String password; 
	    @NotNull private String uiUrl;
}
