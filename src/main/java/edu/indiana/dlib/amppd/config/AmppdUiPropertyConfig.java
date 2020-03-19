package edu.indiana.dlib.amppd.config;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;


/**
 * Configuration for AMPPD-UI related properties.
 * @author yingfeng
 *
 */
@ConfigurationProperties(prefix="amppdui")
@Validated
@Getter
@Setter
public class AmppdUiPropertyConfig {

    @NotNull private String url;
    @NotNull private String documentRoot;
    @NotNull private String symlinkDir;

}

