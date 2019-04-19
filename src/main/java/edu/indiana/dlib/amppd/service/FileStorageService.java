package edu.indiana.dlib.amppd.service;

import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primary;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.Unit;

/**
 * Service for storing/retrieving files, including primary files, supplement files, as well as intermediate files.
 * @author yingfeng
 *
 */
public interface FileStorageService {

	/**
	 * Stores the specified sourceFile to the specified targetPathname on the file system.
	 * @param sourceFile the specified source File
	 * @param targetPathname the specified target file path name relative to the storage root
	 */
	public void store(MultipartFile sourceFile, String targetPathname);
	
	/**
	 * Returns the target storage directory path name relative to the storage root for the specified unit.
	 * @param unit the specified unit
	 * @return the target storage directory path name
	 */
	public String getDirPathName(Unit unit);

	/**
	 * Returns the target storage directory path name relative to the storage root for the specified collection.
	 * @param collection the specified collection
	 * @return the target storage directory path name
	 */
	public String getDirPathName(Collection collection);

	/**
	 * Returns the target storage directory path name relative to the storage root for the specified item.
	 * @param item the specified item
	 * @return the target storage directory path name
	 */
	public String getDirPathName(Item item);

	/**
	 * Returns the target storage directory path name relative to the storage root for the specified primary.
	 * @param primary the specified primary
	 * @return the target storage directory path name
	 */
	public String getDirPathName(Primary primary);

	/**
	 * Returns the target storage file path name relative to the storage root for the specified primary.
	 * @param primary the specified primary
	 * @return the target storage file path
	 */
	public String getFilePathName(Primary primary);

	/**
	 * Returns the target storage file path name relative to the storage root for the specified supplement.
	 * @param supplement the specified supplement
	 * @return the target storage file path
	 */
	public String getFilePathName(Supplement supplement);


//	public Path load(String path);
//
//	public Resource loadAsResource(String path);
//
//	public void delete(String path);
	
}
