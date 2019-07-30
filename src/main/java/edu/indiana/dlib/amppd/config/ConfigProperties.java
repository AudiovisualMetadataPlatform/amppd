package edu.indiana.dlib.amppd.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import javax.validation.constraints.NotNull;


/**
 * Configuration for persistence layer.
 * @author adeelahmad
 *
 */
@ConfigurationProperties(prefix="amppd")
@Getter
@Setter
public class ConfigProperties {


    @NotNull private String galaxyhost;
    @NotNull private String galaxyport;
    @NotNull private String galaxykey;
    
    @NotNull private String filesysroot;

    //getters and setters
}
