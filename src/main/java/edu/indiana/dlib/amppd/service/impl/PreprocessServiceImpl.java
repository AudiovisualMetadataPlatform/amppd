package edu.indiana.dlib.amppd.service.impl;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.exception.MediaConversionException;
import edu.indiana.dlib.amppd.model.Primaryfile;
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

	public String convertFlacToWav(String sourceFilepath) throws MediaConversionException {
		String extension = FilenameUtils.getExtension(sourceFilepath);
		
		if (extension == null || !extension.equalsIgnoreCase("flac")) {
			log.info("No conversion as the given source file is not in FLAC format: " + sourceFilepath);
			return null;
		}
		
		String targetFilePath = FilenameUtils.getFullPath(sourceFilepath) + FilenameUtils.getBaseName(sourceFilepath) + ".wav";
		String command = "ffmpeg -i " + fileStorageService.absolutePathName(sourceFilepath) + " " + fileStorageService.absolutePathName(targetFilePath);
//		String command = "cp " + fileStorageService.absolutePathName(sourceFilepath) + " " + fileStorageService.absolutePathName(targetFilePath);
		
		try {
			Process process = Runtime.getRuntime().exec(command);
		}
		catch (Exception e) {
			throw new MediaConversionException("Exception while converting " + sourceFilepath + " to " + targetFilePath, e);
		}
		
		log.info("Converted source flac file to target wav file: " + sourceFilepath + "->" + targetFilePath);
		return targetFilePath;		
	}
	
	public boolean convertFlac(Primaryfile primaryfile) throws MediaConversionException {
		String targetFilePath = convertFlacToWav(primaryfile.getPathname());
		if (targetFilePath != null) {
			primaryfile.setPathname(targetFilePath);
			log.info("Updated media file path after flac->wav conversion for primaryfile: " + primaryfile.getId());
			return true;
		}
		return false;
	}
	
}
