package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import lombok.extern.java.Log;

/**
 * Controller for REST operations on Bundle.
 * @author yingfeng
 *
 */
@RestController
@Log
public class BundleController {

	@Autowired
    private BundleRepository bundleRepository;
	
	@Autowired
    private PrimaryfileRepository primaryfileRepository;

	@PostMapping("/bundles/{bundleId}/add/primaryfiles/{primaryfileId}")
	public Bundle addPrimaryfileToBundle(@PathVariable("bundleId") Long bundleId, @PathVariable("primaryfileId") Long primaryfileId) {		
		Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("primaryfile <" + primaryfileId + "> does not exist!"));    
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("bundle <" + bundleId + "> does not exist!"));    

		// don't add the primaryfile if it already exists in the bundle
		if (bundle.getPrimaryfiles().contains(primaryfile)) {
			// TODO the warning message shall be displayed back to the screen
			String msg = "Primaryfile <" + primaryfileId + "> has already been added to bundle<" + bundleId + ">!";
			log.warning(msg);	
			return bundle;
		}

		primaryfile.getBundles().add(bundle);	// need to add from primaryfile side since Primaryfile owns the M;M relationship
		bundle.getPrimaryfiles().add(primaryfile);	// TODO do we need this?
		primaryfileRepository.save(primaryfile);
		bundleRepository.save(bundle);	// TODO do we need this?
		
		String msg = "You successfully added primaryfile <" + primaryfileId + "> to bundle<" + bundleId + ">!";
		log.info(msg);
		return bundle;
    }

	@PostMapping("/bundles/{bundleId}/delete/primaryfiles/{primaryfileId}")
    public Bundle deletePrimaryfileFromBundle(@PathVariable("bundleId") Long bundleId, @PathVariable("primaryfileId") Long primaryfileId) {		
		Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("primaryfile <" + primaryfileId + "> does not exist!"));    
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("bundle <" + bundleId + "> does not exist!"));    

		// don't delete anything if the primaryfile doesn't exist in the bundle
		if (!bundle.getPrimaryfiles().contains(primaryfile)) {
			// TODO the warning message shall be displayed back to the screen
			String msg = "Primaryfile <" + primaryfileId + "> doesn't exist bundle<" + bundleId + ">!";
			log.warning(msg);	
			return bundle;
		}

		primaryfile.getBundles().remove(bundle);	// need to remove from primaryfile side since Primaryfile owns the M;M relationship
		bundle.getPrimaryfiles().remove(primaryfile);		// TODO do we need this?
		primaryfileRepository.save(primaryfile);
		bundleRepository.save(bundle);		// TODO do we need this?

    	String msg = "You successfully deleted primaryfile <" + primaryfileId + "> from bundle<" + bundleId + ">!";
    	log.info(msg);
        return bundle;
    }
	

	// TODO We shall allow users to add/delete multiple primaryfiles to a bundle at once; question is, how to represent an array of primaryfileIds in the request?
	
	
}
