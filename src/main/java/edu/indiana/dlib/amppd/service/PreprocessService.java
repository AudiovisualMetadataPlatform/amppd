package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.Asset;

/**
 * Service for pre-processing media files upon ingestion.
 * @author yingfeng
 *
 */
public interface PreprocessService {

	/**
	 * If the given file is of flac format then convert it to wav format.
	 * @param sourceFilepath path of the given source file relative to the root
	 * @return relative path of the generated wav file, if conversion happened; otherwise null
	 */
	public String convertFlacToWav(String sourceFilepath);
	
	/**
	 * Converts flac file to wav file and update the pathname if the given asset points to a flac file.
	 * @param asset the given asset
	 * @return true if conversion happened; false otherwise
	 */
	public boolean convertFlac(Asset asset);
	
	/**
	 * Retrieves media info of the given media file.
	 * @@param filepath path relative to the root of the given media file
	 * @return relative path of the json output file containing the retrieved media info
	 */
	public String retrieveMediaInfo(String filepath);

	/**
	 * Retrieves media info for the given asset.
	 * @@param filepath path relative to the root of the given media file
	 * @return relative path of the json output file containing the retrieved media info
	 */
	public boolean retrieveMediaInfo(Asset asset);
	

}
