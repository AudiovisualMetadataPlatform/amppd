package edu.indiana.dlib.amppd.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * 
 * @author yingfeng dfische3
 *
 */
@Service
@Slf4j
public class DropboxServiceImpl implements DropboxService {
	
	private CollectionRepository collectionRepository;
	
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
		return Paths.get(config.getDropboxRoot(), encodedUnitName);
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.getDropboxPath(String, String)
	 */
	@Override
	public Path getDropboxPath(String unitName, String collectionName) {
		Path unitPath = getDropboxPath(unitName);
		String encodedCollectionName = encodeUri(collectionName);
		System.out.println("Collection Name: "  + encodedCollectionName);
		return Paths.get(unitPath.toString(), encodedCollectionName);
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.createCollectionSubdir(Collection)
	 */
	@Override
	public Path createCollectionSubdir(Collection collection) {
		Path path = getDropboxPath(collection.getUnit().getName(), collection.getName());
		try {
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
		for (Collection collection : collections) {
			createCollectionSubdir(collection);
		}
	}

}
