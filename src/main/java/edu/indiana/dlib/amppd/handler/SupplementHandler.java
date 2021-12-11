package edu.indiana.dlib.amppd.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.validator.OnRefObj;
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

    @HandleBeforeCreate
    public void handleBeforeCreate(@Validated(OnRefObj.class) Supplement supplement){
    	// The purpose of this method is to invoke validation before DB persistence.   	        
    }

    @HandleAfterCreate
    public void handleAfterCreate(CollectionSupplement collectionSupplement){
    	log.info("Handling process after creating collectionSupplement " + collectionSupplement.getId() + " ...");
    	if (collectionSupplement.getMediaFile() != null) {
    		fileStorageService.uploadCollectionSupplement(collectionSupplement, collectionSupplement.getMediaFile());
    	}
    	else {
//    		throw new RuntimeException("No media file is provided for the collectionSupplement to be created.");
    		log.warn("No media file is provided for the collectionSupplement to be created.");
    	}
    }
    
    @HandleAfterCreate
    public void handleAfterCreate(ItemSupplement itemSupplement){
    	log.info("Handling process after creating itemSupplement " + itemSupplement.getId() + " ...");
    	if (itemSupplement.getMediaFile() != null) {
    		fileStorageService.uploadItemSupplement(itemSupplement, itemSupplement.getMediaFile());
    	}
    	else {
//    		throw new RuntimeException("No media file is provided for the itemSupplement to be created.");
    		log.warn("No media file is provided for the itemSupplement to be created.");
    	}
    }
    
    @HandleAfterCreate
    public void handleAfterCreate(PrimaryfileSupplement primaryfileSupplement){
    	log.info("Handling process after creating primaryfileSupplement " + primaryfileSupplement.getId() + " ...");
    	if (primaryfileSupplement.getMediaFile() != null) {
    		fileStorageService.uploadPrimaryfileSupplement(primaryfileSupplement, primaryfileSupplement.getMediaFile());
    	}
    	else {
//    		throw new RuntimeException("No media file is provided for the primaryfileSupplement to be created.");
    		log.warn("No media file is provided for the primaryfileSupplement to be created.");
    	}
    }
    
    @HandleBeforeSave
    public void handleBeforeUpdate(@Validated(OnRefObj.class) Supplement supplement){
    	// The purpose of this method is to invoke validation before DB persistence.   	                
    }
    
    @HandleAfterSave
    public void handleAfterUpdate(CollectionSupplement collectionSupplement){
    	log.info("Handling process after udpating collectionSupplement " + collectionSupplement.getId() + " ...");
    	if (collectionSupplement.getMediaFile() != null) {
    		fileStorageService.uploadCollectionSupplement(collectionSupplement, collectionSupplement.getMediaFile());
    	}
    }
        
    @HandleAfterSave
    public void handleAfterUpdate(ItemSupplement itemSupplement){
    	log.info("Handling process after udpating itemSupplement " + itemSupplement.getId() + " ...");
    	if (itemSupplement.getMediaFile() != null) {
    		fileStorageService.uploadItemSupplement(itemSupplement, itemSupplement.getMediaFile());
    	}
    }
    
    @HandleAfterSave
    public void handleAfterUpdate(PrimaryfileSupplement primaryfileSupplement){
    	log.info("Handling process after udpating primaryfileSupplement " + primaryfileSupplement.getId() + " ...");
    	if (primaryfileSupplement.getMediaFile() != null) {
    		fileStorageService.uploadPrimaryfileSupplement(primaryfileSupplement, primaryfileSupplement.getMediaFile());
    	}
    }
    
    @HandleBeforeDelete
    public void handleBeforeDelete(Supplement supplement){
        log.info("Handling process before deleting supplement " + supplement.getId() + " ...");

        /* Note: 
         * Below file system deletions should be done before the data entity is deleted, so that 
         * in case of exception, the process can be repeated instead of manual operations.
         */

        // delete media/info file of the supplement
        String pathname = fileStorageService.unloadSupplement(supplement);           
    }
    
}
