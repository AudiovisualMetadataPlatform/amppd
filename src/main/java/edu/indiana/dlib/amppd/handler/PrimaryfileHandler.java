package edu.indiana.dlib.amppd.handler;

import javax.validation.Valid;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Primaryfile;
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

//    @HandleAfterDelete
//    public void handleAfterDelete(Primaryfile Primaryfile){
//        log.info("Handling process after deleting Primaryfile " + Primaryfile.getId() + " ...");
//    }
    
}
