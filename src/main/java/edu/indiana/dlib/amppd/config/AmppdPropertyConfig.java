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
@ConfigurationProperties(prefix="amppd")
@Validated
@Getter
@Setter
public class AmppdPropertyConfig {

    @NotNull private String fileStorageRoot;
    @NotNull private String dropboxRoot;
    @NotNull private String encryptionSecret;  
    @NotNull private String username; 
    @NotNull private String password; 
    @NotNull private String ampurl;
    @NotNull private String pythonPath;
    @NotNull private String mediaprobeDir;

}
