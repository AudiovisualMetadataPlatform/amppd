package edu.indiana.dlib.amppd.handler;

import javax.validation.Valid;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Supplement;
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

    }

//    @HandleAfterDelete
//    public void handleAfterDelete(Supplement Supplement){
//        log.info("Handling process after deleting Supplement " + Supplement.getId() + " ...");
//    }
    
}
