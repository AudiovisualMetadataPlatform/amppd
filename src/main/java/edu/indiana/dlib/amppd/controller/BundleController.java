package edu.indiana.dlib.amppd.controller;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.service.BundleService;
import edu.indiana.dlib.amppd.service.PermissionService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for REST operations on Bundle.
 * @author yingfeng
 */
//@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class BundleController {

	@Autowired
	private Validator validator;
	
	@Autowired
	private BundleRepository bundleRepository;

	@Autowired
	private BundleService bundleService;
	
	@Autowired
	private PermissionService permissionService;
	

	/**
	 * Find bundles satisfying the combined criteria of name, keyword and creator, if provided.
	 * Note: If name is provided, other fields will be ignored, since name is unique and serves as ID.
	 * @param name name of bundle
	 * @param keyword keyword in the bundle name 
	 * @param creator username of the bundle creator
	 * @return bundles satisfying the criteria
	 */
	// TODO use BundleBrief in response
	@GetMapping("/bundles/search")
	public List<Bundle> findNamedByCurrentUser(
			@RequestParam(required = false) String name, 
			@RequestParam(required = false) String keyword, 
			@RequestParam(required = false) String creator) {	
		log.info("Finding bundle matching: name = " + name + ", keyword = " + keyword + ", creator = " + creator);		
		Set<Long> acUnitIds = permissionService.getAccessibleUnitIds(ActionType.Read, TargetType.Bundle);
		List<Bundle> bundles = bundleService.findByNameKeywordCreator(name, keyword, creator);
		List<Bundle> bundlesF = bundleService.filterBundles(bundles, acUnitIds);
		return bundlesF;
	}
	
	/**
	 * Find all named bundles, i.e. bundles with non-empty name and non-empty primaryfiles.
	 * @return all named bundles
	 */
	// Disable endpoint not in use
//	@GetMapping("/bundles/search/findAllNamed")
	public List<Bundle> findAllNamed() {
		log.info("Finding all named bundles ... " );
		return bundleService.findAllNamed();		
	}
	
//	/**
//	 * Find the bundle with the given name created by the current user.
//	 * @param name name of the bundle
//	 * @return the matching bundle if found, or null otherwise
//	 */
//	@GetMapping("/bundles/search/findNamedByCurrentUser")
//	public Bundle findNamedByCurrentUser(@RequestParam("name") String name) {
//		log.info("Finding bundle with name " + name + " for the current user ...");
//		return bundleService.findNamedByCurrentUser(name);
//	}

	/**
	 * Add the given primaryfile to the given bundle.
	 * @param bundleId ID of the given bundle
	 * @param primaryfileId ID of the given primaryfile
	 * @return the updated bundle
	 */
	// Disable endpoint not in use
//	@PostMapping("/bundles/{bundleId}/addPrimaryfile")
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
	// Disable endpoint not in use
//	@PostMapping("/bundles/{bundleId}/deletePrimaryfile")
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
	// Disable endpoint not in use
//	@PostMapping("/bundles/{bundleId}/addPrimaryfiles")
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
	// Disable endpoint not in use
//	@PostMapping("/bundles/{bundleId}/deletePrimaryfiles")
	public Bundle deletePrimaryfiles(@PathVariable("bundleId") Long bundleId, @RequestParam("primaryfileIds") Long[] primaryfileIds) {		
		log.info("Deleteing primaryfiles " + primaryfileIds + " from bundle " + bundleId);
		return bundleService.deletePrimaryfiles(bundleId, primaryfileIds);
	}

	/**
	 * Update the given bundle with the given name, description and set of primaryfiles.
	 * @param bundleId ID of the bundle to be updated
	 * @param name new name of the bundle
	 * @param description new description of the bundle
	 * @param primaryfileIds IDs of the new set of primaryfiles
	 * @return the updated bundle
	 */
	@PatchMapping("/bundles/{bundleId}")
	public Bundle updateBundle(
			@PathVariable Long bundleId, 
			@RequestParam(required = false) String name, 
			@RequestParam(required = false) String description, 
			@RequestParam Long[] primaryfileIds) {		
		log.info("Updating bundle " + bundleId + " with name: " + name + ", description: " + description + ", primaryfiles: " + primaryfileIds);
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("bundle <" + bundleId + "> does not exist!"));    		

		// get unit IDs within which current user can Update Bundle
		Set<Long> acUnitIds = permissionService.getAccessibleUnitIds(ActionType.Update, TargetType.Bundle);
		// check if original primaryfiles in the bundle are accessible, i.e. intact after filter
		boolean can = bundleService.filterBundle(bundle, acUnitIds);
		// if yes, also check if the updated primaryfiles in the bundle are accessible, i.e. intact after filter
		if (can) {
			bundle = bundleService.updateBundle(bundle, name, description, primaryfileIds);
			can = bundleService.filterBundle(bundle, acUnitIds);
		}
		// only allow update if all primaryfiles in the bundle before & after update are within accessible units
		if (!can) {
			throw new AccessDeniedException("The current user cannot update the bundle across all units of the contained primaryfiles!");
		} 
		
		// validate bundle before persistence
    	Set<ConstraintViolation<Bundle>> violations = validator.validate(bundle);
    	if (!violations.isEmpty()) {
          throw new ConstraintViolationException(violations);
        }

		// persist updated bundle
		bundle = bundleRepository.save(bundle);				
		log.info("Peristed updated bundle " + bundle.getId() + " with name: " + name + ", description: " + description + " and " + bundle.getPrimaryfiles().size() + " prifmaryfiles.");			
		return bundle;
	}

	/**
	 * Create a new bundle with the given name, description and primaryfiles.
	 * @param name name of the new bundle
	 * @param description description of the new bundle
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @return the newly created bundle
	 */
	@PostMapping("/bundles")
	public Bundle createBundle(@RequestParam String name, @RequestParam String description, @RequestParam Long[] primaryfileIds) {
		log.info("Creating new bundle with name: " + name + ", description: " + description + ", primaryfiles: " + primaryfileIds);		
		
		// create a new bundle instance populated with data
		Bundle bundle = bundleService.createBundle(name, description, primaryfileIds);

		// only allow create if all primaryfiles of the bundle are within accessible units, i.e. intact after filter
		Set<Long> acUnitIds = permissionService.getAccessibleUnitIds(ActionType.Create, TargetType.Bundle);
		boolean can = bundleService.filterBundle(bundle, acUnitIds);
		if (!can) {
			throw new AccessDeniedException("The current user cannot create the bundle across all units of the contained primaryfiles!");
		}
		
		// validate bundle after before persistence
    	Set<ConstraintViolation<Bundle>> violations = validator.validate(bundle);
    	if (!violations.isEmpty()) {
          throw new ConstraintViolationException(violations);
        }

		// persist new bundle
		bundle = bundleRepository.save(bundle);
		log.info("Peristed created bundle " + bundle.getId() + " with name: " + name + ", description: " + description  + ", and " + bundle.getPrimaryfiles().size() + " prifmaryfiles.");			
		return bundle;
	}


}
