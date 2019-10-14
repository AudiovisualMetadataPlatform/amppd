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
import edu.indiana.dlib.amppd.service.FileStorageService;
import lombok.extern.java.Log;

// TODO: when we add controllers for data entities, we might want to move the actions into controllers for the associated entities.

/**
 * Controller to handle file upload for primaryfiles and supplements.
 * @author yingfeng
 *
 */
@RestController
@Log
public class FileUploadController {
	
	@Autowired
    private FileStorageService fileStorageService;

	@PostMapping("/primaryfiles/{id}/file")
	public Primaryfile uploadPrimaryfile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {		
    	log.info("Uploading media file " + file.getName() + " for primaryfile ID " + id);
    	return fileStorageService.uploadPrimaryfile(id, file);
    }

    @PostMapping("/collections/supplements/{id}/file")
    public CollectionSupplement uploadCollectionSupplement(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {		
    	log.info("Uploading media file " + file.getName() + " for collectionSupplement ID " + id);
    	return fileStorageService.uploadCollectionSupplement(id, file);
    }
    
    @PostMapping("/items/supplements/{id}/file")
    public ItemSupplement uploadItemSupplement(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {		
    	log.info("Uploading media file " + file.getName() + " for itemSupplement ID " + id);
    	return fileStorageService.uploadItemSupplement(id, file);
    }
    
    @PostMapping("/primaryfiles/supplements/{id}/file")
    public PrimaryfileSupplement uploadPrimaryfileSupplement(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {		
    	log.info("Uploading media file " + file.getName() + " for primaryfileSupplement ID " + id);
    	return fileStorageService.uploadPrimaryfileSupplement(id, file);
    }

}
