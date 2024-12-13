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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.service.WorkflowResultService;
import lombok.extern.slf4j.Slf4j;


/**
 * Event handler for Item related requests.
 * @author yingfeng
 */
@RepositoryEventHandler(Item.class)
@Component
@Validated
@Slf4j
public class ItemHandler {    

    @Autowired
    private WorkflowResultService workflowResultService;

    @Autowired
	private MgmEvaluationService mgmEvaluationService;

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private PermissionService permissionService;


    @HandleBeforeCreate
    public void handleBeforeCreate(@Valid Item item) {
		// check permission
		Long acUnitId = item.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Create, TargetType.Item, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot create items in unit " + acUnitId);
		}
		
    	// This method is needed to invoke validation before DB persistence.   	        
        log.info("Creating item " + item.getName() + " ...");
    }

    @HandleAfterCreate
    public void handleAfterCreate(Item item) {
    	log.info("Successfully created item " + item.getId());
    }
    
    @HandleBeforeSave
    public void handleBeforeUpdate(@Valid Item item) {
		// check permission
		Long acUnitId = item.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Update, TargetType.Item, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot update items in unit " + acUnitId);
		}
		
		log.info("Updating item " + item.getName() + " ...");
    	 
        // Below file system operations should be done before the data entity is updated, 
    	// as we need the values stored in the old entity

    	/* TODO
    	 * fileStorageService.moveEntityDir might not be called, see its TODO comment 
    	 */
    	// move media subdir (if exists) of the item in case its parent is changed 
    	fileStorageService.moveEntityDir(item);    	
    }
 
    @HandleAfterSave
    public void handleAfterUpdate(Item item) {
    	log.info("Successfully updated item " + item.getId());
    }
        
    @HandleBeforeDelete
    @Transactional
    public void handleBeforeDelete(Item item) {
		// check permission
    	// Note: It's assumed that a role with permission to delete a parent entity can also delete all its descendants' data.
		Long acUnitId = item.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Delete, TargetType.Item, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot delete items in unit " + acUnitId);
		}
		
		// check if item is deletable
		if (!item.getDeletable()) {
			throw new RuntimeException("Item " + item.getId() + " is not deletable!");
		}
				
        log.info("Deleting item " + item.getId() + " ...");

        // Below file system and Galaxy operations should be done before the data entity is deleted, so that 
        // in case of exception, the process can be repeated instead of manual operations.
         
        // delete evaluation output files associated with groundtruth supplements under the item as applicable
        mgmEvaluationService.deleteEvaluationOutputs(item);
                
        // delete workflow results associated with the item if any
        workflowResultService.deleteWorkflowResults(item);

        // delete media subdir (if exists) of the item
        // which also takes care of deleting all descendants' subdirs and media files 
        fileStorageService.deleteEntityDir(item);    	
    }
    
    @HandleAfterDelete
    public void handleAfterDelete(Item item) {
    	log.info("Successfully deleted item " + item.getId());           
    }
            
}


