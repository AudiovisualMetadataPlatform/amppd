package edu.indiana.dlib.amppd.service.impl;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.BundleService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BundleServiceImpl implements BundleService {
	
	@Autowired
    private BundleRepository bundleRepository;
	
	@Autowired
    private PrimaryfileRepository primaryfileRepository;

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

		primaryfile.getBundles().add(bundle);	// need to add from primaryfile side since Primaryfile owns the M:M relationship
		bundle.getPrimaryfiles().add(primaryfile);	// TODO do we need this?
		primaryfileRepository.save(primaryfile);
		bundleRepository.save(bundle);	// TODO do we need this?
		
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

		primaryfile.getBundles().remove(bundle);	// need to remove from primaryfile side since Primaryfile owns the M;M relationship
		bundle.getPrimaryfiles().remove(primaryfile);		// TODO do we need this?
		primaryfileRepository.save(primaryfile);
		bundleRepository.save(bundle);		// TODO do we need this?

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
		}
		for (Long primaryfileId : primaryfileIds) {
			addPrimaryfile(bundleId, primaryfileId);			
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
		}
		for (Long primaryfileId : primaryfileIds) {
			deletePrimaryfile(bundleId, primaryfileId);			
		}		
		return bundle;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.BundleService.createBundle(String, Set<Primaryfile>)
	 */	
	@Override
	public Bundle createBundle(String name, Set<Primaryfile> prifmaryfiles) {
		if (StringUtils.isEmpty(name)) {
			log.error("The given bundle name is empty!");
			return null;
		}		

		if (prifmaryfiles == null) {
			log.error("The given prifmaryfiles is null!");
			return null;
		}		

		Bundle bundle = new Bundle();
		bundle.setName(name);
		bundle.setPrimaryfiles(prifmaryfiles);
		bundle = bundleRepository.save(bundle);
		
		log.info("Successfully created new bundle " + bundle.getId() + " with name " + name + " and " + prifmaryfiles.size() + " prifmaryfiles.");			
		return bundle;
	}
	 
}
