package edu.indiana.dlib.amppd.security.unused;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Spring FactoryBean to allow flexibility on Bootstrapping the RestTemplate into the Spring context with Basic Authentication
 * @author yingfeng
 *
 */
@Component
@Data
@NoArgsConstructor
public class RestTemplateFactory implements FactoryBean<RestTemplate>, InitializingBean {
	@Autowired
	private GalaxyPropertyConfig config;

    private RestTemplate restTemplate; 
	private String host;
	private Integer port;
	
    public RestTemplateFactory(String host, Integer port) {
    	this.host = host;
    	this.port = port;
    }
    
    public RestTemplate getObject() {
        return restTemplate;
    }
    
    public Class<RestTemplate> getObjectType() {
        return RestTemplate.class;
    }
    
    public boolean isSingleton() {
        return true;
    }
 
    public void afterPropertiesSet() {
    	/* TODO
    	 * Somehow this part doesn't work because the constructor with host/port never got called upon SpringContext init, thus the host/port are always null.
    	 * Once this issue is fixed, the below commented code shall replace the hardcoded host/port from config. The purpose of having customizable host/port 
    	 * is so that this class can be used for other service than what's specified in the config (for ex, for both Amppd and Galaxy REST Template)
    	 */
//        HttpHost hhost = new HttpHost(host, port, "http");
    	HttpHost hhost = new HttpHost(config.getHost(), config.getPort(), "http");
        restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactoryBasicAuth(hhost));
    }
    
}