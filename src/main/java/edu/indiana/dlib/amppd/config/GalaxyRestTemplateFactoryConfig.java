package edu.indiana.dlib.amppd.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import edu.indiana.dlib.amppd.web.GalaxyRestTemplateFactory;

/**
 * Configuration for generating a RestTemplate for a particular REST API via GalaxyRestTemplateFactory.
 * @author yingfeng
 *
 */
@Configuration
public class GalaxyRestTemplateFactoryConfig {
	
	@Autowired
	private GalaxyPropertyConfig config;


    @Bean(name = "galaxyRestTemplate")
    public GalaxyRestTemplateFactory factory() {
//    	GalaxyRestTemplateFactory factory = new GalaxyRestTemplateFactory(config.getHost(), config.getPort());
//    	factory.setHost(config.getHost());
//      factory.setPort(config.getPort());

    	GalaxyRestTemplateFactory factory = new GalaxyRestTemplateFactory();
        return factory;
    }
 
    @Bean
    public RestTemplate restTemplate() throws Exception {
        return factory().getObject();
    }
    
}
