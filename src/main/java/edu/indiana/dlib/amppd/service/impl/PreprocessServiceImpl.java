package edu.indiana.dlib.amppd.service.impl;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.exception.MediaConversionException;
import edu.indiana.dlib.amppd.model.Asset;
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
    private FileStorageService fileStorageService;

	
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
//		String command = "copy " + fileStorageService.absolutePathName(sourceFilepath) + " " + fileStorageService.absolutePathName(targetFilePath);
		
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
	public boolean convertFlac(Asset asset) {
		String targetFilePath = convertFlacToWav(asset.getPathname());
		if (targetFilePath != null) {
			asset.setPathname(targetFilePath);
			log.info("Updated media file path after flac->wav conversion for primaryfile: " + asset.getId());
			// note that we do not remove the original flac file just in case of future use
			return true;
		}
		return false;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PreprocessServiceImpl.retrieveMediaInfo(String)
	 */
	@Override
	public String retrieveMediaInfo(String filepath) {
		String command = "python3 media_probe.py --json " + fileStorageService.absolutePathName(filepath);
		
		try {
			Process process = Runtime.getRuntime().exec(command);
		}
		catch (Exception e) {
			throw new MediaConversionException("Exception while retrieving media info for " + filepath, e);
		}
		
		log.info("Retrieved media info for " + filepath);
		return filepath;		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PreprocessServiceImpl.retrieveMediaInfo(Asset)
	 */
	@Override	
	public boolean retrieveMediaInfo(Asset asset) {
		return true;
	}

	
}
