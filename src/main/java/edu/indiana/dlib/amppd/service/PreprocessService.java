package edu.indiana.dlib.amppd.service;

/**
 * Service for pre-processing media files upon ingestion.
 * @author yingfeng
 *
 */
public interface PreprocessService {

	/**
	 * Converts the given file from flac format to wav format
	 * @param sourceFilepath of the given source flac file
	 * @return tfilepath of the generated target wav file
	 */
	public String convertFlacToWav(String sourceFilepath);
	
}
