package edu.indiana.dlib.amppd.handler;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import edu.indiana.dlib.amppd.service.PermissionService;
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

	@Autowired
	private FileStorageService fileStorageService;
	
	@Autowired
	private MgmEvaluationService mgmEvaluationService;

	@Autowired
	private PermissionService permissionService;
	

	// This method is not needed anymore since the corresponding API is disabled.
//    @HandleBeforeCreate
////    @Validated({WithReference.class, WithoutReference.class})
//    public void handleBeforeCreate(@Valid Supplement supplement){
//    	// This method is needed to invoke validation before DB persistence.   	        
//    	log.info("Creating supplement " + supplement.getName() + "...");
//    }

	// This method is not needed anymore since the corresponding API is disabled.
//    @HandleAfterCreate
//    public void handleAfterCreate(Supplement supplement){
//		// ingest media file after supplement is saved
//    	if (supplement.getMediaFile() != null) {
//    		fileStorageService.uploadAsset(supplement, supplement.getMediaFile());
//    	}
//    	else {
////    		throw new RuntimeException("No media file is provided for the collectionSupplement to be created.");
//    		log.warn("No media file is provided for the supplement to be created.");
//    	}
//    	
//    	log.info("Successfully created supplement " + supplement.getId());
//    }
    
    @HandleBeforeSave
//    @Validated({WithReference.class, WithoutReference.class})
    public void handleBeforeUpdate(@Valid Supplement supplement){
		// check permission
		Long acUnitId = supplement.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Update, TargetType.Supplement, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot update supplements in unit " + acUnitId);
		}
		
    	log.info("Updating supplement " + supplement.getId() + "...");

        // Below file system operations should be done before the data entity is updated, 
    	// as we need the values stored in the old entity

    	// move media/info files of the supplement in case its parent is changed 
    	// no need to save primaryfile here as it will be saved by RepositoryRestResource after this
        fileStorageService.moveAsset(supplement, false);    	
    }
    
    @HandleAfterSave
    public void handleAfterUpdate(Supplement supplement){
		// ingest media file after supplement is saved
    	if (supplement.getMediaFile() != null) {
    		fileStorageService.uploadAsset(supplement, supplement.getMediaFile());
    	}
    	
    	log.info("Successfully updated supplement " + supplement.getId());
    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Supplement supplement) {
		// check permission
		Long acUnitId = supplement.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Delete, TargetType.Supplement, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot delete supplements in unit " + acUnitId);
		}
		
        log.info("Deleting supplement " + supplement.getId() + " ...");

        // Below file system operations should be done before the data entity is deleted, so that 
        // in case of exception, the process can be repeated instead of manual operations.
         
        // delete media/info files of the supplement 
        fileStorageService.unloadAsset(supplement); 	        

        // delete associated MGM evaluation test result files associated with the supplement if applicable 
        mgmEvaluationService.deleteEvaluationOutputs(supplement); 	        
    }
    
    @HandleAfterDelete
    public void handleAfterDelete(Supplement supplement){
    	log.info("Successfully deleted supplement " + supplement.getId());           
    }
        
}
