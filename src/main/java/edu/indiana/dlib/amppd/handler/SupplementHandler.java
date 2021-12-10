package edu.indiana.dlib.amppd.handler;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;


/**
 * Event handler for Supplement related requests.
 * @Supplement yingfeng
 */
@RepositoryEventHandler(Supplement.class)
@Component
@Validated
@Slf4j
public class SupplementHandler {    

	@Autowired
	private FileStorageService fileStorageService;

    @HandleBeforeCreate
    public void handleBeforeCreate(@Valid Supplement supplement){
    	// The purpose of this method is to invoke validation before DB persistence.   	        
    }

    @HandleBeforeSave
    public void handleBeforeUpdate(@Valid Supplement supplement){
    	// The purpose of this method is to invoke validation before DB persistence.   	                
    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Supplement supplement){
        log.info("Handling process before deleting supplement " + supplement.getId() + " ...");

        /* Note: 
         * Below file system deletions should be done before the data entity is deleted, so that 
         * in case of exception, the process can be repeated instead of manual operations.
         */

        // delete media file of the supplement
        String pathname = fileStorageService.getFilePathname(supplement);
        fileStorageService.delete(pathname);               
    }

//    @HandleAfterDelete
//    public void handleAfterDelete(Supplement Supplement){
//        log.info("Handling process after deleting Supplement " + Supplement.getId() + " ...");
//    }
    
}
