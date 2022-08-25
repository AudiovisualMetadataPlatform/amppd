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
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.service.FileStorageService;
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
    
	@Autowired
	private FileStorageService fileStorageService;	
	
    @HandleBeforeCreate
//    @Validated({WithReference.class, WithoutReference.class})
    public void handleBeforeCreate(@Valid Primaryfile primaryfile) {
    	// This method is needed to invoke validation before DB persistence.
    	log.info("Creating primaryfile " + primaryfile.getName() + "...");
    }

    @HandleAfterCreate
    public void handleAfterCreate(Primaryfile primaryfile){
		// ingest media file after primaryfile is saved
    	if (primaryfile.getMediaFile() != null) {
    		fileStorageService.uploadAsset(primaryfile, primaryfile.getMediaFile());
    	}
    	else {
//    		throw new RuntimeException("No media file is provided for the primaryfile to be created.");
    		log.warn("No media file is provided for the primaryfile to be created.");
    	}
    	
    	log.info("Successfully created primaryfile " + primaryfile.getId());
    }

    @HandleBeforeSave
//    @Validated({WithReference.class, WithoutReference.class})
    public void handleBeforeUpdate(@Valid Primaryfile primaryfile){
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
    public void handleAfterUpdate(Primaryfile primaryfile){
		// ingest media file after primaryfile is saved
    	if (primaryfile.getMediaFile() != null) {
    		fileStorageService.uploadAsset(primaryfile, primaryfile.getMediaFile());
    	}
    	
    	log.info("Successfully updated primaryfile " + primaryfile.getId());
    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Primaryfile primaryfile){
        log.info("Deleting primaryfile " + primaryfile.getId() + " ...");

        // Below file system operations should be done before the data entity is deleted, so that 
        // in case of exception, the process can be repeated instead of manual operations.
         
        // delete media/info files and subdir (if exists) of the primaryfile 
        fileStorageService.unloadAsset(primaryfile);
        fileStorageService.deleteEntityDir(primaryfile);    	
    }
    
    @HandleAfterDelete
    public void handleAfterDelete(Primaryfile primaryfile){
    	log.info("Successfully deleted primaryfile " + primaryfile.getId());           
    }
    
}
