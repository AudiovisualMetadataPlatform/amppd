package edu.indiana.dlib.amppd.handler;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;


/**
 * Event handler for Item related requests.
 * @item yingfeng
 */
@RepositoryEventHandler(Item.class)
@Component
@Validated
@Slf4j
public class ItemHandler {    

	@Autowired
	private FileStorageService fileStorageService;
    
    @HandleBeforeCreate
    public void handleBeforeCreate(@Valid Item item){
    	// This method is needed to invoke validation before DB persistence.   	        
    }

    @HandleBeforeSave
    public void handleBeforeUpdate(@Valid Item item){
    	// This method is needed to invoke validation before DB persistence.   	                
    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Item item){
        log.info("Handling process before deleting item " + item.getId() + " ...");

        /* Note: 
         * Below file system deletions should be done before the data entity is deleted, so that 
         * in case of exception, the process can be repeated instead of manual operations.
         */

        // delete media directory tree of the item
        fileStorageService.delete(fileStorageService.getDirPathname(item));        
    }
    
}


