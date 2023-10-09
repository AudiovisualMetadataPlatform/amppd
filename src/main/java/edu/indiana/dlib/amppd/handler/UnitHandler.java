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
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.service.DropboxService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.PermissionService;
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
	private FileStorageService fileStorageService;

	@Autowired
	private DropboxService dropboxService;

	@Autowired
	private PermissionService permissionService;

	
	@HandleBeforeCreate
    public void handleBeforeCreate(@Valid Unit unit) {        
		// check permission
		boolean can = permissionService.hasPermission(ActionType.Create, TargetType.Unit, null);
		if (!can) {
			throw new AccessDeniedException("The current user cannot create units.");
		}

		// This method is needed to invoke validation before DB persistence.
		// We don't need to create dropbox sub-directory when creating a unit, as that can be handled when a collection is created.    	
		log.info("Creating unit " + unit.getName() + "...");
    }

    @HandleAfterCreate
    public void handleAfterCreate(Unit unit) {
    	log.info("Successfully created unit " + unit.getId());
    }
    
    @HandleBeforeSave
    public void handleBeforeUpdate(@Valid Unit unit) {
		// check permission
		Long acUnitId = unit.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Update, TargetType.Unit, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot update unit " + acUnitId);
		}
		
    	log.info("Updating unit " + unit.getName() + "...");

        // Below file system operations should be done before the data entity is updated, 
    	// as we need the values stored in the old entity

    	// no need to move media subdir as unit doesn't have parent
    	
    	/* TODO
    	 * DropboxServiceImpl.renameSubdir(Unit) might not work well, see its TODO comment.
    	 */
    	// rename dropbox subdir (if exists) of the unit in case its name is changed
        dropboxService.renameSubdir(unit);        
    }
    
    @HandleAfterSave
    public void handleAfterUpdate(Unit unit) {
    	log.info("Successfully updated unit " + unit.getId());
    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Unit unit) {
		// check permission
		Long acUnitId = unit.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Delete, TargetType.Unit, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot delete unit " + acUnitId);
		}
		
        log.info("Deleting unit " + unit.getId() + " ...");

        // Below file system operations should be done before the data entity is deleted, so that
        // in case of exception, the process can be repeated instead of manual operations.

        // delete media subdir (if exists) of the unit
        fileStorageService.deleteEntityDir(unit);
        
        // delete dropbox subdir (if exists) of the unit 
        dropboxService.deleteSubdir(unit);        
    }
    
    @HandleAfterDelete
    public void handleAfterDelete(Unit unit) {
    	log.info("Successfully deleted unit " + unit.getId());           
    }
    
}
