package edu.indiana.dlib.amppd.handler;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.validator.WithReference;
import edu.indiana.dlib.amppd.validator.WithoutReference;
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
	
	@Autowired
	private ItemRepository itemRepository;	
	
    @HandleBeforeCreate
    @Validated({WithReference.class, WithoutReference.class})
    public void handleBeforeCreate(@Valid Primaryfile primaryfile){
    	// The purpose of this method is to invoke validation before DB persistence.
    }

    @HandleAfterCreate
    public void handleAfterCreate(Primaryfile primaryfile){
    	log.info("Handling process after creating primaryfile " + primaryfile.getId() + " ...");
    	if (primaryfile.getMediaFile() != null) {
    		fileStorageService.uploadPrimaryfile(primaryfile, primaryfile.getMediaFile());
    	}
    	else {
//    		throw new RuntimeException("No media file is provided for the primaryfile to be created.");
    		log.warn("No media file is provided for the primaryfile to be created.");
    	}
    }

    @HandleBeforeSave
    @Validated({WithReference.class, WithoutReference.class})
    public void handleBeforeUpdate(@Valid Primaryfile primaryfile){
    	// The purpose of this method is to invoke validation before DB persistence.  
    }

    @HandleAfterSave
    public void handleAfterUpdate(Primaryfile primaryfile){
    	log.info("Handling process after updating primaryfile " + primaryfile.getId() + " ...");
    	if (primaryfile.getMediaFile() != null) {
    		fileStorageService.uploadPrimaryfile(primaryfile, primaryfile.getMediaFile());
    	}
    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Primaryfile primaryfile){
        log.info("Handling process before deleting primaryfile " + primaryfile.getId() + " ...");

        /* Note: 
         * Below file system deletions should be done before the data entity is deleted, so that 
         * in case of exception, the process can be repeated instead of manual operations.
         */

        // delete media/info file and directory tree (if exists) of the primaryfile 
        fileStorageService.unloadPrimaryfile(primaryfile);
        fileStorageService.delete(fileStorageService.getDirPathname(primaryfile));               
    }
    
}
