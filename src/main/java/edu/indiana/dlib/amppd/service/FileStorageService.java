package edu.indiana.dlib.amppd.service;

import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.github.jmchilton.blend4j.galaxy.beans.Library;
import com.sun.jersey.api.client.ClientResponse;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.Unit;

/**
 * Service for storing/retrieving files, including primaryfiles, supplements associated with collections/items/primaryfiles, as well as intermediate files.
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
	 * @param pathname the specified pathname
	 * @return path of the loaded file
	 */
	public Path load(String pathname);
	
	/**
	 * Loads the file with the specified pathname as a resource.
	 * @param pathname the specified pathname
	 * @return resource loaded
	 */
	public Resource loadAsResource(String pathname);
	
	/**
	 * Deletes the file with the specified pathname.
	 * @param pathname the specified pathname
	 */
	public void delete(String pathname);
	
	/**
	 * Deletes all directfiles under the file storage root.
	 */
	public void deleteAll();
	
	/**
	 * Returns the target storage directory path name relative to the storage root for the specified unit.
	 * @param unit the specified unit
	 * @return the target storage directory path name
	 */
	public String getDirPathname(Unit unit);

	/**
	 * Returns the target storage directory path name relative to the storage root for the specified collection.
	 * @param collection the specified collection
	 * @return the target storage directory path name
	 */
	public String getDirPathname(Collection collection);

	/**
	 * Returns the target storage directory path name relative to the storage root for the specified item.
	 * @param item the specified item
	 * @return the target storage directory path name
	 */
	public String getDirPathname(Item item);

	/**
	 * Returns the target storage directory path name relative to the storage root for the specified primaryfile.
	 * @param primaryfile the specified primaryfile
	 * @return the target storage directory path name
	 */
	public String getDirPathname(Primaryfile primaryfile);

	/**
	 * Returns the target storage file path name relative to the storage root for the specified primaryfile with its originalFilename populated.
	 * @param primaryfile the specified primaryfile
	 * @return the target storage file path name
	 */
	public String getFilePathname(Primaryfile primaryfile);

	/**
	 * Returns the target storage file path name relative to the storage root for the specified supplement with its originalFilename populated.
	 * @param supplement the specified supplement
	 * @return the target storage file path name
	 */
	public String getFilePathname(Supplement supplement);

//	/**
//	 * Upload a file/folder from AMP file system to Galaxy data library without copying the physical file. 
//	 * 
//	 */
//	public ClientResponse uploadFileToGalaxy(String filePath, String lib_name);
	
}
