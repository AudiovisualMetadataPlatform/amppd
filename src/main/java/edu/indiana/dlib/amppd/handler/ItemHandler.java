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
    public void handleBeforeCreate(@Valid Item item) {
    	// This method is needed to invoke validation before DB persistence.   	        
        log.info("Creating item " + item.getName() + " ...");
    }

    @HandleAfterCreate
    public void handleAfterCreate(Item item) {
    	log.info("Successfully created item " + item.getId());
    }
    
    @HandleBeforeSave
    public void handleBeforeUpdate(@Valid Item item) {
        log.info("Updating item " + item.getName() + " ...");
    	 
        // Below file system operations should be done before the data entity is updated, 
    	// as we need the values stored in the old entity

    	// move media subdir (if exists) of the item in case its parent is changed 
    	fileStorageService.moveEntityDir(item);
    }
 
    @HandleAfterSave
    public void handleAfterUpdate(Item item) {
    	log.info("Successfully updated item " + item.getId());
    }
        
    @HandleBeforeDelete
    public void handleBeforeDelete(Item item) {
        log.info("Deleting item " + item.getId() + " ...");

        // Below file system operations should be done before the data entity is deleted, so that 
        // in case of exception, the process can be repeated instead of manual operations.
         
        // delete media subdir (if exists) of the item
        fileStorageService.deleteEntityDir(item);    	
    }
    
    @HandleAfterDelete
    public void handleAfterDelete(Item item) {
    	log.info("Successfully deleted item " + item.getId());           
    }
            
}


