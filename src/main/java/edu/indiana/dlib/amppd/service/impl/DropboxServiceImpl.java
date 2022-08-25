package edu.indiana.dlib.amppd.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.service.DropboxService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of DropboxService.
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
	private UnitRepository unitRepository;
	
	@Autowired
	private CollectionRepository collectionRepository;
	
	@Autowired
	private DataentityService dataentityService;
	
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
	 * @see edu.indiana.dlib.amppd.service.DropboxService.getSubDirPath(String)
	 */
	@Override
	public Path getSubDirPath(String unitName) {
		String encodedUnitName = encodeUri(unitName);
		return Paths.get(config.getDropboxRoot(), encodedUnitName);
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.getSubDirPath(Unit)
	 */
	@Override
	public Path getSubDirPath(Unit unit) {
		return getSubDirPath(unit.getName());
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.getSubDirPath(String, String)
	 */
	@Override
	public Path getSubDirPath(String unitName, String collectionName) {
		Path unitPath = getSubDirPath(unitName);
		String encodedCollectionName = encodeUri(collectionName);
		return Paths.get(unitPath.toString(), encodedCollectionName);
	}
		
	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.getSubDirPath(Collection)
	 */
	@Override
	public Path getSubDirPath(Collection collection) {
		log.trace("collection = " + collection);
		log.trace("collection.getUnit() = "  + collection.getUnit());
		return getSubDirPath(collection.getUnit().getName(), collection.getName());
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.renameSubdir(Unit)
	 */
	@Override
	public Path renameSubdir(Unit unit) {
		// get the dropbox subdir pathnames for the original and current unit
		Unit oldUnit = (Unit)dataentityService.findOriginalDataentity(unit);    
		Path oldPath = getSubDirPath(oldUnit);
		Path path = getSubDirPath(unit);
		
		/* TODO
		 * Because dataentityService.findOriginalDataentity doesn't work (see its TODO comment),
		 * the oldPath will the same as path. If old unit dir doesn't exist, that's OK, new one won't be created if not exist;
		 * otherwise old dir won't be renamed to the new one, causing its collections dropboxes not found
		 */
		
		// only rename subdir if the name changed and the previous subdir exists 
		return move(oldPath, path, unit.getId(), false);	
	}	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.renameSubdir(Collection)
	 */
	@Override
	public Path renameSubdir(Collection collection) {
		// get the dropbox subdir pathnames for the original and current collection
		Collection oldCol = (Collection)dataentityService.findOriginalDataentity(collection);     
		Path oldPath = getSubDirPath(oldCol);
		Path path = getSubDirPath(collection);
		
		/* TODO
		 * Because dataentityService.findOriginalDataentity doesn't work (see its TODO comment),
		 * the oldPath will the same as path. 
		 * If old collection dir doesn't exist, that's OK, the new dir will be created if not already exists;
		 * otherwise old dir won't be renamed to the new one, ending up with both old and new dirs exist.
		 */
		
		// rename/move subdir from oldPath to current path as needed
		return move(oldPath, path, collection.getId(), true);		
	}	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.moveSubdir(Collection, Unit)
	 */
	@Override
	public Path moveSubdir(Collection collection, Unit unit) {	
		// get the dropbox subdir pathname for the collection before updating its parent
		Path oldPath = getSubDirPath(collection);

		// get the subdir pathname for the collection after updating its parent
		// note that pathname will change if collection's parent is changed
		collection.setUnit(unit);
		Path path = getSubDirPath(collection);
		
		// move subdir from oldPath to current path as needed
		return move(oldPath, path, collection.getId(), true);	
	}
	
	/**
	 * Move dropbox sub-directory of the collection/unit with the give ID from the given oldPath to the given new path;
	 * if old sub-directory doesn't exist, create the new one.
	 */
	protected Path move(Path oldPath, Path path, Long id, boolean forCollection) {
		String entity = forCollection ? "collection " : "unit ";
	
		try {
			// if previous subdir doesn't exist for collection, create the new one with warning	
			if (!Files.exists(oldPath) && forCollection) {
				Files.createDirectories(path); 
				log.warn("Dropbox sub-directory " + oldPath + " doesn't exit, created the new one " + path + " for " + entity + id);
				return path;
			}

			// otherwise, no action if the source and target subdirs are the same
			if (oldPath.equals(path)) {
				log.warn("Dropbox source and target sub-directories are the same: " + path + ", no need to move for " + entity + id);
				return path;
			}

			// otherwise move subdir
			if (Files.exists(path)) {
				log.warn("Dropbox target sub-directory " + path + " already exists and will be replaced.");    			
			}    		
			else if (forCollection) {
				// make sure target path's parent unit subdir exists
				Files.createDirectories(path.getParent());
			}				
			Files.move(oldPath,  path, StandardCopyOption.REPLACE_EXISTING);  	
			
			log.info("Successfully moved dropbox sub-directory " + oldPath + " to " + path + " for " + entity + id);
			return path;
		}
		catch (IOException e) {
			throw new StorageException("Failed to move dropbox sub-directory " + oldPath + " to " + path + " for " + entity + id, e);
		}		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.deleteSubdir(Unit)
	 */
	@Override
	public Path deleteSubdir(Unit unit) {
		Path path = getSubDirPath(unit);
		
		try {
			FileSystemUtils.deleteRecursively(path);
			log.info("Dropbox sub-directory " + path + " has been deleted for unit " + unit.getId());
			return path;
		}
		catch (IOException e) {
			throw new StorageException("Failed to delete dropbox sub-directory "  + path + " for unit " + unit.getId(), e);
		}		
	}	
		
	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.deleteSubdir(Collection)
	 */
	@Override
	public Path deleteSubdir(Collection collection) {
		Path path = getSubDirPath(collection);
		
		try {
			FileSystemUtils.deleteRecursively(path);
			log.info("Dropbox sub-directory " + path + " has been deleted for collection " + collection.getId());
			return path;
		}
		catch (IOException e) {
			throw new StorageException("Failed to delete dropbox sub-directory "  + path + " for collection " + collection.getId(), e);
		}		
	}	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DropboxService.createSubdir(Collection)
	 */
	@Override
	public Path createSubdir(Collection collection) {
		Path path = getSubDirPath(collection);
		
		try {
			// directory is only created if not pre-existing
			Files.createDirectories(path); 
			log.info("Dropbox sub-directory " + path + " for collection " + collection.getName() + " has been created." );
			return path;
		}
		catch (IOException e) {
			throw new StorageException("Failed to create dropbox sub-directory " + path + " for collection " + collection.getName(), e);
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
			createSubdir(collection);
		}
	}

	// TODO add cleanupCollectionSubdirs() do remove all unused subdirs due to inconsistent manual operations
	
}
