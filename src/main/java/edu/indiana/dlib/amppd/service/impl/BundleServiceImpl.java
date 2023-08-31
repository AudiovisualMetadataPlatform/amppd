package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.BundleService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of BundleService.
 * @author yingfeng
 */ 
@Service
@Slf4j
public class BundleServiceImpl implements BundleService {

	@Autowired
	private BundleRepository bundleRepository;

	@Autowired
	private PrimaryfileRepository primaryfileRepository;

//	@Autowired
//	private AmpUserService ampUserService;

	
	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.findByNameKeywordCreator(String, String, String)
	 */
	public List<Bundle> findByNameKeywordCreator(String name, String keyword, String creator) {
		List<Bundle> bundles = null; 

		// if name is provided, only use name for query and ignore other criteria, as name is unique
		if (StringUtils.isNotBlank(name)) {
			bundles = bundleRepository.findByName(name);
		}		
		// otherwise, if both keyword and creator not empty, match both
		else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(creator)) {
			bundles = bundleRepository.findByNameContainingIgnoreCaseAndCreatedBy(keyword, creator);
		}		
		// otherwise, if keyword not empty (creator is), match keyword
		else if (StringUtils.isNotBlank(keyword)) {
			bundles = bundleRepository.findByNameContainingIgnoreCase(keyword);
		}
		// otherwise, creator not empty (keyword is), match creator
		else if (StringUtils.isNotBlank(creator)) {
			bundles = bundleRepository.findByCreatedBy(creator);
		}
		// otherwise all criteria are empty, match all
		else {
			bundles = bundleRepository.findBy();
		}
		
		log.info("Found " + bundles.size() + " bundles " + " matching: name: " + name + ", keyword: " + keyword + ", creator: " + creator);
		return bundles;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.findAllNamed()
	 */
	public List<Bundle> findAllNamed() {
		List<Bundle> bundles = bundleRepository.findAllWithNonEmptyNameNonEmptyPrimaryfiles();
		log.info("Successfully found " + bundles.size() + " named bundles.");
		return bundles;
	}

//	/**
//	 * @see edu.indiana.dlib.amppd.service.BundleService.findNamedByCurrentUser(String)
//	 */
//	public Bundle findNamedByCurrentUser(String name) {
//		String username = ampUserService.getCurrentUsername();
//		List<Bundle> bundles = bundleRepository.findByNameAndCreatedBy(name, ampUserService.getCurrentUsername());
//
//		// bundle name shall be unique per user if all bundles are created via AMPPD UI;
//		// however just in case there're more than one found, we will return the first
//		if (bundles == null || bundles.isEmpty()) {
//			log.info("No bundle found with name " + name + " for the current user " + username);
//			return null;
//		}
//
//		if (bundles.size() > 1) {
//			throw new RuntimeException("There are " + bundles.size() + " bundles found with name " + name + " for the current user " + username);
//		}
//
//		Bundle bundle = bundles.get(0);
//		log.info("Successfully found bundle " + bundle.getId() + " with name " + name + " for the current user " + username);
//		return bundle;
//	}

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.addPrimaryfile(Bundle, Long)
	 */
	@Override
	@Transactional
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

		bundle.getPrimaryfiles().add(primaryfile);	// need to add on bundle side since bundle owns the M:M relationship
		bundle = bundleRepository.save(bundle);	

		String msg = "Successfully added primaryfile <" + primaryfileId + "> to bundle<" + bundle.getId() + ">.";
		log.info(msg);
		return bundle;
	}	

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.deletePrimaryfile(Bundle, Long)
	 */
	@Override
	@Transactional
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

		bundle.getPrimaryfiles().remove(primaryfile); // need to remove from bundle side since bundle owns the M:M relationship
		bundle = bundleRepository.save(bundle);		

		String msg = "Ssuccessfully deleted primaryfile <" + primaryfileId + "> from bundle<" + bundle.getId() + ">.";
		log.info(msg);
		return bundle;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.addPrimaryfile(Long, Long)
	 */
	@Override
	@Transactional
	public Bundle addPrimaryfile(Long bundleId, Long primaryfileId) {
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("bundle <" + bundleId + "> does not exist!"));    
		return addPrimaryfile(bundle, primaryfileId);
	}	

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.deletePrimaryfile(Long, Long)
	 */
	@Override
	@Transactional
	public Bundle deletePrimaryfile(Long bundleId, Long primaryfileId) {		
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("bundle <" + bundleId + "> does not exist!"));    
		return deletePrimaryfile(bundle, primaryfileId);
	}	

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.addPrimaryfiles(Long, Long[])
	 */
	@Override
	@Transactional
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
	@Transactional
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
	 * @see edu.indiana.dlib.amppd.service.BundleService.updatePrimaryfiles(Long, String, String, Long[])
	 */
	@Override
	public Bundle updateBundle(Bundle bundle, String name, String description, Long[] primaryfileIds) {
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
		bundle.setPrimaryfiles(primaryfiles);

		// update name/description if provided
		if (name != null) {
			bundle.setName(name);
		}
		if (description != null) {
			bundle.setDescription(description);
		}
		
		log.info("Updated bundle instance " + bundle.getId() + " with name: " + name + ", description: " + description + " and " + primaryfiles.size() + " prifmaryfiles.");			
		return bundle;		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.updatePrimaryfiles(Long, String, Long[])
	 */
	@Override
	public Bundle updateBundle(Long bundleId, String name, String description, Long[] primaryfileIds) {
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("bundle <" + bundleId + "> does not exist!"));    		
		return updateBundle(bundle, name, description, primaryfileIds);
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
		log.info("Created bundle instance " + bundle.getId() + " with name " + name + " and " + primaryfiles.size() + " prifmaryfiles.");			
		return bundle;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.filterBundle(Bundle, Set<Long>)
	 */
	public boolean filterBundle(Bundle bundle, Set<Long> acUnitIds) {
		// for AMP admin, no need to filter for AC
		if (acUnitIds == null) return true;
		
		Set<Primaryfile> pfs = bundle.getPrimaryfiles();
		Set<Primaryfile> pfsF = new HashSet<Primaryfile>(); 

		// filter each primaryfile by its AC unit ID
		for (Primaryfile pf : pfs) {
			if (acUnitIds.contains(pf.getAcUnitId())) {
				pfsF.add(pf);
			}
		}

		// if number of primaryfiles didn't reduce, intact is true; otherwise false
		bundle.setPrimaryfiles(pfsF);
		boolean intact = pfsF.size() == pfs.size();

		log.info("Filtered " + pfs.size() + " primaryfiles into " + pfsF.size() + " within " + acUnitIds.size() + " accessible units for bundle " + bundle.getId());
		return intact;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.filterBundles(List<Bundle>, Set<Long>)
	 */
	public List<Bundle> filterBundles(List<Bundle> bundles, Set<Long> acUnitIds) {
		List<Bundle> bundlesF = new ArrayList<Bundle>(); 
		
		// filter each bundle
		// note that even for AMP admin, we still need to filter out empty bundle returned by query
		for (Bundle bundle : bundles) {			
			filterBundle(bundle, acUnitIds);
			
			// include only non=empty bundle			
			if (!bundle.getPrimaryfiles().isEmpty()) {
				bundlesF.add(bundle);
			}
		}
		
		log.info("Filtered " + bundles.size() + " bundles into " + bundlesF.size() + " non-empty ones.");
		return bundlesF;
	}
	
}
