package edu.indiana.dlib.amppd.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
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
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.PreprocessService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of FileStorageService.
 * Directory hierarchy: Root - Unit - Collection - Item - Primaryfile - Supplement
 * Naming convention: At each level of the above hierarchy, 
 * directory names abide to this format: <1st Letter of the entity class>-<ID of the entity>
 * file names abide to this format: <1st Letter of the entity class>-<ID of the entity>.<file extension>
 * @author yingfeng
 */
@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {	

	@Autowired
    private PrimaryfileRepository primaryfileRepository;
		
	@Autowired
    private CollectionSupplementRepository collectionSupplementRepository;

	@Autowired
    private ItemSupplementRepository itemSupplementRepository;

	@Autowired
    private PrimaryfileSupplementRepository primaryfileSupplementRepository;
	
	@Autowired
    private PreprocessService preprocessService;
	
	private AmppdPropertyConfig config; 	
	private Path root;

	@Autowired
	public FileStorageServiceImpl(AmppdPropertyConfig amppdconfig) {
		// initialize Amppd file system 
		config = amppdconfig;
		try {
			root = Paths.get(config.getFileStorageRoot());
			Files.createDirectories(root);	// creates root directory if not already exists
			Files.createDirectories(Paths.get(config.getDropboxRoot()));	// creates batch root directory if not already exists
			log.info("File storage root directory " + config.getFileStorageRoot() + " has been created." );
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize file storage root directory " + config.getFileStorageRoot(), e);
		}		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadPrimaryfile(Long, MultipartFile)
	 */
	@Override
	public Primaryfile uploadPrimaryfile(Long id, MultipartFile file) {		
    	Primaryfile primaryfile = primaryfileRepository.findById(id).orElseThrow(() -> new StorageException("Primaryfile <" + id + "> does not exist!"));        	
    	return (Primaryfile)preprocessService.preprocess(uploadPrimaryfile(primaryfile, file));
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadPrimaryfile(Primaryfile, MultipartFile)
	 */
	@Override
	public Primaryfile uploadPrimaryfile(Primaryfile primaryfile, MultipartFile file) {		
    	if (primaryfile == null) {
    		throw new RuntimeException("The given primaryfile for uploading media file is null.");
    	}
    	
    	// if primaryfile has been run against a workflow then do not allow uploading to replace existing media, 
    	// as this will cause discrepancy with existing workflow outputs, which are linked to existing media 	
    	if (primaryfile.getDatasetId() != null || primaryfile.getHistoryId() != null) {
    		throw new StorageException("Uploading new media file to primaryfile " + primaryfile.getId() + " is not allowed as it has been run against a workflow." );
    	}
    		
    	primaryfile.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));	
    	String targetPathname = getFilePathname(primaryfile);    	    	
    	primaryfile.setPathname(targetPathname);
    	store(file, targetPathname);    	
    	    	
    	primaryfile = primaryfileRepository.save(primaryfile);      	
    	String msg = "Primaryfile " + primaryfile.getId() + " has media file " + file.getOriginalFilename() + " successfully uploaded to " + targetPathname + ".";
    	log.info(msg);
    	return primaryfile;
	}	

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadCollectionSupplement(Long, MultipartFile)
	 */
	@Override
	public CollectionSupplement uploadCollectionSupplement(Long id, MultipartFile file) {		
    	CollectionSupplement collectionSupplement = collectionSupplementRepository.findById(id).orElseThrow(() -> new StorageException("CollectionSupplement <" + id + "> does not exist!"));        	
    	return (CollectionSupplement)preprocessService.preprocess(uploadCollectionSupplement(collectionSupplement, file));
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadCollectionSupplement(CollectionSupplement, MultipartFile)
	 */
	@Override
	public CollectionSupplement uploadCollectionSupplement(CollectionSupplement collectionSupplement, MultipartFile file) {		
    	if (collectionSupplement == null) {
    		throw new RuntimeException("The given collectionSupplement for uploading media file is null.");
    	}
    	
    	collectionSupplement.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));	
    	String targetPathname = getFilePathname(collectionSupplement);    	    	
    	collectionSupplement.setPathname(targetPathname);
    	store(file, targetPathname);    	
    	collectionSupplement = collectionSupplementRepository.save(collectionSupplement);  
    	
    	String msg = "CollectionSupplement " + collectionSupplement.getId() + " has media file " + file.getOriginalFilename() + " successfully uploaded to " + targetPathname + ".";
    	log.info(msg);
    	return collectionSupplement;
	}	

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadItemSupplement(Long, MultipartFile)
	 */
	@Override
	public ItemSupplement uploadItemSupplement(Long id, MultipartFile file) {		
    	ItemSupplement itemSupplement = itemSupplementRepository.findById(id).orElseThrow(() -> new StorageException("ItemSupplement <" + id + "> does not exist!"));        	
    	return (ItemSupplement)preprocessService.preprocess(uploadItemSupplement(itemSupplement, file));
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadItemSupplement(ItemSupplement, MultipartFile)
	 */
	@Override
	public ItemSupplement uploadItemSupplement(ItemSupplement itemSupplement, MultipartFile file) {		
    	if (itemSupplement == null) {
    		throw new RuntimeException("The given itemSupplement for uploading media file is null.");
    	}
    	
    	itemSupplement.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));	
    	String targetPathname = getFilePathname(itemSupplement);    	    	
    	itemSupplement.setPathname(targetPathname);
    	store(file, targetPathname);    	
    	itemSupplement = itemSupplementRepository.save(itemSupplement);  
    	
    	String msg = "ItemSupplement " + itemSupplement.getId() + " has media file " + file.getOriginalFilename() + " successfully uploaded to " + targetPathname + ".";
    	log.info(msg);
    	return itemSupplement;
	}	

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadPrimaryfileSupplement(Long, MultipartFile)
	 */
	@Override
	public PrimaryfileSupplement uploadPrimaryfileSupplement(Long id, MultipartFile file) {		
    	PrimaryfileSupplement primaryfileSupplement = primaryfileSupplementRepository.findById(id).orElseThrow(() -> new StorageException("PrimaryfileSupplement <" + id + "> does not exist!"));        	
    	return (PrimaryfileSupplement)preprocessService.preprocess(uploadPrimaryfileSupplement(primaryfileSupplement, file));
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadPrimaryfileSupplement(PrimaryfileSupplement, MultipartFile)
	 */
	@Override
	public PrimaryfileSupplement uploadPrimaryfileSupplement(PrimaryfileSupplement primaryfileSupplement, MultipartFile file) {		
    	if (primaryfileSupplement == null) {
    		throw new RuntimeException("The given primaryfileSupplement for uploading media file is null.");
    	}
    	
    	primaryfileSupplement.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));	
    	String targetPathname = getFilePathname(primaryfileSupplement);    	    	
    	primaryfileSupplement.setPathname(targetPathname);
    	store(file, targetPathname);    	
    	primaryfileSupplement = primaryfileSupplementRepository.save(primaryfileSupplement);  
    	
    	String msg = "PrimaryfileSupplement " + primaryfileSupplement.getId() + " has media file " + file.getOriginalFilename() + " successfully uploaded to " + targetPathname + ".";
    	log.info(msg);
    	return primaryfileSupplement;
	}	

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.store(MultipartFile, String)
	 */
	@Override
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
				Path path = resolve(targetPathname);
				
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
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.resolve(String)
	 */
	@Override
    public Path resolve(String pathname) {
        return root.resolve(pathname);
    }

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.resolve(String)
	 */
	@Override
    public String absolutePathName(String pathname) {
        return resolve(pathname).toAbsolutePath().toString();
    }

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.loadAsResource(String)
	 */
	@Override
	public Resource loadAsResource(String pathname) {
        try {
            Path file = resolve(pathname);
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
	@Override
    public void delete(String pathname) {
    	try {
    		Path path = resolve(pathname);
    		FileSystemUtils.deleteRecursively(path);
    		log.info("Successfully deleted directory/file " + pathname);
    	}
    	catch (IOException e) {
    		throw new StorageException("Could not delete file " + pathname, e);
    	}
    }
    
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.cleanup()
	 */
	@Override
    public void cleanup() {
    	try {
    		FileUtils.cleanDirectory(new File(root.toString()));
    		log.info("Successfully deleted all directories/files under file storage root.");
    	}
    	catch (IOException e) {
    		throw new StorageException("Could not delete all directories/files under file storage root.");
    	}  	
    }
    
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getDirPathname(Unit)
	 */
	@Override
	public String getDirPathname(Unit unit) {
		// directory path for unit: U-<unitID>
		return "U-" + unit.getId();		
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getDirPathname(Collection)
	 */
	@Override
	public String getDirPathname(Collection collection) {
		// directory path for collection: U-<unitID/C-<collectionId>
		return getDirPathname(collection.getUnit()) + File.separator + "C-" + collection.getId();
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getDirPathname(Item)
	 */
	@Override
	public String getDirPathname(Item item) {
		// directory path for item: U-<unitID/C-<collectionId>/I-<itemId>
		return getDirPathname(item.getCollection()) + File.separator + "I-" + item.getId();
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getDirPathname(Primaryfile)
	 */
	@Override
	public String getDirPathname(Primaryfile primaryfile) {
		// directory path for primaryfile: U-<unitID/C-<collectionId>/I-<itemId>/P-<primaryfileId>
		return getDirPathname(primaryfile.getItem()) + File.separator + "P-" + primaryfile.getId();
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getFilePathname(Primaryfile)
	 */
	@Override
	public String getFilePathname(Primaryfile primaryfile) {
		// file path for primaryfile: U-<unitID/C-<collectionId>/I-<itemId>/P-<primaryfileId>.<primaryExtension>
		return getDirPathname(primaryfile) + "." + FilenameUtils.getExtension(primaryfile.getOriginalFilename());
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getFilePathname(Supplement)
	 */
	@Override
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
	
	public void moveFile(Path sourcePath, Path destinationPath) throws IOException {
		// Create a hard link
		Files.createLink(destinationPath, sourcePath);
		
		// If the new file doesn't exists for some reason, throw an exception
		if(!Files.exists(destinationPath)) {
			throw new FileNotFoundException(String.format("File %s failed to create.", destinationPath.getFileName()));
		}
		
		// Delete original
		Files.delete(sourcePath);
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.readTextFile(String)
	 */
	@Override
	public String readTextFile(String pathame) {
		try {
			return Files.readString(resolve(pathame));
		}
		catch(IOException e) {
			throw new StorageFileNotFoundException("Error reading file " + pathame, e);
		}
	}
	
}
