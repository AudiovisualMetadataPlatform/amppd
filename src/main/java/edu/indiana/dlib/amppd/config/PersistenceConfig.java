package edu.indiana.dlib.amppd.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import edu.indiana.dlib.amppd.service.AmpUserService;

/**
 * Configuration for persistence layer.
 * @author yingfeng
 *
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class PersistenceConfig {

	@Autowired
	private AmpUserService userService;
	
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(userService.getCurrentUsername());
    }
    
}
