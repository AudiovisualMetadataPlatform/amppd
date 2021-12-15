package edu.indiana.dlib.amppd.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.exception.StorageFileNotFoundException;
import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.service.DataentityService;
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
		
	@Autowired
	private DataentityService dataentityService;
	
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
			throw new StorageException("Failed to initialize file storage root directory " + config.getFileStorageRoot(), e);
		}		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getDirPathname(Dataentity)
	 */
	@Override
	public String getDirPathname(Dataentity dataentity) {
		String pdir = "";
		String key = "";
		
		if (dataentity instanceof Unit) {
			// directory path for unit: U-<unitID>				
			key = "U";		
		}
		else if (dataentity instanceof Collection) {
			// directory path for collection: U-<unitID/C-<collectionId>
			pdir = getDirPathname(((Collection)dataentity).getUnit());
			key = "C";		
		}
		else if (dataentity instanceof Item) {
			// directory path for item: U-<unitID/C-<collectionId>/I-<itemId>
			pdir = getDirPathname(((Item)dataentity).getCollection());				
			key = "C";		
		}
		else if (dataentity instanceof Primaryfile) {
			// directory path for primaryfile: U-<unitID/C-<collectionId>/I-<itemId>/P-<primaryfileId>
			pdir = getDirPathname(((Primaryfile)dataentity).getItem());		
			key = "P";		
		}
		
		return pdir + File.separator + key + "-" + dataentity.getId();
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getFilePathname(Asset)
	 */
	@Override
	public String getFilePathname(Asset asset) {
		if (asset instanceof Primaryfile) {
			// file path for primaryfile: U-<unitID/C-<collectionId>/I-<itemId>/P-<primaryfileId>.<primaryExtension>
			return getDirPathname((Primaryfile)asset) + "." + FilenameUtils.getExtension(asset.getOriginalFilename());
		}
		else {
			// file name for supplement: S-<supplementId>
			String filename = "S-" + asset.getId() + "." + FilenameUtils.getExtension(asset.getOriginalFilename());
			
			// directory path for supplement depends on the parent of the supplement, could be in the directory of collection/item/primaryfile
			String dirname = "";
			
			if (asset instanceof CollectionSupplement) {
				dirname = getDirPathname(((CollectionSupplement)asset).getCollection());
			}
			else if (asset instanceof ItemSupplement) {
				dirname = getDirPathname(((ItemSupplement)asset).getItem());
			}
			else if (asset instanceof PrimaryfileSupplement) {
				dirname = getDirPathname(((PrimaryfileSupplement)asset).getPrimaryfile());
			}
			
			return dirname + File.separator  + filename;
		}
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadPrimaryfile(Long, MultipartFile)
	 */
	@Override
	@Transactional
	public Asset uploadAsset(Long id, MultipartFile file, SupplementType type) {		
    	Asset asset = dataentityService.findAsset(id, type);        	
    	return (Primaryfile)preprocessService.preprocess(uploadAsset(asset, file));
	}
		
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadPrimaryfile(Primaryfile, MultipartFile)
	 */
	@Override
	public Asset uploadAsset(Asset asset, MultipartFile file) {		
    	if (asset == null || file == null) {
    		throw new IllegalArgumentException("The given asset or file for uploading media file is null.");
    	}
    	
    	// for primaryfiles that have been run against workflows, do not allow uploading to replace existing media, 
    	// as this will cause discrepancy with existing workflow outputs, which are linked to existing media 	
    	if (asset instanceof Primaryfile ) {
    		Primaryfile primaryfile = (Primaryfile)asset;
	    	if (primaryfile.getDatasetId() != null || primaryfile.getHistoryId() != null) {
	    		throw new StorageException("Uploading new media file to primaryfile " + primaryfile.getId() + " is not allowed as it has been run against a workflow." );
	    	}
    	}
    		
    	// store the media file and update asset
    	asset.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));	
    	String targetPathname = getFilePathname(asset);    	    	
    	asset.setPathname(targetPathname);
    	store(file, targetPathname);    	    	    	
    	asset = dataentityService.saveAsset(asset);
    	
    	String msg = "Successfully uploaded asset " + asset.getId() + " media file " + file.getOriginalFilename() + " to " + targetPathname;
    	log.info(msg);
    	return asset;
	}	

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.moveEntityDir(Dataentity)
	 */
	public String moveEntityDir(Dataentity dataentity) {
		// get the media subdir pathname for the original and current dataentity
		// note that pathname will change if dataentity's parent change
		Dataentity oldentity = dataentityService.findOriginalDataentity(dataentity);
		String olddir = getDirPathname(oldentity);
		String newdir = getDirPathname(dataentity);
		
		// if the pathname isn't changed, do nothing
		if (StringUtils.pathEquals(olddir, newdir)) {
			return newdir;
		}
		
		// otherwise move the subdir
		Path path = move(olddir, newdir);
		if (path != null) {
	        log.info("Successfully moved dataentity " + dataentity.getId() + " media sub-directory: " + olddir + " -> " + newdir);
		}
		else {
	        log.info("No need to move non-existing dataentity " + dataentity.getId() + " media sub-directory: " + olddir + " -> " + newdir);			
		}

		return newdir;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.unloadAsset(Asset)
	 */
	@Override
	public String moveAsset(Asset asset) {
		// get the un-updated media/info file pathnames 
		String oldmedia = asset.getPathname();
        String oldJson = preprocessService.getMediaInfoJsonPath(oldmedia);        
        
		// get the possibly changed new media/info file pathnames due to asset parent change 
		String newMedia = getFilePathname(asset);
        String newJson = preprocessService.getMediaInfoJsonPath(newMedia);        
        
		// if the media pathname isn't changed, do nothing
		if (StringUtils.pathEquals(oldmedia, newMedia)) {
			return newMedia;
		}
		
		// otherwise move the media/info files
		Path mediaPath = move(oldmedia, newMedia);  
		Path jsonPath = move(oldJson, newJson);  
	
		// if media/info file doesn't exist throw error
		if (mediaPath == null) {
			throw new StorageException("Failed to move non-existing asset " + asset.getId() + " media file: " + oldmedia + " -> " + newMedia);
		}
		if (jsonPath == null) {
			throw new StorageException("Failed to move non-existing asset " + asset.getId() + " media info file: " + oldJson + " -> " + newJson);
		}
		
		// otherwise, update asset pathname
		asset.setPathname(newMedia);
		dataentityService.saveAsset(asset);	   
		
		log.info("Successfully moved asset " + asset.getId() + " media/info file : " + oldmedia + " -> " + newMedia + ", " + oldJson + " -> " + newJson);	
        return newMedia;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.deleteEntityDir(Dataentity)
	 */
	public String deleteEntityDir(Dataentity dataentity) {
		String pathname = getDirPathname(dataentity);
		Path path = delete(pathname);

		if (path != null) {
			log.info("Successfully deleted dataentity " + dataentity.getId() + " media sub-directory " + pathname);
		}
		else {
			log.info("No need to delete non-existing media sub-directory " + pathname);
    	}
		
		return pathname;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.unloadAsset(Asset)
	 */
	@Override
	public String unloadAsset(Asset asset) {
		String mediaPathname = asset.getPathname();
        String jsonPathname = preprocessService.getMediaInfoJsonPath(mediaPathname);        
        Path mediaPath = delete(mediaPathname);  
        Path jsonPath = delete(jsonPathname); 
        
        if (mediaPath != null) {
        	log.info("Successfully unloaded asset " + asset.getId() + " media file " + mediaPathname);
        }
        else {
        	log.error("Did not unload non-existing asset " + asset.getId() + " media file " + mediaPathname);
        }
        
        if (jsonPath != null) {
        	log.info("Successfully unloaded asset " + asset.getId() + " media info file " + jsonPathname);
        }
        else {
        	log.error("Did not unload non-existing asset " + asset.getId() + " media info file " + jsonPathname);
        }
        
        return mediaPathname;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.store(MultipartFile, String)
	 */
	@Override
	public Path store(MultipartFile file, String targetPathname) {
		String originalFilename = file.getOriginalFilename();

		if (file.isEmpty()) {
			throw new StorageException("Cannot store empty file "  + originalFilename + " to " + targetPathname);
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

			// save file content 
			Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
			
			log.debug("Successfully stored file " + originalFilename + " to " + targetPathname);
			return path;
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file " + originalFilename + " to " + targetPathname, e);
		}
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.move(String, String)
	 */    
	@Override
    public Path move(String sourcePathname, String targetPathname) {
    	try {
    		Path srcpath = resolve(sourcePathname);
    		Path tgtpath = resolve(targetPathname);
    		if (!Files.exists(srcpath)) {
    			return null;
    		}    		
    		if (Files.exists(tgtpath)) {
    	    	log.warn("Target directory/file " + targetPathname + " already exists and will be overwritten.");    			
    		}    		
	    	return Files.move(srcpath, tgtpath, StandardCopyOption.REPLACE_EXISTING);  		
    	}
    	catch (IOException e) {
    		throw new StorageException("Failed to move directory/file from " + sourcePathname + " to " + targetPathname, e);
    	}
    }

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.delete(String)
	 */    
	@Override
    public Path delete(String pathname) {
    	try {
    		Path path = resolve(pathname);
    		if (FileSystemUtils.deleteRecursively(path)) {
    			return path;
    		}
    		else {
        		return null;
    		}
    	}
    	catch (IOException e) {
    		throw new StorageException("Failed to delete directory/file " + pathname, e);
    	}
    }
    
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.linkFile(Path, Path)
	 */
	@Override
	public Path linkFile(Path sourcePath, Path targetPath) {
		try {
			// create a hard link
			Files.createLink(targetPath, sourcePath);

			// delete original file
			Files.delete(sourcePath);

			return targetPath;
		}
		catch (IOException e) {
			throw new StorageException("Failed to link directory/file from " + sourcePath + " to " + targetPath, e);
		}
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
                throw new StorageFileNotFoundException("Error while reading file " + pathname);
            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Error while reading file " + pathname, e);
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
    		throw new StorageException("Error while deleting all directories/files under file storage root.");
    	}  	
    }
	
	
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadPrimaryfile(Long, MultipartFile)
//	 */
//	@Override
//	@Transactional
//	public Primaryfile uploadPrimaryfile(Long id, MultipartFile file) {		
//    	Primaryfile primaryfile = primaryfileRepository.findById(id).orElseThrow(() -> new StorageException("Primaryfile <" + id + "> does not exist!"));        	
//    	return (Primaryfile)preprocessService.preprocess(uploadPrimaryfile(primaryfile, file));
//	}
//	
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadPrimaryfile(Primaryfile, MultipartFile)
//	 */
//	@Override
//	public Primaryfile uploadPrimaryfile(Primaryfile primaryfile, MultipartFile file) {		
//    	if (primaryfile == null) {
//    		throw new RuntimeException("The given primaryfile for uploading media file is null.");
//    	}
//    	
//    	// if primaryfile has been run against a workflow then do not allow uploading to replace existing media, 
//    	// as this will cause discrepancy with existing workflow outputs, which are linked to existing media 	
//    	if (primaryfile.getDatasetId() != null || primaryfile.getHistoryId() != null) {
//    		throw new StorageException("Uploading new media file to primaryfile " + primaryfile.getId() + " is not allowed as it has been run against a workflow." );
//    	}
//    		
//    	primaryfile.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));	
//    	String targetPathname = getFilePathname(primaryfile);    	    	
//    	primaryfile.setPathname(targetPathname);
//    	store(file, targetPathname);    	    	    	
//    	primaryfile = primaryfileRepository.save(primaryfile);
//    	
//    	String msg = "Primaryfile " + primaryfile.getId() + " has media file " + file.getOriginalFilename() + " successfully uploaded to " + targetPathname + ".";
//    	log.info(msg);
//    	return primaryfile;
//	}	
//
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadCollectionSupplement(Long, MultipartFile)
//	 */
//	@Override
//	@Transactional
//	public CollectionSupplement uploadCollectionSupplement(Long id, MultipartFile file) {		
//    	CollectionSupplement collectionSupplement = collectionSupplementRepository.findById(id).orElseThrow(() -> new StorageException("CollectionSupplement <" + id + "> does not exist!"));        	
//    	return (CollectionSupplement)preprocessService.preprocess(uploadCollectionSupplement(collectionSupplement, file));
//	}
//	
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadCollectionSupplement(CollectionSupplement, MultipartFile)
//	 */
//	@Override
//	public CollectionSupplement uploadCollectionSupplement(CollectionSupplement collectionSupplement, MultipartFile file) {		
//    	if (collectionSupplement == null) {
//    		throw new RuntimeException("The given collectionSupplement for uploading media file is null.");
//    	}
//    	
//    	collectionSupplement.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));	
//    	String targetPathname = getFilePathname(collectionSupplement);    	    	
//    	collectionSupplement.setPathname(targetPathname);
//    	store(file, targetPathname);    	
//    	collectionSupplement = collectionSupplementRepository.save(collectionSupplement);  
//    	
//    	String msg = "CollectionSupplement " + collectionSupplement.getId() + " has media file " + file.getOriginalFilename() + " successfully uploaded to " + targetPathname + ".";
//    	log.info(msg);
//    	return collectionSupplement;
//	}	
//
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadItemSupplement(Long, MultipartFile)
//	 */
//	@Override
//	@Transactional
//	public ItemSupplement uploadItemSupplement(Long id, MultipartFile file) {		
//    	ItemSupplement itemSupplement = itemSupplementRepository.findById(id).orElseThrow(() -> new StorageException("ItemSupplement <" + id + "> does not exist!"));        	
//    	return (ItemSupplement)preprocessService.preprocess(uploadItemSupplement(itemSupplement, file));
//	}
//	
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadItemSupplement(ItemSupplement, MultipartFile)
//	 */
//	@Override
//	public ItemSupplement uploadItemSupplement(ItemSupplement itemSupplement, MultipartFile file) {		
//    	if (itemSupplement == null) {
//    		throw new RuntimeException("The given itemSupplement for uploading media file is null.");
//    	}
//    	
//    	itemSupplement.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));	
//    	String targetPathname = getFilePathname(itemSupplement);    	    	
//    	itemSupplement.setPathname(targetPathname);
//    	store(file, targetPathname);    	
//    	itemSupplement = itemSupplementRepository.save(itemSupplement);  
//    	
//    	String msg = "ItemSupplement " + itemSupplement.getId() + " has media file " + file.getOriginalFilename() + " successfully uploaded to " + targetPathname + ".";
//    	log.info(msg);
//    	return itemSupplement;
//	}	
//
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadPrimaryfileSupplement(Long, MultipartFile)
//	 */
//	@Override
//	@Transactional
//	public PrimaryfileSupplement uploadPrimaryfileSupplement(Long id, MultipartFile file) {		
//    	PrimaryfileSupplement primaryfileSupplement = primaryfileSupplementRepository.findById(id).orElseThrow(() -> new StorageException("PrimaryfileSupplement <" + id + "> does not exist!"));        	
//    	return (PrimaryfileSupplement)preprocessService.preprocess(uploadPrimaryfileSupplement(primaryfileSupplement, file));
//	}
//	
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadPrimaryfileSupplement(PrimaryfileSupplement, MultipartFile)
//	 */
//	@Override
//	public PrimaryfileSupplement uploadPrimaryfileSupplement(PrimaryfileSupplement primaryfileSupplement, MultipartFile file) {		
//    	if (primaryfileSupplement == null) {
//    		throw new RuntimeException("The given primaryfileSupplement for uploading media file is null.");
//    	}
//    	
//    	primaryfileSupplement.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));	
//    	String targetPathname = getFilePathname(primaryfileSupplement);    	    	
//    	primaryfileSupplement.setPathname(targetPathname);
//    	store(file, targetPathname);    	
//    	primaryfileSupplement = primaryfileSupplementRepository.save(primaryfileSupplement);  
//    	
//    	String msg = "PrimaryfileSupplement " + primaryfileSupplement.getId() + " has media file " + file.getOriginalFilename() + " successfully uploaded to " + targetPathname + ".";
//    	log.info(msg);
//    	return primaryfileSupplement;
//	}	
	
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getDirPathname(Unit)
//	 */
//	@Override
//	public String getDirPathname(Unit unit) {
//		// directory path for unit: U-<unitID>
//		return "U-" + unit.getId();		
//	}
//
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getDirPathname(Collection)
//	 */
//	@Override
//	public String getDirPathname(Collection collection) {
//		// directory path for collection: U-<unitID/C-<collectionId>
//		return getDirPathname(collection.getUnit()) + File.separator + "C-" + collection.getId();
//	}
//
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getDirPathname(Item)
//	 */
//	@Override
//	public String getDirPathname(Item item) {
//		// directory path for item: U-<unitID/C-<collectionId>/I-<itemId>
//		return getDirPathname(item.getCollection()) + File.separator + "I-" + item.getId();
//	}
//
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getDirPathname(Primaryfile)
//	 */
//	@Override
//	public String getDirPathname(Primaryfile primaryfile) {
//		// directory path for primaryfile: U-<unitID/C-<collectionId>/I-<itemId>/P-<primaryfileId>
//		return getDirPathname(primaryfile.getItem()) + File.separator + "P-" + primaryfile.getId();
//	}
//	
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getFilePathname(Primaryfile)
//	 */
//	@Override
//	public String getFilePathname(Primaryfile primaryfile) {
//		// file path for primaryfile: U-<unitID/C-<collectionId>/I-<itemId>/P-<primaryfileId>.<primaryExtension>
//		return getDirPathname(primaryfile) + "." + FilenameUtils.getExtension(primaryfile.getOriginalFilename());
//	}
//
//	/**
//	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getFilePathname(Supplement)
//	 */
//	@Override
//	public String getFilePathname(Supplement supplement) {
//		// file name for supplement: S-<supplementId>
//		String filename = "S-" + supplement.getId() + "." + FilenameUtils.getExtension(supplement.getOriginalFilename());
//		
//		// directory path for supplement depends on the parent of the supplement, could be in the directory of collection/item/primaryfile
//		String dirname = "";
//		
//		if (supplement instanceof CollectionSupplement) {
//			dirname = getDirPathname(((CollectionSupplement)supplement).getCollection());
//		}
//		else if (supplement instanceof ItemSupplement) {
//			dirname = getDirPathname(((ItemSupplement)supplement).getItem());
//		}
//		else if (supplement instanceof PrimaryfileSupplement) {
//			dirname = getDirPathname(((PrimaryfileSupplement)supplement).getPrimaryfile());
//		}
//		
//		return dirname + File.separator  + filename;
//	}
    	
}
