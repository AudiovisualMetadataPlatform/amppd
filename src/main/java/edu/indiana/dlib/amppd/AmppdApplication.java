package edu.indiana.dlib.amppd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
//@EnableJdbcHttpSession
@EnableJpaRepositories("edu.indiana.dlib.amppd.repository")
@EnableConfigurationProperties({GalaxyPropertyConfig.class, AmppdPropertyConfig.class})
@Slf4j
public class AmppdApplication {
	
    private static ApplicationContext applicationContext;
	
	public static void main(String[] args) {
		SpringApplication.run(AmppdApplication.class, args);
		log.info("AMPPD has started successfully.");
		
//        log.debug("Hello, I'm DEBUG message.");
//        log.info("Hello, I'm INFO message.");
//        log.warn("Hello, I'm WARN message.");
//        log.error("Hello, I'm ERROR message.");
	}
	
    public static void displayAllBeans() {
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        for(String beanName : allBeanNames) {
            System.out.println(beanName);
        }
    }	

}
