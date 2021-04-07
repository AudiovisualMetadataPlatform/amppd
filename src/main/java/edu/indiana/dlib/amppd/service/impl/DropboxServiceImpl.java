package edu.indiana.dlib.amppd.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.service.DropboxService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of FileStorageService.
 * Dropbox contains sub-directories for users to drop their asset files for batch ingest. 
 * The sub-directories are organized in the same hierarchy as the asset contents, i.e. one directory per unit, 
 * under which one directory per collection. The names of the directories are encoded based on the unit/collection
 * name, respectively, with special characters replaced to avoid conflicts with OS directory naming rules.
 * @author yingfeng dfische3
 */
@Service
@Slf4j
public class DropboxServiceImpl implements DropboxService {
	
	@Autowired
	private CollectionRepository collectionRepository;
	
	@Autowired
	private AmppdPropertyConfig config; 	

	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.encodeUri(String)
	 */
	@Override
	public String encodeUri(String path) {
		Pattern pattern = Pattern.compile("[^A-Za-z0-9._-]");
        
		char[] chars = path.toCharArray();

		for (char ch : chars) {
			String charString = Character.toString(ch);
	        Matcher matcher = pattern.matcher(charString);
	        while (matcher.find()) {
	            String hexString = "%" + Integer.toHexString((int) ch);
	            path = path.replace(charString, hexString);
	        }
		}		
		
	    return path;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.getDropboxPath(String)
	 */
	@Override
	public Path getDropboxPath(String unitName) {
		String encodedUnitName = encodeUri(unitName);
		log.info("Dropbox Root: "  + config.getDropboxRoot());
		log.info("Encoded Unit Name: "  + encodedUnitName);
		return Paths.get(config.getDropboxRoot(), encodedUnitName);
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.getDropboxPath(String, String)
	 */
	@Override
	public Path getDropboxPath(String unitName, String collectionName) {
		Path unitPath = getDropboxPath(unitName);
		String encodedCollectionName = encodeUri(collectionName);
		log.info("Encoded Collection Name: "  + encodedCollectionName);
		return Paths.get(unitPath.toString(), encodedCollectionName);
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.getDropboxPath(Collection)
	 */
	@Override
	public Path getDropboxPath(Collection collection) {
		log.info("Unit: "  + collection.getUnit());
		log.info("Unit Name: "  + collection.getUnit().getName());
		log.info("Collection Name: "  + collection.getName());
		return getDropboxPath(collection.getUnit().getName(), collection.getName());
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.createCollectionSubdir(Collection)
	 */
	@Override
	public Path createCollectionSubdir(Collection collection) {
		Path path = getDropboxPath(collection);
		try {
			// directory is only created if not existing
			Files.createDirectories(path); 
			log.info("Dropbox sub-directory " + path + " has been created." );
			return path;
		}
		catch (IOException e) {
			throw new StorageException("Could not create dropbox sub-directory " + path, e);
		}		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.createCollectionSubdirs()
	 */
	@Override
	public void createCollectionSubdirs() {
		Iterable<Collection> collections = collectionRepository.findAll();
		
		// we could choose to skip a collection and continue with the rest in case an exception occurs;
		// however, if such IO exception happens, it usually is caused by system-wide issue instead of per collection
		// so likely there is no point to try other collections
		// meanwhile, admin can fix whatever causing the exception and rerun this process, 
		// in which case the creation will continue where it's stopped before
		
		for (Collection collection : collections) {
			createCollectionSubdir(collection);
		}
	}

}
