package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.Primaryfile;

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
	
	/**
	 * Converts flac file to wav file and update the pathname if the given primaryfile points to a flac file 
	 * @param primaryfile the given primaryfile
	 * @return true if conversion happened; false otherwise
	 */
	public boolean convertFlac(Primaryfile primaryfile);
}
