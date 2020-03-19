package edu.indiana.dlib.amppd.config;


import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;


/**
 * Configuration for Galaxy related properties.
 * @author adeelahmad
 *
 */
@ConfigurationProperties(prefix="galaxy")
@Validated
@Getter
@Setter
public class GalaxyPropertyConfig {

    @NotNull private String host = "localhost";
    @NotNull private Integer port = 8300;
    @NotNull private String username;
    @NotNull private String password;
    
    /**
     * Get the base URL of Galaxy application.
     * @return
     */
    public String getBaseUrl() {
    	return "http://" + host + ':' + port;
    }
    
    /**
     * Get the base REST API URL of Galaxy application.
     * @return
     */
    public String getApiUrl() {
    	return getBaseUrl() + "/api";
    }
        
}
