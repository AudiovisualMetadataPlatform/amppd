package edu.indiana.dlib.amppd.config;


import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;


/**
 * Configuration for persistence layer.
 * @author adeelahmad
 *
 */
@ConfigurationProperties(prefix="galaxy")
@Validated
@Getter
@Setter
public class GalaxyPropertyConfig {

    @NotNull private String host = "localhost";
    @NotNull private String port = "8300";
    @NotNull private String user = "amppd";
    @NotNull private String key;
    @NotNull private String workflowrApi = "/api/workflows";
    
    /**
     * Get the base URL of Galaxy application.
     * @return
     */
    public String getBaseUrl() {
    	return "http://" + host + ':' + port;
    }
    
    /**
     * Get the URL for workflow Rest API.
     * @return
     */
    public String getWorkflowUrl() {
    	return getBaseUrl() + workflowrApi;
    }
    
}
