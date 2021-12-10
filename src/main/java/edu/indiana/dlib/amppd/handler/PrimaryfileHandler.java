package edu.indiana.dlib.amppd.handler;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;


/**
 * Event handler for Primaryfile related requests.
 * @Primaryfile yingfeng
 */
@RepositoryEventHandler(Primaryfile.class)
@Component
@Validated
@Slf4j
public class PrimaryfileHandler {    
    
	@Autowired
	private FileStorageService fileStorageService;
	
    @HandleBeforeCreate
    public void handleBeforeCreate(@Valid Primaryfile primaryfile){
    	// The purpose of this method is to invoke validation before DB persistence.   	        
    }

    @HandleBeforeSave
    public void handleBeforeUpdate(@Valid Primaryfile primaryfile){
    	// The purpose of this method is to invoke validation before DB persistence.   	                
    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Primaryfile primaryfile){
        log.info("Handling process before deleting primaryfile " + primaryfile.getId() + " ...");

    }

    @HandleAfterCreate
    public void handleAfterCreate(@Valid Primaryfile primaryfile){
    	log.info("Handling process after creating primaryfile " + primaryfile.getId() + " ...");
    	if (primaryfile.getMediaFile() != null) {
    		fileStorageService.uploadPrimaryfile(primaryfile.getId(), primaryfile.getMediaFile());
    	}
    	else {
//    		throw new RuntimeException("No media file is provided for the primaryfile to be created.");
    		log.warn("No media file is provided for the primaryfile to be created.");
    	}
    }

    @HandleAfterSave
    public void handleAfterUpdate(@Valid Primaryfile primaryfile){
    	log.info("Handling process after updating primaryfile " + primaryfile.getId() + " ...");
    	if (primaryfile.getMediaFile() != null) {
    		fileStorageService.uploadPrimaryfile(primaryfile.getId(), primaryfile.getMediaFile());
    	}
    }
    
    @HandleAfterDelete
    public void handleAfterDelete(Primaryfile primaryfile){
        log.info("Handling process after deleting primaryfile " + primaryfile.getId() + " ...");

    }

    
//    @HandleAfterDelete
//    public void handleAfterDelete(Primaryfile Primaryfile){
//        log.info("Handling process after deleting Primaryfile " + Primaryfile.getId() + " ...");
//    }
    
}
