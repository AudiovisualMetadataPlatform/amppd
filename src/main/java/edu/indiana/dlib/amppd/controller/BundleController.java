package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.service.BundleService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for REST operations on Bundle.
 * @author yingfeng
 *
 */
@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class BundleController {

	@Autowired
    private BundleService bundleService;
	
	/**
	 * Find the bundle with the given name created by the current user.
	 * @param name name of the bundle
	 * @return the matching bundle if found, or null otherwise
	 */
	@GetMapping("/bundles/search/findByNameCreatedByCurrentUser")
	public Bundle findByNameCreatedByCurrentUser(@RequestParam("name") String name) {
		log.info("Finding bundle with name " + name + " for the current user ...");
		return bundleService.findByNameCreatedByCurrentUser(name);
	}
	
	/**
	 * Add the given primaryfile to the given bundle.
	 * @param bundleId ID of the given bundle
	 * @param primaryfileId ID of the given primaryfile
	 * @return the updated bundle
	 */
	@PostMapping("/bundles/{bundleId}/addPrimaryfile")
	public Bundle addPrimaryfile(@PathVariable("bundleId") Long bundleId, @RequestParam("primaryfileId") Long primaryfileId) {		
		log.info("Adding primaryfile " + primaryfileId + " to bundle " + bundleId);
		return bundleService.addPrimaryfile(bundleId, primaryfileId);
    }

	/**
	 * Delete the given primaryfile from the given bundle.
	 * @param bundleId ID of the given bundle
	 * @param primaryfileId ID of the given primaryfile
	 * @return the updated bundle
	 */
	@PostMapping("/bundles/{bundleId}/deletePrimaryfile")
    public Bundle deletePrimaryfile(@PathVariable("bundleId") Long bundleId, @RequestParam("primaryfileId") Long primaryfileId) {		
		log.info("Deleteing primaryfile " + primaryfileId + " from bundle " + bundleId);
		return bundleService.deletePrimaryfile(bundleId, primaryfileId);
    }
	
	/**
	 * Add the given primaryfiles to the given bundle.
	 * @param bundleId ID of the given bundle
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @return the updated bundle
	 */
	@PostMapping("/bundles/{bundleId}/addPrimaryfiles")
	public Bundle addPrimaryfiles(@PathVariable("bundleId") Long bundleId, @RequestParam("primaryfileIds") Long[] primaryfileIds) {		
		log.info("Adding primaryfiles " + primaryfileIds + " to bundle " + bundleId);
		return bundleService.addPrimaryfiles(bundleId, primaryfileIds);
    }

	/**
	 * Delete the given primaryfiles from the given bundle.
	 * @param bundleId ID of the given bundle
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @return the updated bundle
	 */
	@PostMapping("/bundles/{bundleId}/deletePrimaryfiles")
	public Bundle deletePrimaryfiles(@PathVariable("bundleId") Long bundleId, @RequestParam("primaryfileIds") Long[] primaryfileIds) {		
		log.info("Deleteing primaryfiles " + primaryfileIds + " from bundle " + bundleId);
		return bundleService.deletePrimaryfiles(bundleId, primaryfileIds);
    }

	 /**
	  * Update the set of primaryfiles with the given set of primaryfiles for the given bundle.
	  * @param bundleId ID of the given bundle
	  * @param primaryfileIds IDs of the given primaryfiles
	  * @return the updated bundle
	  */
	@PostMapping("/bundles/{bundleId}/updatePrimaryfiles")
	public Bundle updatePrimaryfiles(@PathVariable("bundleId") Long bundleId, @RequestParam("primaryfileIds") Long[] primaryfileIds) {		
		log.info("Updating primaryfiles " + primaryfileIds + " for bundle " + bundleId);
		return bundleService.updatePrimaryfiles(bundleId, primaryfileIds);
    }

}
