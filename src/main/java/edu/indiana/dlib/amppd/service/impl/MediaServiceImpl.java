package edu.indiana.dlib.amppd.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.config.AmppdUiPropertyConfig;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.MediaService;
import edu.indiana.dlib.amppd.web.ItemSearchResponse;
import edu.indiana.dlib.amppd.web.ItemSearchResult;
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
	PrimaryfileSupplementRepository primaryfileSupplementRepository;

	@Autowired
	ItemSupplementRepository itemSupplementRepository;

	@Autowired
	CollectionSupplementRepository collectionSupplementRepository;

	@Autowired
	private WorkflowResultRepository workflowResultRepository;

	@Autowired
	private FileStorageService fileStorageService;
	
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
	 * @see edu.indiana.dlib.amppd.service.MediaService.getCollectionSupplementPathname(Primaryfile, String, SupplementType)
	 */
	@Override
	public String getSupplementPathname(Primaryfile primaryfile, String name, SupplementType type) {
		// validate passed in parameters
		if (primaryfile == null || name == null || type == null) {
			return null;
		}
		
		String pathname = null;
		Supplement supplement = null;
		List<? extends Supplement> supplements = null;
		
		// find the supplements by its name and associated parent's ID
		switch(type) {
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
	 * @see edu.indiana.dlib.amppd.service.MediaService.getPrimaryfileMediaUrl(Primaryfile)
	 */
	@Override
	public String getPrimaryfileMediaUrl(Primaryfile primaryfile) {
		String url = amppdPropertyConfig.getUrl() + "/primaryfiles/" + primaryfile.getId() + "/media";
		return url;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.getWorkflowResultOutputUrl(Long)
	 */
	@Override
	public String getWorkflowResultOutputUrl(Long workflowResultId) {
		String url = amppdPropertyConfig.getUrl() + "/workflow-results/" + workflowResultId + "/output";
		return url;
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
		if ( symlink != null && Files.exists(resolve(symlink))) {
			log.info("Symlink for asset " + asset.getId() + " already exists, will reuse it");
			return asset.getSymlink();
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
		saveAsset(asset);

		log.info("Successfully created symlink " + symlink + " for asset " + asset.getId());
		return symlink;
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
	 * @see edu.indiana.dlib.amppd.service.MediaService.createSymlink(workflowResult)
	 */
	@Override
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
		if ( symlink != null  && Files.exists(resolve(symlink))) {
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
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MediaService.findItemOrFile(String, String)
	 */
	@Override
	public ItemSearchResponse findItemOrFile(String keyword, String mediaType) {
		ItemSearchResponse response = new ItemSearchResponse();
		ArrayList<ItemSearchResult> rows = new ArrayList<ItemSearchResult>();
		
		try {
			List<Primaryfile> matchedFiles = primaryfileRepository.findByItemOrFileName(keyword);
			ItemSearchResult result = new ItemSearchResult();;
			Map <String, Object>primaryFileinfo;
			ArrayList<Map> primaryFilerows = new ArrayList<Map>();
			long curr_item_id = 0;
			for(Primaryfile p : matchedFiles) {
				//reset if the current item is a new entry
				primaryFileinfo = new HashMap<String, Object>();
				if(p.getItem().getId() != curr_item_id && primaryFilerows.size()>0) {
					log.trace("Now new item id:"+p.getItem().getId()+" curr item id:"+curr_item_id);
					result.setPrimaryFiles(primaryFilerows);
					rows.add(result);
					result = new ItemSearchResult();
					primaryFilerows = new ArrayList<Map>();
					
				}
				String mime_type = getMediaTypeFromJson(p);
				if(mime_type!=null && !mediaType.contentEquals("000"))
				{
					if((mime_type.contains("audio") && mediaType.substring(0, 1).contentEquals("1")) 
							|| (mime_type.contains("video") && mediaType.substring(1, 2).contentEquals("1")) 
							|| (!mime_type.contains("video") && !mime_type.contains("audio") && mediaType.contentEquals("001"))){
						curr_item_id = p.getItem().getId();
						result.setItemName(p.getItem().getName());
						primaryFileinfo.put("id", p.getId()); 
						primaryFileinfo.put("name",p.getName());
						primaryFileinfo.put("mediaType",mime_type);
						primaryFilerows.add(primaryFileinfo);
					}
				}
				else {
					curr_item_id = p.getItem().getId();
					result.setItemName(p.getItem().getName());
					primaryFileinfo.put("id", p.getId()); 
					primaryFileinfo.put("name",p.getName());
					primaryFileinfo.put("mediaType",mime_type);
					primaryFilerows.add(primaryFileinfo);
				}
			}
			//add the last item to the rows
			if(primaryFilerows.size()>0) {
				result.setPrimaryFiles(primaryFilerows);
				rows.add(result);
				response.setRows(rows);
			}
			else {
				response.setError("No primary file found");
			}
			response.setSuccess(true);
			log.info("Successfully found " + rows.size() + " items containing primaryfiles: keywowrd = " + keyword + ", mediaType = " + mediaType);			
		} catch (Exception e) {
			response.setError(e.getMessage());
			response.setSuccess(false);
			log.error("Error searching for items/primaryfiles: keywowrd = " + keyword + ", mediaType = " + mediaType, e);			
		}
		return response;
	}
	
	protected String getMediaTypeFromJson(Primaryfile p) {
		String mime_type = new String();
		String media_type = p.getMediaInfo();
		try {
			if(media_type!=null) {
				JSONObject jsonObject = new JSONObject(media_type);
				JSONObject jsonObject_container = new JSONObject(jsonObject.getString("container"));
				mime_type = jsonObject_container.getString("mime_type");
				log.trace("====>>>>>MIME TYPE IS:"+jsonObject_container.getString("mime_type"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mime_type;
	}

}
