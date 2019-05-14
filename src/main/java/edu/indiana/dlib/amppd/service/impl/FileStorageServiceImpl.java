package edu.indiana.dlib.amppd.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.exception.StorageFileNotFoundException;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.service.FileStorageService;
import lombok.extern.java.Log;

/**
 * Implementation of FileStorageService.
 * Directory hierarchy: Root - Unit - Collection - Item - Primaryfile - Supplement
 * Naming convention: At each level of the above hierarchy, 
 * directory names abide to this format: <1st Letter of the entity class>-<ID of the entity>
 * file names abide to this format: <1st Letter of the entity class>-<ID of the entity>.<file extension>
 * @author yingfeng
 *
 */
@Service
@Log
public class FileStorageServiceImpl implements FileStorageService {

	@Value("${amppd.filesys.root:/tmp/amppd/}")
	private String rootPathname;

	private Path root;

	@Autowired
	public FileStorageServiceImpl() {
		try {			
			if (rootPathname == null) rootPathname = "/tmp/amppd/"; // TODO: AMP-69 remove below, which is a tmp work-around for @Value not taking effect
			root = Paths.get(rootPathname);
			Files.createDirectories(root);	// creates root directory if not already exists
			log.info("File storage root directory " + rootPathname + " has been created." );
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.store(MultipartFile, String)
	 */
	public void store(MultipartFile file, String targetPathname) {
		String originalFilename = file.getOriginalFilename();
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file "  + originalFilename + " to " + targetPathname);
			}
			if (originalFilename.startsWith("..")) {
				// This is a security check
				throw new StorageException("Cannot store file " + originalFilename + " with relative path outside current directory to " + targetPathname);
			}
			try (InputStream inputStream = file.getInputStream()) {
				// TODO: consider FileAttributes for access control
				Path path = root.resolve(targetPathname);
				
				// create parent directory for targetPathname if not exists yet
				Files.createDirectories(path.getParent());
				
				Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
				log.info("Successfully stored file " + originalFilename + " to " + targetPathname);
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file " + originalFilename + " to " + targetPathname, e);
		}
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.load(MString)
	 */
    public Path load(String pathname) {
        return root.resolve(pathname);
    }

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.loadAsResource(String)
	 */
    public Resource loadAsResource(String pathname) {
        try {
            Path file = load(pathname);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException("Could not read file " + pathname);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file " + pathname, e);
        }
    }	

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.delete(String)
	 */    
    public void delete(String pathname) {
    	try {
    		Path path = load(pathname);
    		FileSystemUtils.deleteRecursively(path);
    		log.info("Successfully deleted directory/file " + pathname);
    	}
    	catch (IOException e) {
    		throw new StorageException("Could not delete file " + pathname, e);
    	}
    }
    
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.deleteAll()
	 */
    public void deleteAll() {
    	try {
    		FileSystemUtils.deleteRecursively(root);
    		log.info("Successfully deleted all directories/files under file storage root.");
    	}
    	catch (IOException e) {
    		throw new StorageException("Could not delete all directories/files under file storage root.");
    	}  	
    }

    
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getDirPathname(Unit)
	 */
	public String getDirPathname(Unit unit) {
		// directory path for unit: U-<unitID>
		return "U-" + unit.getId();		
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getDirPathname(Collection)
	 */
	public String getDirPathname(Collection collection) {
		// directory path for collection: U-<unitID/C-<collectionId>
		return getDirPathname(collection.getUnit()) + File.separator + "C-" + collection.getId();
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getDirPathname(Item)
	 */
	public String getDirPathname(Item item) {
		// directory path for item: U-<unitID/C-<collectionId>/I-<itemId>
		return getDirPathname(item.getCollection()) + File.separator + "I-" + item.getId();
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getDirPathname(Primaryfile)
	 */
	public String getDirPathname(Primaryfile primaryfile) {
		// directory path for primaryfile: U-<unitID/C-<collectionId>/I-<itemId>/P-<primaryfileId>
		return getDirPathname(primaryfile.getItem()) + File.separator + "P-" + primaryfile.getId();
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getFilePathname(Primaryfile)
	 */
	public String getFilePathname(Primaryfile primaryfile) {
		// file path for primaryfile: U-<unitID/C-<collectionId>/I-<itemId>/P-<primaryfileId>.<primaryExtension>
		return getDirPathname(primaryfile) + "." + FilenameUtils.getExtension(primaryfile.getOriginalFilename());
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getFilePathname(Supplement)
	 */
	public String getFilePathname(Supplement supplement) {
		// file name for supplement: S-<supplementId>
		String filename = "S-" + supplement.getId() + "." + FilenameUtils.getExtension(supplement.getOriginalFilename());
		
		// directory path for supplement depends on the parent of the supplement, could be in the directory of collection/item/primaryfile
		String dirname = "";
		
		if (supplement instanceof CollectionSupplement) {
			dirname = getDirPathname(((CollectionSupplement)supplement).getCollection());
		}
		else if (supplement instanceof ItemSupplement) {
			dirname = getDirPathname(((ItemSupplement)supplement).getItem());
		}
		else if (supplement instanceof PrimaryfileSupplement) {
			dirname = getDirPathname(((PrimaryfileSupplement)supplement).getPrimaryfile());
		}
		
		return dirname + File.separator  + filename;
	}

}
