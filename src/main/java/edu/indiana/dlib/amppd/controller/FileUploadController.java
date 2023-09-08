package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import edu.indiana.dlib.amppd.model.UnitSupplement;
import edu.indiana.dlib.amppd.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;

// TODO: when we add controllers for data entities, we might want to move the actions into controllers for the associated entities.

/**
 * Controller to handle file upload for primaryfiles and supplements.
 * @author yingfeng
 */
//@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class FileUploadController {
	
	@Autowired
    private FileStorageService fileStorageService;

	/**
	 * Upload the given media file to Amppd file system for the given primaryfile.
	 * @param id ID of the given primaryfile
	 * @param file the media file content to be uploaded
	 * @return the primaryfile with media uploaded
	 */
	@PostMapping("/primaryfiles/{id}/upload")
	public Primaryfile uploadPrimaryfile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {		
		// permission is checked inside service layer to minimize duplicate code
    	log.info("Uploading media file " + file.getName() + " for primaryfile ID " + id);
    	return (Primaryfile)fileStorageService.uploadAsset(id, file, SupplementType.PFILE);
    }

	/**
	 * Upload the given media file to Amppd file system for the given unitSupplement.
	 * @param id ID of the given unitSupplement
	 * @param file the media file content to be uploaded
	 * @return the unitSupplement with media uploaded
	 */
    @PostMapping("/unitSupplements/{id}/upload")
    public UnitSupplement uploadUnitSupplement(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {		
		// permission is checked inside service layer to minimize duplicate code
    	log.info("Uploading media file " + file.getName() + " for unitSupplement ID " + id);
    	return (UnitSupplement)fileStorageService.uploadAsset(id, file, SupplementType.UNIT);
    }
    
	/**
	 * Upload the given media file to Amppd file system for the given collectionSupplement.
	 * @param id ID of the given collectionSupplement
	 * @param file the media file content to be uploaded
	 * @return the collectionSupplement with media uploaded
	 */
    @PostMapping("/collectionSupplements/{id}/upload")
    public CollectionSupplement uploadCollectionSupplement(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {		
		// permission is checked inside service layer to minimize duplicate code
    	log.info("Uploading media file " + file.getName() + " for collectionSupplement ID " + id);
    	return (CollectionSupplement)fileStorageService.uploadAsset(id, file, SupplementType.COLLECTION);
    }
    
	/**
	 * Upload the given media file to Amppd file system for the given itemSupplement.
	 * @param id ID of the given itemSupplement
	 * @param file the media file content to be uploaded
	 * @return the itemSupplement with media uploaded
	 */
    @PostMapping("/itemSupplements/{id}/upload")
    public ItemSupplement uploadItemSupplement(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {		
		// permission is checked inside service layer to minimize duplicate code
    	log.info("Uploading media file " + file.getName() + " for itemSupplement ID " + id);
    	return (ItemSupplement)fileStorageService.uploadAsset(id, file, SupplementType.ITEM);
    }
    
	/**
	 * Upload the given media file to Amppd file system for the given primaryfileSupplement.
	 * @param id ID of the given primaryfileSupplement
	 * @param file the media file content to be uploaded
	 * @return the primaryfileSupplement with media uploaded
	 */
    @PostMapping("/primaryfileSupplements/{id}/upload")
    public PrimaryfileSupplement uploadPrimaryfileSupplement(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {		
		// permission is checked inside service layer to minimize duplicate code
    	log.info("Uploading media file " + file.getName() + " for primaryfileSupplement ID " + id);
    	return (PrimaryfileSupplement)fileStorageService.uploadAsset(id, file, SupplementType.PRIMARYFILE);
    }

}
