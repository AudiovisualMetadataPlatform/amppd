package edu.indiana.dlib.amppd.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import edu.indiana.dlib.amppd.config.AmppdUiPropertyConfig;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.MediaService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of MediaService.
 * @author yingfeng
 *
 */
@Service
@Slf4j
public class MediaServiceImpl implements MediaService {

	public static int SYMLINK_LENGTH = 16;

	@Autowired
	private PrimaryfileRepository primaryfileRepository;

	@Autowired
	PrimaryfileSupplementRepository primaryfileSupplementRepository;

	@Autowired
	ItemSupplementRepository itemSupplementRepository;

	@Autowired
	CollectionSupplementRepository collectionSupplementRepository;

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private AmppdUiPropertyConfig amppduiConfig; 	
	
	private Path root;

	@Autowired
	public MediaServiceImpl(AmppdUiPropertyConfig amppduiConfig) {
		// initialize Amppd UI Apache server media root folder 
		try {
			root = Paths.get(amppduiConfig.getDocumentRoot(), amppduiConfig.getSymlinkDir());
			Files.createDirectories(root);	// creates root directory if not already exists
			log.info("Media symlink root directory " + root + " has been created." );
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize media symlink root directory " + root, e);
		}		
	}	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.getPrimaryfileSymlink(Long)
	 */
	@Override
	public String getPrimaryfileSymlinkUrl(Long id) {
		Primaryfile primaryfile = primaryfileRepository.findById(id).orElseThrow(() -> new StorageException("Primaryfile <" + id + "> does not exist!"));   
		String serverUrl = StringUtils.removeEnd(amppduiConfig.getUrl(), "/#"); // exclude /# for static contents
		String url = serverUrl + "/" + amppduiConfig.getSymlinkDir() + "/" + createSymlink(primaryfile);
		log.info("Media symlink URL for primaryfile <" + id + "> is: " + url);
		return url;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.createSymlink(Asset)
	 */
	@Override
	public String createSymlink(Asset asset) {
		if (asset == null) {
			throw new RuntimeException("The given asset for creating symlink is null.");
		}
		if (asset.getPathname() == null ) {
			throw new StorageException("Can't create symlink for asset " + asset.getId() + ": its media file hasn't been uploaded.");
		}

		// if symlink hasn't been created, create it
		if (asset.getSymlink() != null ) {
			log.info("Symlink for asset " + asset.getId() + " already exists, will reuse it");
			return asset.getSymlink();
		}

		// use a random string to obscure the symlink for security
		// TODO do we want to include asset ID to rule out any chance of name collision
		String symlink = asset.getId() + "-" + RandomStringUtils.random(SYMLINK_LENGTH, true, true);			    
		Path path = fileStorageService.resolve(asset.getPathname());
		Path link = resolve(symlink);

		// create the symbolic link for the original media file using the random string
		try {
			Files.createSymbolicLink(link, path);
		}
		catch (IOException e) {
			throw new StorageException("Error creating symlink for asset " + asset.getId(), e);		    	
		}

		// save the symlink into asset
		asset.setSymlink(symlink);
		saveAsset(asset);

		log.info("Successfully created symlink " + symlink + " for asset " + asset.getId());
		return symlink;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.resolve(String)
	 */
	@Override
	public Path resolve(String pathname) {
		return root.resolve(pathname);
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.delete(String)
	 */    
	@Override
    public void delete(String symlink) {
    	try {
    		Path path = resolve(symlink);
    		FileSystemUtils.deleteRecursively(path);
    		log.info("Successfully deleted directory/file " + symlink);
    	}
    	catch (IOException e) {
    		throw new StorageException("Could not delete file " + symlink, e);
    	}
    }
    
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.cleanup()
	 */
	@Override
    public void cleanup() {
    	try {
    		FileUtils.cleanDirectory(new File(root.toString()));
    		log.info("Successfully deleted all directories/files under media symlink root.");
    	}
    	catch (IOException e) {
    		throw new StorageException("Could not delete all directories/files under media symlink root.");
    	}  	
    }
	
	/**
	 * Saves the given asset to DB.
	 * @param asset the given asset
	 */
	private Asset saveAsset(Asset asset) {
		if (asset instanceof Primaryfile) {
			return primaryfileRepository.save((Primaryfile)asset);
		}
		else if (asset instanceof PrimaryfileSupplement) {
			return primaryfileSupplementRepository.save((PrimaryfileSupplement)asset);
		}
		else if (asset instanceof ItemSupplement) {
			return itemSupplementRepository.save((ItemSupplement)asset);
		}
		else if (asset instanceof CollectionSupplement) {
			return collectionSupplementRepository.save((CollectionSupplement)asset);
		}
		return asset;
	}


}
