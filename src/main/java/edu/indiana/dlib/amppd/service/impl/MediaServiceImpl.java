package edu.indiana.dlib.amppd.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.config.AmppdUiPropertyConfig;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.repository.UnitSupplementRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.MediaService;
import edu.indiana.dlib.amppd.web.ItemInfo;
import edu.indiana.dlib.amppd.web.ItemSearchResponse;
import edu.indiana.dlib.amppd.web.PrimaryfileInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of MediaService.
 * @author yingfeng
 */
@Service
@Slf4j
public class MediaServiceImpl implements MediaService {

	public static int SYMLINK_LENGTH = 16;

	// AMP extended Galaxy data types that need extension converted to standard media types viewable by browsers    
	// NOTE: .vtt is a standard format browsers can handle, no need to translate to .txt
//	public static List<String> TYPE_TXT = Arrays.asList(new String[] {"vtt"});
	public static List<String> TYPE_JSON = Arrays.asList(new String[] {"segments, segment", "transcript", "ner", "vocr", "shot", "face"});
	public static List<String> TYPE_AUDIO = Arrays.asList(new String[] {"audio", "speech", "music"});
	public static List<String> TYPE_VIDEO = Arrays.asList(new String[] {"video"});
	// NOTE: segments is deprecated in Galaxy and replaced by segment; we still handle it here for the existing legacy datasets of this type.

	// corresponding standard media types to convert to for workflow output symlinks
	public static String FILE_EXT_TXT = "txt";
	public static String FILE_EXT_JSON = "json";
	public static String FILE_EXT_AUDIO = "wav";
	public static String FILE_EXT_VIDEO = "mp4";

	@Autowired
	private PrimaryfileRepository primaryfileRepository;

	@Autowired
	private PrimaryfileSupplementRepository primaryfileSupplementRepository;

	@Autowired
	private ItemSupplementRepository itemSupplementRepository;

	@Autowired
	private CollectionSupplementRepository collectionSupplementRepository;

	@Autowired
	private UnitSupplementRepository unitSupplementRepository;

	@Autowired
	private WorkflowResultRepository workflowResultRepository;

	@Autowired
	private FileStorageService fileStorageService;
	
	@Autowired
	private DataentityService dataentityService;
	
	@Autowired
	private AmppdPropertyConfig amppdPropertyConfig;
	
	@Autowired
	private AmppdUiPropertyConfig amppduiConfig; 	
	
