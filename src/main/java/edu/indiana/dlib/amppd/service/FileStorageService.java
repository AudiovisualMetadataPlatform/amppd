package edu.indiana.dlib.amppd.service;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.Unit;

/**
 * Service for storing/retrieving files, including primaryfiles, supplements associated with collections/items/primaryfiles, as well as intermediate files.
 * @author yingfeng
 *
 */
public interface FileStorageService {
	
	/**
	 * Upload the given file for the given primaryfile.
	 * @param id ID of the given primaryfile
	 * @param file the media file to be uploaded
	 * @return the primaryfile with media file uploaded
	 */
	public Primaryfile uploadPrimaryfile(Long id, MultipartFile file);

	/**
	 * Upload the given file for the given primaryfile.
	 * @param primaryfile the given primaryfile
	 * @param file the media file to be uploaded
	 * @return the primaryfile with media file uploaded
	 */
	public Primaryfile uploadPrimaryfile(Primaryfile primaryfile, MultipartFile file);

	/**
	 * Upload the given file for the given collectionSupplement.
	 * @param id ID of the given collectionSupplement
	 * @param file the media file to be uploaded
	 * @return the collectionSupplement with media file uploaded
	 */
	public CollectionSupplement uploadCollectionSupplement(Long id, MultipartFile file);

	/**
	 * Upload the given file for the given collectionSupplement.
	 * @param collectionSupplement the given collectionSupplement
	 * @param file the media file to be uploaded
	 * @return the collectionSupplement with media file uploaded
	 */
	public CollectionSupplement uploadCollectionSupplement(CollectionSupplement collectionSupplement, MultipartFile file);

	/**
	 * Upload the given file for the given itemSupplement.
	 * @param id ID of the given itemSupplement
	 * @param file the media file to be uploaded
	 * @return the itemSupplement with media file uploaded
	 */
	public ItemSupplement uploadItemSupplement(Long id, MultipartFile file);

	/**
	 * Upload the given file for the given itemSupplement.
	 * @param itemSupplement the given itemSupplement
	 * @param file the media file to be uploaded
	 * @return the itemSupplement with media file uploaded
	 */
	public ItemSupplement uploadItemSupplement(ItemSupplement itemSupplement, MultipartFile file);

	/**
	 * Upload the given file for the given primaryfileSupplement.
	 * @param id ID of the given primaryfileSupplement
	 * @param file the media file to be uploaded
	 * @return the primaryfileSupplement with media file uploaded
	 */
	public PrimaryfileSupplement uploadPrimaryfileSupplement(Long id, MultipartFile file);

	/**
	 * Upload the given file for the given primaryfileSupplement.
	 * @param primaryfileSupplement the given primaryfileSupplement
	 * @param file the media file to be uploaded
	 * @return the primaryfileSupplement with media file uploaded
	 */
	public PrimaryfileSupplement uploadPrimaryfileSupplement(PrimaryfileSupplement primaryfileSupplement, MultipartFile file);

	/**
	 * Stores the specified sourceFile to the specified targetPathname on the file system.
	 * @param sourceFile the specified source File
	 * @param targetPathname the specified target file path name relative to the storage root
	 */
	public void store(MultipartFile sourceFile, String targetPathname);
	
	/**
	 * Resolve the path for the given pathname relative to the amppd file system root.
	 * @param pathname the given pathname 
	 * @return the path object for the given pathname
	 */
	public Path resolve(String pathname);	

	/**
	 * Returns the absolute pathname for the given pathname relative to the Amppd file system root.
	 * @param pathname the given relative pathname 
	 * @return the absolute pathname resolved
	 */
	public String absolutePathName(String pathname);	

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
	 * Clean up all files under the file storage root.
	 */
	public void cleanAll();
		
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

	/**
	 * Moves a file from the source to the destination by creating a hard link and then deleting the original file.  
	 * @param sourcePath Source file path
	 * @param destinationPath Destination file path
	 * @throws IOException 
	 */
	public void moveFile(Path sourcePath, Path destinationPath) throws IOException;
	
	/**
	 * Reads all content from the given text file to a string using UTF-8 encoding.
	 * @param pathame pathname of the given file relative to amppd root
	 * @return the string with all file content
	 */
	public String readTextFile(String pathame);
	
	/**
	 * Encodes characters not in [A-Z], [0-9], [.], [-], or [_] using a %[hex] format.  Note that passing in an entire path will result in a malformed path as '/' will be encoded
	 * @param path to encode
	 * @return URI encoded value
	 */
	public String encodeUri(String path);
	
	/**
	 * Gets the URI encoded dropbox path for a given collection
	 * @param unitName
	 * @param collectionName
	 * @return
	 */
	public Path getDropboxPath(String unitName, String collectionName);
	
	/**
	 * Gets the URI encoded drop box path for a given unit
	 * @param unitName
	 * @return
	 */
	public Path getDropboxPath(String unitName);
	
	
}
