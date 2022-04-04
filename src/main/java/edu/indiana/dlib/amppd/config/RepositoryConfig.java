package edu.indiana.dlib.amppd.config;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class RepositoryConfig implements RepositoryRestConfigurer {
    @PersistenceContext
    private EntityManager entityManager;
	
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
    	config.exposeIdsFor(
                entityManager.getMetamodel().getEntities().stream()
                .map(EntityType::getJavaType)
                .toArray(Class[]::new));
    }
}