	// absolute path of the root directory for symlinks
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
	 * @see eedu.indiana.dlib.amppd.service.MediaService.isMediaTypeAV(String)
	 */
	@Override
	public boolean isMediaTypeAV(String mediaType) {
		return StringUtils.isBlank(mediaType) || "av".equalsIgnoreCase(mediaType) || "audio/video".equalsIgnoreCase(mediaType);
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.isMediaTypeMatched(String, String)
	 */
	@Override
	public boolean isMediaTypeMatched(String mimeType, String mediaType) {
		return isMediaTypeAV(mediaType) || StringUtils.containsIgnoreCase(mimeType, mediaType);
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.isMediaTypeMatched(Primaryfile, String)
	 */
	@Override
	public boolean isMediaTypeMatched(Primaryfile primaryfile, String mediaType) {
		String type = primaryfile.getMimeType();
		return isMediaTypeAV(mediaType) || StringUtils.containsIgnoreCase(type, mediaType);
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.getPrimaryfileMediaUrl(Long)
	 */
	@Override
	public String getPrimaryfileMediaUrl(Long primaryfileId) {		
		String url = amppdPropertyConfig.getUrl() + "/primaryfiles/" + primaryfileId + "/media";
		return url;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.getPrimaryfileMediaUrl(Primaryfile)
	 */
	@Override
	public String getPrimaryfileMediaUrl(Primaryfile primaryfile) {
		return getPrimaryfileMediaUrl(primaryfile.getId());
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.getSupplementPath(Primaryfile, String, SupplementType)
	 */
	@Override
	public String getSupplementPath(Primaryfile primaryfile, String name, SupplementType type) {
		// validate passed in parameters
		if (primaryfile == null || name == null || type == null) {
			return null;
		}
		
		String pathname = null;
		Supplement supplement = null;
		List<? extends Supplement> supplements = null;
		
		// find the supplements by its name and associated parent's ID
		switch(type) {
		case UNIT:
			supplements = unitSupplementRepository.findByUnitIdAndName(primaryfile.getItem().getCollection().getUnit().getId(), name);
			break;
		case COLLECTION:
			supplements = collectionSupplementRepository.findByCollectionIdAndName(primaryfile.getItem().getCollection().getId(), name);
			break;
		case ITEM:
			supplements = itemSupplementRepository.findByItemIdAndName(primaryfile.getItem().getId(), name);
			break;
		case PRIMARYFILE:
			supplements = primaryfileSupplementRepository.findByPrimaryfileIdAndName(primaryfile.getId(), name);
			break;
		}		
		
		// there should be exactly one supplement found, as supplement is unique by name within its parent's scope
		// in this case, resolve its pathname to the absolute path; otherwise pathname will be null
		if (supplements != null && supplements.size() == 1) {
			supplement = supplements.get(0);
			pathname = fileStorageService.resolve(supplement.getPathname()).toString();
		}	
		
		return pathname;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.getAssetMediaInfoPath(Asset)
	 */
	@Override
	public String getAssetMediaInfoPath(Asset asset) {
		String jsonpath = FilenameUtils.getFullPath(asset.getPathname()) + FilenameUtils.getBaseName(asset.getPathname()) + ".json";
		jsonpath = fileStorageService.absolutePathName(jsonpath);
		return jsonpath;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.setAssetAbsoluatePath(Asset)
	 */
	@Override
	public String setAssetAbsoluatePath(Asset asset) {
		String pathname = fileStorageService.resolve(asset.getPathname()).toString();
		asset.setAbsolutePathname(pathname);
		return pathname;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.getWorkflowResultOutputUrl(Long)
	 */
	@Override
	public String getWorkflowResultOutputUrl(Long workflowResultId) {
		String url = amppdPropertyConfig.getUrl() + "/workflow-iteminfos/" + workflowResultId + "/output";
		return url;
	}
	
	/*
	 * @see edu.indiana.dlib.amppd.service.MediaService.getWorkflowResultOutputExtension(WorkflowResult)
	 */
	public String getWorkflowResultOutputExtension(WorkflowResult workflowResult) {
		String extension = workflowResult.getOutputType();
		
		// We make the following assumptions based on current Galaxy tool output data types and file types:
		// all audio/music/speech outputs are of wav format
		// all video outputs are of mp4 format
//		if (TYPE_TXT.contains(extension)) {
//			return FILE_EXT_TXT;				
//		}
		if (TYPE_JSON.contains(extension)) {
			return FILE_EXT_JSON;				
		}
		if (TYPE_AUDIO.contains(extension)) {
			return FILE_EXT_AUDIO;				
		}
		if (TYPE_VIDEO.contains(extension)) {
			return FILE_EXT_VIDEO;				
		}
		
		// TODO if more data types are added with extensions that need standardization, 
		// logic should be added here to handle those
		
		// for data types already with standard extension (such as csv, pdf, png), just return as is
		return extension;				
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
	 * @see edu.indiana.dlib.amppd.service.MediaService.getWorkflowResultOutputSymlinkUrl(Long)
	 */
	@Override
	public String getWorkflowResultOutputSymlinkUrl(Long id) {
		WorkflowResult workflowResult = workflowResultRepository.findById(id).orElseThrow(() -> new StorageException("workflowResultId <" + id + "> does not exist!"));   
		String serverUrl = StringUtils.removeEnd(amppduiConfig.getUrl(), "/#"); // exclude /# for static contents
		String url = serverUrl + "/" + amppduiConfig.getSymlinkDir() + "/" + createSymlink(workflowResult);
		log.info("Output symlink URL for workflowResult <" + id + "> is: " + url);
		return url;
	}
		
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.resetSymlink(Asset, boolean)
	 */
	@Override
	public String resetSymlink(Asset asset, boolean persist) {
		// make sure the asset exists
		if (asset == null) {
			throw new RuntimeException("The given asset for resetting symlink is null.");
		}
		
		// delete the symlink if exists
		String symlink = asset.getSymlink();
		if (StringUtils.isNotEmpty(symlink)) {
			delete(symlink);
		}
		
		// reset asset symlink and save it if indicated		
		asset.setSymlink(null);
		if (persist) {
			asset = dataentityService.saveAsset(asset); 
		} 
		
		log.info("Successfully reset symlink " + symlink + " for asset " + asset.getId());
		return symlink;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.createSymlink(Asset)
	 */
	@Override
	@Transactional
	public String createSymlink(Asset asset) {
		// make sure the asset exists and its pathname populated
		if (asset == null) {
			throw new RuntimeException("The given asset for creating symlink is null.");
		}
		if (asset.getPathname() == null ) {
			throw new StorageException("Can't create symlink for asset " + asset.getId() + ": its media file hasn't been uploaded.");
		}

		// make sure the asset's media file exists
		Path path = fileStorageService.resolve(asset.getPathname());
		if (!Files.exists(path)) {
			throw new StorageException("Can't create symlink for asset " + asset.getId() + ": its media file " + path + " doesn't exist");	
		}

		// if symlink was created and exists, reuse it
		String symlink = asset.getSymlink();
		if (!StringUtils.isEmpty(symlink) && Files.exists(resolve(symlink))) {
			log.info("Symlink for asset " + asset.getId() + " already exists, will reuse it");
			return symlink;
		}
		// otherwise, create a new one
		
		// use a random string to obscure the symlink for security
		// prefix A stands for Asset
		// include asset ID to rule out any chance of name collision
		// add file extension to help browser decide file type so to use proper display app
		String fileExt = FilenameUtils.getExtension(asset.getPathname());
		symlink = "A-" + asset.getId() + "-" + RandomStringUtils.random(SYMLINK_LENGTH, true, true) + "." + fileExt;			    
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
		dataentityService.saveAsset(asset);

		log.info("Successfully created symlink " + symlink + " for asset " + asset.getId());
		return symlink;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.createSymlink(workflowResult)
	 */
	@Override
	@Transactional
	public String createSymlink(WorkflowResult workflowResult) {
		// make sure the workflowResult exists and its outputPath populated
		if (workflowResult == null) {
			throw new RuntimeException("The given workflowResult for creating symlink is null.");
		}
		if (workflowResult.getOutputPath() == null ) {
			throw new StorageException("Can't create output symlink for workflowResult " + workflowResult.getId() + ": its output file path is null.");
		}

		// make sure the output file exists
		Path path = Paths.get(workflowResult.getOutputPath());
		if (!Files.exists(path)) {
			throw new StorageException("Can't create output symlink for workflowResult " + workflowResult.getId() + ": its output file " + path + " doesn't exist");	
		}

		// if symlink was created and exists, reuse it
		String symlink = workflowResult.getOutputLink();
		if ( symlink != null && Files.exists(resolve(symlink))) {
			log.info("Output symlink for workflowResult " + workflowResult.getId() + " already exists, will reuse it");
			return workflowResult.getOutputLink();
		}
		// otherwise, create a new one

		// use a random string to obscure the symlink for security
		// prefix O stands for Output
		// include workflowResult ID to rule out any chance of name collision
		// add file extension to help browser decide file type so to use proper display app 
		symlink = "O-" + workflowResult.getId() + "-" + RandomStringUtils.random(SYMLINK_LENGTH, true, true) + "." + getWorkflowResultOutputExtension(workflowResult);
		Path link = resolve(symlink);

		// create the symbolic link for the output file using the random string
		try {
			Files.createSymbolicLink(link, path);
		}
		catch (IOException e) {
			throw new StorageException("Error creating output symlink for workflowResult " + workflowResult.getId(), e);		    	
		}

		// save the symlink into workflowResult
		workflowResult.setOutputLink(symlink);
		workflowResultRepository.save(workflowResult);

		log.info("Successfully created output symlink " + symlink + " for workflowResult " + workflowResult.getId());
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
    		boolean deleted = FileSystemUtils.deleteRecursively(path);
    		if (deleted) {
    			log.debug("Successfully deleted directory/file " + symlink);
    		}
    		else {
    			log.warn("Failed to delete non-ecisting directory/file " + symlink);
    		}
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
    		log.debug("Successfully deleted all directories/files under media symlink root.");
    	}
    	catch (IOException e) {
    		throw new StorageException("Could not delete all directories/files under media symlink root.");
    	}  	
    }
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.findItemOrFile(String, String)
	 */
	@Override
	public ItemSearchResponse findItemOrFile(String keyword, String mediaType) {
		ItemSearchResponse response = new ItemSearchResponse();
		List<ItemInfo> iteminfos = new ArrayList<ItemInfo>();

		try {
			List<Primaryfile> matchedFiles = primaryfileRepository.findActiveByCollectionOrItemOrFileName(keyword);
			ItemInfo iteminfo = new ItemInfo();
			List<PrimaryfileInfo> primaryfileinfos = new ArrayList<PrimaryfileInfo>();
			long curr_item_id = 0;
			for(Primaryfile p : matchedFiles) {
				//reset if the current item is a new entry
				if(p.getItem().getId() != curr_item_id && primaryfileinfos.size()>0) {
					log.trace("Now new item id:"+p.getItem().getId()+" curr item id:"+curr_item_id);
					iteminfo.setPrimaryfiles(primaryfileinfos);
					iteminfos.add(iteminfo);
					iteminfo = new ItemInfo();
					primaryfileinfos = new ArrayList<PrimaryfileInfo>();					
				}
				String mime_type = p.getMimeType();
				curr_item_id = p.getItem().getId();
				iteminfo.setCollectionId(p.getItem().getCollection().getId());
				iteminfo.setCollectionName(p.getItem().getCollection().getName());
				iteminfo.setItemId(p.getItem().getId());
				iteminfo.setItemName(p.getItem().getName());
				iteminfo.setExternalSource(p.getItem().getExternalSource());
				iteminfo.setExternalId(p.getItem().getExternalId());
				PrimaryfileInfo primaryfileinfo = new PrimaryfileInfo(p.getId(), p.getName(), mime_type, p.getOriginalFilename());
				primaryfileinfos.add(primaryfileinfo);
			}
			//add the last item to the iteminfos
			if(primaryfileinfos.size()>0) {
				iteminfo.setPrimaryfiles(primaryfileinfos);
				iteminfos.add(iteminfo);
				response.setItems(iteminfos);
			}
			else {
				response.setError("No primary file found");
			}
			response.setSuccess(true);
			log.info("Successfully found " + iteminfos.size() + " items containing primaryfiles: keywowrd = " + keyword + ", mediaType = " + mediaType);			
		} catch (Exception e) {
			response.setError(e.getMessage());
			response.setSuccess(false);
			log.error("Error searching for items/primaryfiles: keywowrd = " + keyword + ", mediaType = " + mediaType, e);			
		}
		return response;
	}	

}
