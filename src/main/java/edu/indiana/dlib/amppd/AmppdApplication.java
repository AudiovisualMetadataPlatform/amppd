package edu.indiana.dlib.amppd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.config.AmppdUiPropertyConfig;
import edu.indiana.dlib.amppd.config.AvalonPropertyConfig;
import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling
//@EnableJdbcHttpSession
@EnableJpaRepositories("edu.indiana.dlib.amppd.repository")
@EnableConfigurationProperties({AmppdUiPropertyConfig.class, AmppdPropertyConfig.class, GalaxyPropertyConfig.class, AvalonPropertyConfig.class})
@Slf4j
public class AmppdApplication {
		
    private static ApplicationContext applicationContext;
	
	public static void main(String[] args) {
		SpringApplication.run(AmppdApplication.class, args);
		log.info("AMPPD has started successfully.");
	}
	
    public static void displayAllBeans() {
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        for(String beanName : allBeanNames) {
            System.out.println(beanName);
        }
    }	

}
