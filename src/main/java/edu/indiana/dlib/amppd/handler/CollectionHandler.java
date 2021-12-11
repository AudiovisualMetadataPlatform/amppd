package edu.indiana.dlib.amppd.handler;


import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.service.DropboxService;
import edu.indiana.dlib.amppd.service.FileStorageService;
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
	private WorkflowResultRepository workflowResultRepository;

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private DropboxService dropboxService;

    @HandleBeforeCreate
    public void handleBeforeCreate(@Valid Collection collection) {
        log.info("Handling process before creating collection " + collection.getName() + " ...");

        // create dropbox subdir for the collection to be created, assume collection name has been validated
        dropboxService.createCollectionSubdir(collection);
    }
    
    @HandleBeforeSave
    public void handleBeforeUpdate(@Valid Collection collection) {
        log.info("Handling process before updating collection " + collection.getId() + " ...");
        
        // rename dropbox subdir for the collection to be updated, assume collection name has been validated
        dropboxService.renameCollectionSubdir(collection);
    }

    @HandleAfterSave
    @Transactional
    public void handleAfterUpdate(Collection collection) {
    	log.info("Handling process after updating collection " + collection.getId() + " ...");

    	// remove all workflow results associated with the collection if it is deactivated
    	if (!collection.getActive()) {
    		try {
    			List<WorkflowResult> deleteResults = workflowResultRepository.deleteByCollectionId(collection.getId());
    			if (deleteResults != null && !deleteResults.isEmpty()) {
    				log.info("Successfully deleted " + deleteResults.size() + " WorkflowResults assoicated with the deactivated collection " + collection.getId());
    			}
    		}
    		catch (Exception e) {
    			log.error("Failed to delete inactive WorkflowResults if any.", e);
    		}
    	}
    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Collection collection) {
        log.info("Handling process before deleting collection " + collection.getId() + " ...");

        /* Note: 
         * Below file system deletions should be done before the data entity is deleted, so that 
         * in case of exception, the process can be repeated instead of manual operations.
         */

        // delete media directory tree of the collection
        fileStorageService.delete(fileStorageService.getDirPathname(collection));

        // delete dropbox subdir for the collection to be deleted
        dropboxService.deleteCollectionSubdir(collection);
    }
    
}
