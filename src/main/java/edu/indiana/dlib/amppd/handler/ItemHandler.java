package edu.indiana.dlib.amppd.handler;

import javax.validation.Valid;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Item;
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
    
    @HandleBeforeCreate
    public void handleBeforeCreate(@Valid Item item){
    	// The purpose of this method is to invoke validation before DB persistence.   	        
    }

    @HandleBeforeSave
    public void handleBeforeUpdate(@Valid Item item){
    	// The purpose of this method is to invoke validation before DB persistence.   	                
    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Item item){
        log.info("Handling process before deleting item " + item.getId() + " ...");

    }

//    @HandleAfterDelete
//    public void handleAfterDelete(Item item){
//        log.info("Handling process after deleting item " + item.getId() + " ...");
//    }
    
}


