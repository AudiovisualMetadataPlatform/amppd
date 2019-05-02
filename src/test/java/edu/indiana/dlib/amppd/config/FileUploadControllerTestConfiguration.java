package edu.indiana.dlib.amppd.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;

@Profile("FileUploadControllerTest")
@Configuration
public class FileUploadControllerTestConfiguration {

	@Bean
	@Primary
	public PrimaryfileRepository primaryfileRepository() {
		return Mockito.mock(PrimaryfileRepository.class);
	}
	
	@Bean
	@Primary
	public CollectionSupplementRepository collectionSupplementRepository() {
		return Mockito.mock(CollectionSupplementRepository.class);
	}
	
	@Bean
	@Primary
	public ItemSupplementRepository itemSupplementRepository() {
		return Mockito.mock(ItemSupplementRepository.class);
	}
	
	@Bean
	@Primary
	public PrimaryfileSupplementRepository primaryfileSupplementRepository() {
		return Mockito.mock(PrimaryfileSupplementRepository.class);
	}
	

}
