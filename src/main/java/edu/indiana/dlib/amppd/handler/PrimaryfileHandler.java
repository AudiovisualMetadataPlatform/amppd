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

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.service.WorkflowResultService;
import lombok.extern.slf4j.Slf4j;


/**
 * Event handler for Primaryfile related requests.
 * @author yingfeng
 */
@RepositoryEventHandler(Primaryfile.class)
@Component
@Validated
@Slf4j
public class PrimaryfileHandler {    
    
    @Autowired
    private WorkflowResultService workflowResultService;

	@Autowired
	private MgmEvaluationService mgmEvaluationService;

	@Autowired
	private FileStorageService fileStorageService;	
	
	@Autowired
	private PermissionService permissionService;
	
	
	// This method is not needed anymore since the corresponding API is disabled.
//    @HandleBeforeCreate
////    @Validated({WithReference.class, WithoutReference.class})
//    public void handleBeforeCreate(@Valid Primaryfile primaryfile) {
//    	// This method is needed to invoke validation before DB persistence.
//    	log.info("Creating primaryfile " + primaryfile.getName() + "...");
//    }

	// This method is not needed anymore since the corresponding API is disabled.
//    @HandleAfterCreate
//    public void handleAfterCreate(Primaryfile primaryfile) {
//		// ingest media file after primaryfile is saved
//    	if (primaryfile.getMediaFile() != null) {
//    		fileStorageService.uploadAsset(primaryfile, primaryfile.getMediaFile());
//    	}
//    	else {
////    		throw new RuntimeException("No media file is provided for the primaryfile to be created.");
//    		log.warn("No media file is provided for the primaryfile to be created.");
//    	}
//    	
//    	log.info("Successfully created primaryfile " + primaryfile.getId());
//    }

    @HandleBeforeSave
//    @Validated({WithReference.class, WithoutReference.class})
    public void handleBeforeUpdate(@Valid Primaryfile primaryfile) {
		// check permission
		Long acUnitId = primaryfile.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Update, TargetType.Primaryfile, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot update primaryfiles in unit " + acUnitId);
		}
		
    	log.info("Updating primaryfile " + primaryfile.getId() + " ...");

        // Below file system operations should be done before the data entity is updated, 
    	// as we need the values stored in the old entity

    	/* TODO
    	 * fileStorageService.moveEntityDir might not be called, see its TODO comment 
    	 */
    	// move media subdir (if exists) and media/info files of the primaryfile in case its parent is changed
    	// no need to save primaryfile here as it will be saved by RepositoryRestResource after this
    	// plus, if primaryfile is saved by moveAsset before moveEntityDir, 
    	// the latter won't work properly as the original parent would have been lost 
    	fileStorageService.moveEntityDir(primaryfile);
        fileStorageService.moveAsset(primaryfile, false);
    }

    @HandleAfterSave
    public void handleAfterUpdate(Primaryfile primaryfile) {
		// ingest media file after primaryfile is saved
    	if (primaryfile.getMediaFile() != null) {
    		fileStorageService.uploadAsset(primaryfile, primaryfile.getMediaFile());
    	}
    	
    	log.info("Successfully updated primaryfile " + primaryfile.getId());
    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Primaryfile primaryfile) {
		// check permission
    	// Note: It's assumed that a role with permission to delete a parent entity can also delete all its descendants' data.
		Long acUnitId = primaryfile.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Delete, TargetType.Primaryfile, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot delete primaryfiles in unit " + acUnitId);
		}
		
		// check if primaryfile is deletable
		if (!primaryfile.getDeletable()) {
			throw new RuntimeException("Primaryfile " + primaryfile.getId() + " is not deletable!");
		}
		
        log.info("Deleting primaryfile " + primaryfile.getId() + " ...");

        // Below file system and Galaxy operations should be done before the data entity is deleted, so that 
        // in case of exception, the process can be repeated instead of manual operations.
         
        // delete evaluation output files associated with groundtruth supplements under the primaryfile as applicable
        mgmEvaluationService.deleteEvaluationOutputs(primaryfile);
        
        // delete workflow results associated with the primaryfile if any
        workflowResultService.deleteWorkflowResults(primaryfile);

        // delete media/info files and subdir (if exists) of the primaryfile 
        // the latter also takes care of deleting all descendants' media files 
        fileStorageService.unloadAsset(primaryfile);
        fileStorageService.deleteEntityDir(primaryfile);    
    }
    
    @HandleAfterDelete
    public void handleAfterDelete(Primaryfile primaryfile) {
    	log.info("Successfully deleted primaryfile " + primaryfile.getId());           
    }
    
}
