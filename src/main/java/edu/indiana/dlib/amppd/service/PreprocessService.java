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
	 * @param sourceFilepath path of the given source file relative to its root
	 * @return relative path of the generated wav file, if conversion happened; otherwise null
	 */
	public String convertFlacToWav(String sourceFilepath);
	
	/**
	 * Convert flac file to wav file and update the pathname if the given asset points to a flac file.
	 * @param asset the given asset
	 * @return the updated asset if conversion happened; otherwise the original asset
	 */
	public Asset convertFlac(Asset asset);
	
	/**
	 * Retrieve media info of the given media file.
	 * @@param filepath path relative to the root of the given media file
	 * @return content of the json output file containing the retrieved media info
	 */
	public String retrieveMediaInfo(String filepath);

	/**
	 * Retrieve media info and update the field of the given asset.
	 * @@asset the given asset
	 * @return the updated asset containing the retrieved media info
	 */
	public Asset retrieveMediaInfo(Asset asset);
	
	/**
	 * Pre-process the given asset by converting flac to wav and retrieving media info, and persist the updated asset if indicated.
	 * This method should be called after an asset is ingested/uploaded, 
	 * and the caller is responsible for persisting the asset later if the indicator is set to false.
	 * @param asset the given asset
	 * @param persist if true, save the updated asset to DB; otherwise, do not save yet 
	 * @return the pre-processed asset
	 */
	public Asset preprocess(Asset asset, boolean persist);

	/**
	 * Get the media info json file pathname for the given media file
	 * @param mediaPathname pathname of the given media file
	 * @return the media info json file pathname
	 */
	public String getMediaInfoJsonPath(String mediaPathname);	

}
