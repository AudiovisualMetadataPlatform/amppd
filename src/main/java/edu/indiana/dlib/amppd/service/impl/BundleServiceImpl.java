package edu.indiana.dlib.amppd.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.BundleService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BundleServiceImpl implements BundleService {

	@Autowired
	private BundleRepository bundleRepository;

	@Autowired
	private PrimaryfileRepository primaryfileRepository;

	@Autowired
	private AmpUserService ampUserService;

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.findAllNamed()
	 */
	public List<Bundle> findAllNamed() {
		List<Bundle> bundles = bundleRepository.findAllWithNonEmptyNameNonEmptyPrimaryfiles();
		log.info("Successfully found " + bundles.size() + " named bundles.");
		return bundles;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.findNamedByCurrentUser(String)
	 */
	public Bundle findNamedByCurrentUser(String name) {
		String username = ampUserService.getCurrentUsername();
		List<Bundle> bundles = bundleRepository.findByNameAndCreatedBy(name, ampUserService.getCurrentUsername());

		// bundle name shall be unique per user if all bundles are created via AMPPD UI;
		// however just in case there're more than one found, we will return the first
		if (bundles == null || bundles.isEmpty()) {
			log.info("No bundle found with name " + name + " for the current user " + username);
			return null;
		}

		if (bundles.size() > 1) {
			log.warn("There are " + bundles.size() + " bundles found with name " + name + " for the current user " + username);
		}

		Bundle bundle = bundles.get(0);
		log.info("Successfully found bundle " + bundle.getId() + " with name " + name + " for the current user " + username);
		return bundle;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.addPrimaryfile(Bundle, Long)
	 */
	@Override
	public Bundle addPrimaryfile(Bundle bundle, Long primaryfileId) {
		if (bundle == null) {
			log.error("The given bundle is null!");
			return null;
		}		
		Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("primaryfile <" + primaryfileId + "> does not exist!"));    

		// don't add the primaryfile if it already exists in the bundle
		if (bundle.getPrimaryfiles().contains(primaryfile)) {
			// TODO the warning message shall be displayed back to the screen
			String msg = "Primaryfile <" + primaryfileId + "> has already been added to bundle<" + bundle.getId() + ">!";
			log.warn(msg);	
			return bundle;
		}

		//		primaryfile.getBundles().add(bundle);	
		bundle.getPrimaryfiles().add(primaryfile);	// need to add on bundle side since bundle owns the M:M relationship
		//		primaryfileRepository.save(primaryfile);
		bundle = bundleRepository.save(bundle);	

		String msg = "Successfully added primaryfile <" + primaryfileId + "> to bundle<" + bundle.getId() + ">.";
		log.info(msg);
		return bundle;
	}	

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.deletePrimaryfile(Bundle, Long)
	 */
	@Override
	public Bundle deletePrimaryfile(Bundle bundle, Long primaryfileId) {		
		if (bundle == null) {
			log.error("The given bundle is null!");
			return null;
		}		
		Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("primaryfile <" + primaryfileId + "> does not exist!"));    

		// don't delete anything if the primaryfile doesn't exist in the bundle
		if (!bundle.getPrimaryfiles().contains(primaryfile)) {
			// TODO the warning message shall be displayed back to the screen
			String msg = "Primaryfile <" + primaryfileId + "> doesn't exist in bundle<" + bundle.getId() + ">!";
			log.warn(msg);	
			return bundle;
		}

		//		primaryfile.getBundles().remove(bundle);	
		bundle.getPrimaryfiles().remove(primaryfile); // need to remove from bundle side since bundle owns the M:M relationship
		//		primaryfileRepository.save(primaryfile);
		bundle = bundleRepository.save(bundle);		

		String msg = "Ssuccessfully deleted primaryfile <" + primaryfileId + "> from bundle<" + bundle.getId() + ">.";
		log.info(msg);
		return bundle;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.addPrimaryfile(Long, Long)
	 */
	@Override
	public Bundle addPrimaryfile(Long bundleId, Long primaryfileId) {
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("bundle <" + bundleId + "> does not exist!"));    
		return addPrimaryfile(bundle, primaryfileId);
	}	

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.deletePrimaryfile(Long, Long)
	 */
	@Override
	public Bundle deletePrimaryfile(Long bundleId, Long primaryfileId) {		
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("bundle <" + bundleId + "> does not exist!"));    
		return deletePrimaryfile(bundle, primaryfileId);
	}	

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.addPrimaryfiles(Long, Long[])
	 */
	@Override
	public Bundle addPrimaryfiles(Long bundleId, Long[] primaryfileIds) {
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("bundle <" + bundleId + "> does not exist!"));    		
		if (primaryfileIds == null) {
			log.warn("The given primaryfileIds is empty." );
			return bundle;
		}
		for (Long primaryfileId : primaryfileIds) {
			bundle = addPrimaryfile(bundle, primaryfileId);			
		}		
		return bundle;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.deletePrimaryfiles(Long, Long[])
	 */
	@Override
	public Bundle deletePrimaryfiles(Long bundleId, Long[] primaryfileIds) {
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("bundle <" + bundleId + "> does not exist!"));    		
		if (primaryfileIds == null) {
			log.warn("The given primaryfileIds is empty." );
			return bundle;
		}
		for (Long primaryfileId : primaryfileIds) {
			bundle = deletePrimaryfile(bundle, primaryfileId);			
		}		
		return bundle;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.updatePrimaryfiles(Long, String, Long[])
	 */
	@Override
	public Bundle updateBundle(Long bundleId, String description, Long[] primaryfileIds) {
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("bundle <" + bundleId + "> does not exist!"));    		
		if (primaryfileIds == null) {
			throw new RuntimeException("The given primaryfileIds is empty.");
		}

		// remove redundant primaryfile IDs
		Set<Long> pidset = new HashSet<Long>(Arrays.asList(primaryfileIds));
		Long[] pids = pidset.toArray(primaryfileIds);
		Set<Primaryfile> primaryfiles = new HashSet<Primaryfile>();

		// retrieve primaryfiles to add
		for (Long primaryfileId : pids) {					
			Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("primaryfile <" + primaryfileId + "> does not exist!"));    
			primaryfiles.add(primaryfile);	
		}

		// we only need to update bundle's reference to primaryfiles but not vice versa,
		// as the M:M relationship is owned and taken care of by bundle
		bundle.setDescription(description);
		bundle.setPrimaryfiles(primaryfiles);
		bundle = bundleRepository.save(bundle);				
		log.info("Successfully updated bundle " + bundle.getId() + " with description " + description + " and " + primaryfiles.size() + " prifmaryfiles.");			
		return bundle;		
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.createBundle(String, String, Long[])
	 */	
	@Override
	public Bundle createBundle(String name, String description, Long[] primaryfileIds) {
		if (name == null) {
			throw new RuntimeException("The given bundle name is null.");
		}		

		if (primaryfileIds == null) {
			throw new RuntimeException("The given prifmaryfileIds is null!");
		}		

		Bundle bundle = new Bundle();
		bundle.setName(name);
		bundle.setDescription(description);

		// remove redundant primaryfile IDs
		Set<Long> pidset = new HashSet<Long>(Arrays.asList(primaryfileIds));
		Long[] pids = pidset.toArray(primaryfileIds);
		Set<Primaryfile> primaryfiles = new HashSet<Primaryfile>();

		for (Long primaryfileId : pids) {		
			// skip null primaryfileId, which could result from redundant IDs passed from request parameter being changed to null
			if (primaryfileId == null) continue; 
			
			Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("primaryfile <" + primaryfileId + "> does not exist!"));    
			primaryfiles.add(primaryfile);	
		}

		bundle.setPrimaryfiles(primaryfiles);
		bundle = bundleRepository.save(bundle);

		log.info("Successfully created new bundle " + bundle.getId() + " with name " + name + " and " + primaryfiles.size() + " prifmaryfiles.");			
		return bundle;
	}

}
