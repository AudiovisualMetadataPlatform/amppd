package edu.indiana.dlib.amppd.service;

/**
 * Service for pre-processing media files upon ingestion.
 * @author yingfeng
 *
 */
public interface PreprocessService {

	/**
	 * If the given file is of flac format then convert it to wav format.
	 * @param sourceFilepath filepath of the given source  file
	 * @return filepath of the generated target wav file if conversion happened; otherwise null
	 */
	public String convertFlacToWav(String sourceFilepath);
	
}
