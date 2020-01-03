package edu.indiana.dlib.amppd.service.impl;

import org.apache.commons.io.FilenameUtils;

import edu.indiana.dlib.amppd.exception.MediaConversionException;
import edu.indiana.dlib.amppd.exception.PreprocessException;

/**
 * Implmentation of PreprocessService.
 * @author yingfeng
 *
 */
public class PreprocessServiceImpl {

	public String convertFlacToWav(String sourceFilepath) throws MediaConversionException {
		String extension = FilenameUtils.getExtension(sourceFilepath);
		
		if (extension != null && extension.equalsIgnoreCase("flac")) {
			throw new PreprocessException("The given source file is not in FLAC format: " + sourceFilepath);
		}
		
		String targetFilePath = FilenameUtils.getFullPath(sourceFilepath) + FilenameUtils.getBaseName(sourceFilepath) + ".wav";
		String command = "ffmpeg -i " + sourceFilepath + " " + targetFilePath;
		
		try {
			Process process = Runtime.getRuntime().exec(command);
		}
		catch (Exception e) {
			throw new MediaConversionException("Exception while converting " + sourceFilepath + " to " + targetFilePath, e);
		}
		
		return targetFilePath;		
	}
	
}
