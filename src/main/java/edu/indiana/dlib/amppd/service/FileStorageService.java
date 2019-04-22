package edu.indiana.dlib.amppd.service;

import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.Unit;

/**
 * Service for storing/retrieving files, including primaryfiles, supplements, as well as intermediate files.
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
	 * Loads the file with the specified pathname.
	 * @param pathname
	 * @return path of the loaded file
	 */
	public Path load(String pathname);
	
	/**
	 * Loads the file with the specified pathname as a resource
	 * @param pathname
	 * @return resource loaded
	 */
	public Resource loadAsResource(String pathname);
	
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
	 * Returns the target storage directory path name relative to the storage root for the specified primaryfile.
	 * @param primaryfile the specified primaryfile
	 * @return the target storage directory path name
	 */
	public String getDirPathName(Primaryfile primaryfile);

	/**
	 * Returns the target storage file path name relative to the storage root for the specified primaryfile.
	 * @param primaryfile the specified primaryfile
	 * @return the target storage file path
	 */
	public String getFilePathName(Primaryfile primaryfile);

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
