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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.service.DropboxService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.WorkflowResultService;
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
	private WorkflowResultService workflowResultService;

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private DropboxService dropboxService;

    @HandleBeforeCreate
    public void handleBeforeCreate(@Valid Collection collection) {
        log.info("Creating collection " + collection.getName() + " ...");

        // create dropbox subdir for the collection to be created, assume collection name has been validated
        dropboxService.createSubdir(collection);
    }
    
    @HandleAfterCreate
    public void handleAfterCreate(Collection collection) {
    	log.info("Successfully created collection " + collection.getId());
    }
    
    @HandleBeforeSave
    public void handleBeforeUpdate(@Valid Collection collection) {
    	log.info("Updating collection " + collection.getId() + " ...");
 
        // Below file system operations should be done before the data entity is updated, 
    	// as we need the values stored in the old entity

    	/* TODO
    	 * fileStorageService.moveEntityDir might not be called, see its TODO comment 
    	 */
    	// move media subdir (if exists) of the collection in case its parent is changed 
    	fileStorageService.moveEntityDir(collection);
    	
    	/* TODO
    	 * DropboxServiceImpl.renameSubdir(Collection) might not work well, see its TODO comment.
    	 */
        // rename (if previously exists) or create (if previously doesn't exist) the dropbox subdir of the collection
    	// in case its name is changed, and/or move the subdir if its parent unit changed    	
        dropboxService.renameSubdir(collection); 
    }

    @HandleAfterSave
    @Transactional
    public void handleAfterUpdate(Collection collection) {
    	// remove all workflow results associated with the collection if it is deactivated
    	workflowResultService.deleteInactiveWorkflowResults(collection);    	
    	
    	log.info("Successfully updated collection " + collection.getId());
    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Collection collection) {
        log.info("Deleting collection " + collection.getId() + " ...");

        // Below file system operations should be done before the data entity is deleted, so that 
        // in case of exception, the process can be repeated instead of manual operations.
         
        // delete media subdir (if exists) of the collection
        fileStorageService.deleteEntityDir(collection);

        // delete dropbox subdir (if exists) of the collection
        dropboxService.deleteSubdir(collection);
    }
        
    @HandleAfterDelete
    public void handleAfterDelete(Collection collection) {
    	log.info("Successfully deleted collection " + collection.getId());           
    }
        
}
