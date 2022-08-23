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
import edu.indiana.dlib.amppd.model.UnitSupplement;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.MediaService;
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
	WorkflowResultRepository workflowResultRepository;
	
	@Autowired
    private PreprocessService preprocessService;
		
	@Autowired
	private MediaService mediaService;

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
			if(!Files.exists(root)) {  // corner case where createDirectories() fails when it is a symlink
				Files.createDirectories(root);	// creates root directory if not already exists
			}
			Path dropbox = Paths.get(config.getDropboxRoot());
			if(!Files.exists(dropbox)) {  // corner case where createDirectories() fails when it is a symlink
				Files.createDirectories(dropbox);	// creates batch root directory if not already exists
			}
			log.info("File storage root directory " + root + " has been created." );
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
			key = "I";		
		}
		else if (dataentity instanceof Primaryfile) {
			// directory path for primaryfile: U-<unitID/C-<collectionId>/I-<itemId>/P-<primaryfileId>
			pdir = getDirPathname(((Primaryfile)dataentity).getItem());		
			key = "P";		
		}
		else {
			throw new IllegalArgumentException("The given dataentity " + dataentity.getId() + " is of invalid type.");
		}
		
		String pathname = pdir.isEmpty() ? key + "-" + dataentity.getId() : pdir + File.separator + key + "-" + dataentity.getId();
		return pathname;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.getFilePathname(Asset)
	 */
	@Override
	public String getFilePathname(Asset asset) {
		if (asset instanceof Primaryfile) {
			// file path for primaryfile: U-<unitID/C-<collectionId>/I-<itemId>/P-<primaryfileId>.<primaryExtension>
			return getDirPathname(asset) + "." + FilenameUtils.getExtension(asset.getOriginalFilename());
		}

		// file name for supplement: S-<supplementId>
		String filename = "S-" + asset.getId() + "." + FilenameUtils.getExtension(asset.getOriginalFilename());

		// directory path for supplement depends on the parent of the supplement, could be in the directory of collection/item/primaryfile
		String dirname = "";

		if (asset instanceof UnitSupplement) {
			dirname = getDirPathname(((UnitSupplement)asset).getUnit());
		}
		else if (asset instanceof CollectionSupplement) {
			dirname = getDirPathname(((CollectionSupplement)asset).getCollection());
		}
		else if (asset instanceof ItemSupplement) {
			dirname = getDirPathname(((ItemSupplement)asset).getItem());
		}
		else if (asset instanceof PrimaryfileSupplement) {
			dirname = getDirPathname(((PrimaryfileSupplement)asset).getPrimaryfile());
		}

		String pathname = dirname + File.separator  + filename;
		return pathname;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadAsset(Long, MultipartFile, SupplementType)
	 */
	@Override
	@Transactional
	public Asset uploadAsset(Long id, MultipartFile file, SupplementType type) {		
    	Asset asset = dataentityService.findAsset(id, type);        	
    	asset = uploadAsset(asset, file);
    	return asset;
	}
		
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.uploadAsset(Asset, MultipartFile)
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
    		// check if there is any workflow results associated with the primayfile
    		// this is more accurate than checking whether the historyId/datasetId is not null,
    		// as the primayfile could have been submitted to workflows but never succeeded with any result,
    		// in which case it's fine (and might be desirable if due to corrupt media file) to replace the file
//	    	if (primaryfile.getDatasetId() != null || primaryfile.getHistoryId() != null) {
    		if (workflowResultRepository.findByPrimaryfileId(primaryfile.getId()).size() > 0) {
	    		throw new StorageException("Uploading new media file to primaryfile " + primaryfile.getId() + " is not allowed as it has been run against a workflow." );
	    	}
    	}
    		
    	// store the media file and update asset path
    	asset.setOriginalFilename(StringUtils.cleanPath(file.getOriginalFilename()));	
    	String targetPathname = getFilePathname(asset);    	    	
    	asset.setPathname(targetPathname);
    	store(file, targetPathname);  
    	
    	// preprocess asset and save to DB
    	asset = preprocessService.preprocess(asset, true);
    	
    	String msg = "Successfully uploaded asset " + asset.getId() + " media file " + file.getOriginalFilename() + " to " + targetPathname;
    	log.info(msg);
    	return asset;
	}	

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.moveEntityDir(Dataentity, Dataentity)
	 */
	@Override
	public String moveEntityDir(Dataentity dataentity, Dataentity parent) {	
		// get the media subdir pathname for the dataentity before updating its parent
		String olddir = getDirPathname(dataentity);

		// if the parents are the same, no need to move subdir
		if (dataentityService.getParentDataentity(dataentity).getId().equals(parent.getId())) {
			return olddir;
		}
		
		// otherwise, get the media subdir pathname for the dataentity after updating its parent
		// note that pathname will change as dataentity's parent is changed
		dataentityService.setParentDataentity(dataentity, parent);
		String newdir = getDirPathname(dataentity);
		
		/* TODO
		 * Below code is commented out, because if we move entity subdir, all the media files under that subdir hieararchy 
		 * will have their paths changed; thus, all the corresponding child entities need to have their pathnames updated.
		 * This would be too much overhead and by all means need to be avoided. Without moving entity subdir, the original
		 * dataentity media subidr hierarchy won't be enforced, but this wouldn't break the code or functionality, 
		 * as the saved asset pathnames shall still be valid. Two other optionss for long term solution:
		 * 1. Enforce the media subdir hierarchy: instead of saving pathname in Asset, always infer asset pathname using 
		 * the getter to compute the pathname based on the parent hierarchy; meanwhile, moveEntityDir when entity is moved.
		 * 2. Use a flat media dir structure instead of based on parent hierarchy; this way, there is no entity subdir and
		 * no need to move asset file when entity is moved; asset pathname can either be inferred from ID or saved with Asset. 
		 */		
		// and move the subdir
//		Path path = move(olddir, newdir);
//		if (path != null) {
//	        log.info("Successfully moved dataentity " + dataentity.getId() + " media sub-directory: " + olddir + " -> " + newdir);
//		}
//		else {
//	        log.info("No need to move non-existing dataentity " + dataentity.getId() + " media sub-directory: " + olddir + " -> " + newdir);			
//		}

		return newdir;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.moveEntityDir(Dataentity)
	 */
	@Override
	public String moveEntityDir(Dataentity dataentity) {
		/* TODO
		 * This method won't have any effect, see explanation in DataentityServiceImpl.findOriginalDataentity.
		 * On the other hand, we shouldn't just moveEntityDir anyways due to the reason explained in above moveEntityDir method.
		 */
		
		// get the media subdir pathname for the original and current dataentity
		// note that pathname will change if dataentity's parent is changed
		Dataentity oldentity = dataentityService.findOriginalDataentity(dataentity);
		String olddir = getDirPathname(oldentity);
		String newdir = getDirPathname(dataentity);
		
		// if the pathname isn't changed, do nothing
		if (StringUtils.pathEquals(olddir, newdir)) {
			return olddir;
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
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.unloadAsset(Asset, boolean)
	 */
	@Override
	public Asset moveAsset(Asset asset, boolean persist) {
		// get the un-updated media/info file pathnames 
		String oldmedia = asset.getPathname();
        String oldJson = preprocessService.getMediaInfoJsonPath(oldmedia);        
        
		// get the possibly changed new media/info file pathnames due to asset parent change 
		String newMedia = getFilePathname(asset);
        String newJson = preprocessService.getMediaInfoJsonPath(newMedia);        
        
		// if the media pathname isn't changed, do nothing
		if (StringUtils.pathEquals(oldmedia, newMedia)) {
			return asset;
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
		
		// otherwise, update asset pathname and reset symlink
		asset.setPathname(newMedia);
		mediaService.resetSymlink(asset, false);
		
		// save asset if indicated		
		if (persist) {
			asset = dataentityService.saveAsset(asset); 
		} 
		
		log.info("Successfully moved asset " + asset.getId() + " media/info file : " + oldmedia + " -> " + newMedia + ", " + oldJson + " -> " + newJson);	
        return asset;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.FileStorageService.deleteEntityDir(Dataentity)
	 */
	@Override
	public String deleteEntityDir(Dataentity dataentity) {
		String pathname = getDirPathname(dataentity);
		Path path = delete(pathname);

		if (path != null) {
			log.info("Successfully deleted dataentity " + dataentity.getId() + " media sub-directory " + pathname);
		}
		else {
			log.info("No need to delete non-existing dataentity " + dataentity.getId() + " media sub-directory " + pathname);
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
        	log.error("No need to unload non-existing asset " + asset.getId() + " media file " + mediaPathname);
        }
        
        if (jsonPath != null) {
        	log.info("Successfully unloaded asset " + asset.getId() + " media info file " + jsonPathname);
        }
        else {
        	log.error("No need to unload non-existing asset " + asset.getId() + " media info file " + jsonPathname);
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
			
			log.debug("Stored file " + originalFilename + " to " + targetPathname);
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
    	    	log.warn("Target directory/file " + targetPathname + " already exists and will be replaced.");    			
    		}    		
    		else {
    			// make sure targat path's parent dir exists
    			Files.createDirectories(tgtpath.getParent());
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
	
}
