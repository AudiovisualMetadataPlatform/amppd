package edu.indiana.dlib.amppd.handler;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.service.DropboxService;
import lombok.extern.slf4j.Slf4j;


/**
 * Event handler for Unit related requests.
 * @unit yingfeng
 */
@RepositoryEventHandler(Unit.class)
@Component
@Validated
@Slf4j
public class UnitHandler {    

	@Autowired
	private DropboxService dropboxService;
    
    @HandleBeforeCreate
    public void handleBeforeCreate(@Valid Unit unit){        
    	/* Note:
    	 * The purpose of this method is to invoke validation before DB persistence.
    	 * We don't need to create dropbox sub-directory when creating a unit, as that can be handled when a collection is created.
    	 */    	
    }

    @HandleBeforeSave
    public void handleBeforeUpdate(@Valid Unit unit){
        log.info("Before updating unit " + unit.getId() + " ...");
        
        // rename dropbox subdir for the unit to be updated, assume unit name has been validated
        dropboxService.renameUnitSubdir(unit);
    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Unit unit){
        log.info("Before deleting unit " + unit.getId() + " ...");

        // delete dropbox subdir for the unit to be deleted
        dropboxService.deleteUnitSubdir(unit);
    }

//    @HandleAfterDelete
//    public void handleAfterDelete(Unit unit){
//        log.info("After deleting unit " + unit.getId() + " ...");
//    }
    
}
