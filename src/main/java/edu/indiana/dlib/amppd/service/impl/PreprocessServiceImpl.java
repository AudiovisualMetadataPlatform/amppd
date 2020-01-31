package edu.indiana.dlib.amppd.service.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.exception.MediaConversionException;
import edu.indiana.dlib.amppd.exception.PreprocessException;
import edu.indiana.dlib.amppd.exception.StorageFileNotFoundException;
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
import edu.indiana.dlib.amppd.service.PreprocessService;
import lombok.extern.java.Log;

/**
 * Implmentation of PreprocessService.
 * @author yingfeng
 *
 */
@Service
@Log
public class PreprocessServiceImpl implements PreprocessService {
	
	@Autowired
	private AmppdPropertyConfig amppdPropertyConfig;
	
	@Autowired
    private FileStorageService fileStorageService;

	@Autowired
	PrimaryfileRepository primaryfileRepository;

	@Autowired
	PrimaryfileSupplementRepository primaryfileSupplementRepository;
	
	@Autowired
	ItemSupplementRepository itemSupplementRepository;
	
	@Autowired
	CollectionSupplementRepository collectionSupplementRepository;
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PreprocessServiceImpl.convertFlacToWav(String)
	 */
	@Override
	public String convertFlacToWav(String sourceFilepath) {
		String extension = FilenameUtils.getExtension(sourceFilepath);
		
		if (extension == null || !extension.equalsIgnoreCase("flac")) {
			log.info("No conversion as the given source file is not in FLAC format: " + sourceFilepath);
			return null;
		}
		
		String targetFilePath = FilenameUtils.getFullPath(sourceFilepath) + FilenameUtils.getBaseName(sourceFilepath) + ".wav";
		String command = "ffmpeg -i " + fileStorageService.absolutePathName(sourceFilepath) + " " + fileStorageService.absolutePathName(targetFilePath);
		
		try {
			Process process = Runtime.getRuntime().exec(command);
		}
		catch (Exception e) {
			throw new MediaConversionException("Exception while converting " + sourceFilepath + " to " + targetFilePath, e);
		}
		
		log.info("Converted source flac file to target wav file: " + sourceFilepath + "->" + targetFilePath);
		return targetFilePath;		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PreprocessServiceImpl.convertFlac(Asset)
	 */
	@Override
	public Asset convertFlac(Asset asset) {
		String targetFilePath = convertFlacToWav(asset.getPathname());
				
		// note that we do not remove the original flac file just in case of future use
		if (targetFilePath != null) {
			asset.setPathname(targetFilePath);
			Asset updatedAsset = saveAsset(asset); 
			log.info("Updated media file path after flac->wav conversion for asset: " + asset.getId());
			return updatedAsset;		
		}

		log.info("No conversion is needed for asset: " + asset.getId());
		return asset;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PreprocessServiceImpl.retrieveMediaInfo(String)
	 */
	@Override
	public String retrieveMediaInfo(String filepath) {
		String jsonpath = FilenameUtils.getFullPath(filepath) + FilenameUtils.getBaseName(filepath) + ".json";
		String command = amppdPropertyConfig.getPythonPath() + " " + 
				amppdPropertyConfig.getMediaprobeDir() + 
				"media_probe.py --json " + fileStorageService.absolutePathName(filepath);

		ProcessBuilder pb = new ProcessBuilder(
				amppdPropertyConfig.getPythonPath(), 
				amppdPropertyConfig.getMediaprobeDir() + "media_probe.py", 
				"--json", 
				fileStorageService.absolutePathName(filepath));

		// merges the standard error to standard output
		pb.redirectErrorStream();
		
		// redirect media_probe output into json file
		// alternatively we can write the json output directly into asset, but creating the json file could be useful for other purpose
		pb.redirectOutput(new File(fileStorageService.absolutePathName(jsonpath)));				
		
		try {
			Process process = pb.start();
		    final int status = process.waitFor();
		    if (status != 0) {		    	
		    	throw new PreprocessException("Error while retrieving media info for " + filepath + ": MediaProbe exited with status " + status);
		    }
		}
		catch (IOException e) {
			// capture the error outputs info log
			log.severe(fileStorageService.readTextFile(jsonpath));
			throw new PreprocessException("Error while retrieving media info for " + filepath, e);
		}
		catch (InterruptedException e) {
			throw new PreprocessException("Error while retrieving media info for " + filepath, e);
		}	
		
		// read the json file content into a string
		String nediainfo = null;
		try {
			nediainfo = fileStorageService.readTextFile(jsonpath);
		}
		catch(StorageFileNotFoundException e) {
			throw new PreprocessException("Error while reading media info from " + jsonpath + ": the json file does not exist");
		}
		
		log.info("Retrieved media info for " + filepath + " into json file " + jsonpath);
		return nediainfo;		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PreprocessServiceImpl.retrieveMediaInfo(Asset)
	 */
	@Override	
	public Asset retrieveMediaInfo(Asset asset) {
		String mediaInfo = retrieveMediaInfo(asset.getPathname());
		if (StringUtils.isEmpty(mediaInfo)) {
			throw new PreprocessException("Error retrieving media info for Asset " + asset.getId() + ": the result is empty");
		}

		asset.setMediaInfo(mediaInfo);
		Asset updatedAsset = saveAsset(asset);
		log.info("Retrieved media info for asset: " + asset.getId());
		return updatedAsset;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PreprocessServiceImpl.preprocess(Asset)
	 */
	@Override	
	public Asset preprocess(Asset asset) {
		log.info("Preprocessing asset: " + asset.getId());
		return retrieveMediaInfo(convertFlac(asset));
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
