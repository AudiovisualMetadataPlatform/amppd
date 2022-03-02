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

    @NotNull private String host;
    @NotNull private Integer port;
    @NotNull private String root;
    @NotNull private String userId;
    @NotNull private String username;
    @NotNull private String password;
    @NotNull private String usernameWorkflowEdit;
    @NotNull private String passwordWorkflowEdit;
    
    /**
     * Get the base URL of Galaxy application at the original root ("/")
     * @return
     */
    public String getBaseUrl() {
    	return "http://" + host + ':' + port;
    }

    /**
     * Get the base URL of Galaxy application at the mounted root.
     * @return
     */
    public String getRootUrl() {
    	return "http://" + host + ':' + port + root;
    }
    
    /**
     * Get the base API URL of Galaxy application.
     * @return
     */
    public String getApiUrl() {
    	return getBaseUrl() + "/api";
    }
            

}
