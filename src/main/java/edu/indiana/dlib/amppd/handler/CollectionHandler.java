package edu.indiana.dlib.amppd.handler;


import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.service.DropboxService;
import lombok.extern.slf4j.Slf4j;


/**
 * Event handler for Collection related requests.
 * @collection yingfeng
 */
@RepositoryEventHandler(Collection.class)
@Component
@Validated
@Slf4j
public class CollectionHandler {    

	@Autowired
	private DropboxService dropboxService;

    @HandleBeforeCreate
    public void handleBeforeCreate(@Valid Collection collection) {
        log.info("Handling process before creating collection " + collection.getName() + " ...");

        // create dropbox subdir for the collection to be created, assume collection name has been validated
        dropboxService.createCollectionSubdir(collection);
    }

//    @HandleAfterCreate
//    public void handleAfterCreate(Collection collection){
//        log.info("Handling process after creating collection " + collection.getName() + " ...");
//    }
    
    @HandleBeforeSave
    public void handleBeforeUpdate(@Valid Collection collection) {
        log.info("Handling process before updating collection " + collection.getId() + " ...");
        
        // rename dropbox subdir for the collection to be updated, assume collection name has been validated
        dropboxService.renameCollectionSubdir(collection);
    }

//    @HandleAfterSave
//    public void handleAfterUpdate(Collection collection){
//        log.info("Handling process after updating collection " + collection.getId() + " ...");
//    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Collection collection) {
        log.info("Handling process before deleting collection " + collection.getId() + " ...");

        // delete dropbox subdir for the collection to be deleted
        dropboxService.deleteCollectionSubdir(collection);
    }

//    @HandleAfterDelete
//    public void handleAfterDelete(Collection collection){
//        log.info("Handling process after deleting collection " + collection.getId() + " ...");
//    }
    
}
