package edu.indiana.dlib.amppd.config;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for AMPPD related properties.
 */
@ConfigurationProperties(prefix="avalon")
@Validated
@Getter
@Setter
public class AvalonPropertyConfig {

    @NotNull private String url;
    @NotNull private String token;
    
}
