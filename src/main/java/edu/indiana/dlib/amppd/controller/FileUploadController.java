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
    	log.info("Uploading media file " + file.getName() + " for primaryfile ID " + id);
    	return (Primaryfile)fileStorageService.uploadAsset(id, file, SupplementType.PFILE);
    }

	/**
	 * Upload the given media file to Amppd file system for the given collectionSupplement.
	 * @param id ID of the given collectionSupplement
	 * @param file the media file content to be uploaded
	 * @return the collectionSupplement with media uploaded
	 */
    @PostMapping("/collections/supplements/{id}/upload")
    public CollectionSupplement uploadCollectionSupplement(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {		
    	log.info("Uploading media file " + file.getName() + " for collectionSupplement ID " + id);
    	return (CollectionSupplement)fileStorageService.uploadAsset(id, file, SupplementType.COLLECTION);
    }
    
	/**
	 * Upload the given media file to Amppd file system for the given itemSupplement.
	 * @param id ID of the given itemSupplement
	 * @param file the media file content to be uploaded
	 * @return the itemSupplement with media uploaded
	 */
    @PostMapping("/items/supplements/{id}/upload")
    public ItemSupplement uploadItemSupplement(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {		
    	log.info("Uploading media file " + file.getName() + " for itemSupplement ID " + id);
    	return (ItemSupplement)fileStorageService.uploadAsset(id, file, SupplementType.ITEM);
    }
    
	/**
	 * Upload the given media file to Amppd file system for the given primaryfileSupplement.
	 * @param id ID of the given primaryfileSupplement
	 * @param file the media file content to be uploaded
	 * @return the primaryfileSupplement with media uploaded
	 */
    @PostMapping("/primaryfiles/supplements/{id}/upload")
    public PrimaryfileSupplement uploadPrimaryfileSupplement(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {		
    	log.info("Uploading media file " + file.getName() + " for primaryfileSupplement ID " + id);
    	return (PrimaryfileSupplement)fileStorageService.uploadAsset(id, file, SupplementType.PRIMARYFILE);
    }

}
