package edu.indiana.dlib.amppd.web;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Spring FactoryBean to allow flexibility on Bootstrapping the RestTemplate into the Spring context with Basic Authentication
 * @author yingfeng
 *
 */
@Component
public class RestTemplateFactory implements FactoryBean<RestTemplate>, InitializingBean {
  
    private RestTemplate restTemplate;
 
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
        HttpHost host = new HttpHost("localhost", 8082, "http");
        restTemplate = new RestTemplate(
          new HttpComponentsClientHttpRequestFactoryBasicAuth(host));
    }
    
}