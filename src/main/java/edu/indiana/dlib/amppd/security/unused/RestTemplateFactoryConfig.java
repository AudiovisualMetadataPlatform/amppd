package edu.indiana.dlib.amppd.security.unused;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;

/**
 * Configuration for generating a RestTemplate for a particular REST API via RestTemplateFactory.
 * @author yingfeng
 *
 */
@Configuration
public class RestTemplateFactoryConfig {
	
	@Autowired
	private GalaxyPropertyConfig config;
 
    @Bean(name = "galaxyFactory")
    public RestTemplateFactory galaxyFactory() {
    	RestTemplateFactory factory = new RestTemplateFactory(config.getHost(), config.getPort());
        return factory;
    }
 
    @Bean(name = "galaxyRestTemplate")
    public RestTemplate galaxyRestTemplate() throws Exception {
        return galaxyFactory().getObject();
    }
    
    // TODO we can add factory/restTemplate for Amppd as needed
}
