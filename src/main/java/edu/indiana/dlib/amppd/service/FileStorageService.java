package edu.indiana.dlib.amppd.service;

import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;

/**
 * Service for storing/retrieving files, including primaryfiles, supplements associated with collections/items/primaryfiles, as well as intermediate files.
 * @author yingfeng
 */
public interface FileStorageService {
	
	/**
	 * Return the target storage directory path name relative to the storage root for the specified dataentity.
	 * @param dataentity the specified dataentity
	 * @return the target storage directory path name
	 */
	public String getDirPathname(Dataentity dataentity);

	/**
	 * Return the target storage file path name relative to the storage root for the specified asset with its originalFilename populated.
	 * @param asset the specified asset
	 * @return the target storage file path name
	 */
	public String getFilePathname(Asset asset);
	
	/**
	 * Upload the given media file for the given asset.
	 * @param id the ID of the given asset
	 * @param file the media file to be uploaded
	 * @param type the SupplementType of the given asset
	 * @return the asset with media file uploaded
	 */
	public Asset uploadAsset(Long id, MultipartFile file, SupplementType type);
	
	/**
	 * Upload the given media file for the given asset.
	 * @param asset the given asset
	 * @param file the media file to be uploaded
	 * @return the asset with media file uploaded
	 */
	public Asset uploadAsset(Asset asset, MultipartFile file);
	
	/**
	 * Move the media sub-directory associated with the given dataentity, as needed,
	 * from its old parent's media sub-directory to its new parent's media sub-directory.
	 * @param dataentity the given dataentity
	 * @return the new pathname of the media sub-directory 
	 */
	public String moveEntityDir(Dataentity dataentity);
			
	/**
	 * Move the media file and the associated media info JSON file for the given asset, as needed,
	 * from its old parent's media sub-directory to its new parent's media sub-directory.
	 * @param asset the given asset
	 * @return the new pathname of the media file 
	 */
	public String moveAsset(Asset asset);
		
	/**
	 * Delete the media sub-directory associated with the given data entity, if exists.
	 * @param dataentity the given dataentity
	 * @return the pathname of the media sub-directory 
	 */
	public String deleteEntityDir(Dataentity dataentity);

	/**
	 * Unload (remove) the media file and the associated media info JSON file for the given asset.
	 * @param asset the given asset
	 * @return the pathname of the media file 
	 */
	public String unloadAsset(Asset asset);

	/**
	 * Stores the specified sourceFile to the specified targetPathname on the file system.
	 * @param sourceFile the specified source File
	 * @param targetPathname the specified target file path name relative to the storage root
	 * @return the path of the stored file
	 */
	public Path store(MultipartFile sourceFile, String targetPathname);

	/**
	 * Move a file or directory from the specified source pathname to the specified target pathname.
	 * If source doesn't exist, no active will be taken; if the target already exists, it will be replaced.
	 * @param sourcePathname the specified source pathname
	 * @param targetPathname the specified target pathname
	 * @return the path of the target pathname
	 */
	public Path move(String sourcePathname, String targetPathname);

	/**
	 * Delete the file/directory recursively with the specified pathname if exists; otherwise no action.
	 * @param pathname the specified pathname
	 * @return the path of the file/directory to delete
	 */
	public Path delete(String pathname);
	
	/**
	 * Link a file from the source to the destination by creating a hard link and then deleting the original file.  
	 * @param sourcePath Source file path
	 * @param targetPath Destination file path
	 * @return the destination file path
	 */
	public Path linkFile(Path sourcePath, Path targetPath);
	
	/**
	 * Read all content from the given text file to a string using UTF-8 encoding.
	 * @param pathame pathname of the given file relative to amppd root
	 * @return the string with all file content
	 */
	public String readTextFile(String pathame);
	
	/**
	 * Resolve the path for the given pathname relative to the amppd file system root.
	 * @param pathname the given pathname 
	 * @return the path object for the given pathname
	 */
	public Path resolve(String pathname);	

	/**
	 * Return the absolute pathname for the given pathname relative to the Amppd file system root.
	 * @param pathname the given relative pathname 
	 * @return the absolute pathname resolved
	 */
	public String absolutePathName(String pathname);	

	/**
	 * Load the file with the specified pathname as a resource.
	 * @param pathname the specified pathname
	 * @return resource loaded
	 */
	public Resource loadAsResource(String pathname);
	
	/**
	 * Clean up all files under the file storage root.
	 */
	public void cleanup();
		
//	/**
//	 * Upload the given file for the given primaryfile.
//	 * @param id ID of the given primaryfile
//	 * @param file the media file to be uploaded
//	 * @return the primaryfile with media file uploaded
//	 */
//	public Primaryfile uploadPrimaryfile(Long id, MultipartFile file);
//
//	/**
//	 * Upload the given file for the given primaryfile.
//	 * @param primaryfile the given primaryfile
//	 * @param file the media file to be uploaded
//	 * @return the primaryfile with media file uploaded
//	 */
//	public Primaryfile uploadPrimaryfile(Primaryfile primaryfile, MultipartFile file);
//
//	/**
//	 * Upload the given file for the given collectionSupplement.
//	 * @param id ID of the given collectionSupplement
//	 * @param file the media file to be uploaded
//	 * @return the collectionSupplement with media file uploaded
//	 */
//	public CollectionSupplement uploadCollectionSupplement(Long id, MultipartFile file);
//
//	/**
//	 * Upload the given file for the given collectionSupplement.
//	 * @param collectionSupplement the given collectionSupplement
//	 * @param file the media file to be uploaded
//	 * @return the collectionSupplement with media file uploaded
//	 */
//	public CollectionSupplement uploadCollectionSupplement(CollectionSupplement collectionSupplement, MultipartFile file);
//
//	/**
//	 * Upload the given file for the given itemSupplement.
//	 * @param id ID of the given itemSupplement
//	 * @param file the media file to be uploaded
//	 * @return the itemSupplement with media file uploaded
//	 */
//	public ItemSupplement uploadItemSupplement(Long id, MultipartFile file);
//
//	/**
//	 * Upload the given file for the given itemSupplement.
//	 * @param itemSupplement the given itemSupplement
//	 * @param file the media file to be uploaded
//	 * @return the itemSupplement with media file uploaded
//	 */
//	public ItemSupplement uploadItemSupplement(ItemSupplement itemSupplement, MultipartFile file);
//
//	/**
//	 * Upload the given file for the given primaryfileSupplement.
//	 * @param id ID of the given primaryfileSupplement
//	 * @param file the media file to be uploaded
//	 * @return the primaryfileSupplement with media file uploaded
//	 */
//	public PrimaryfileSupplement uploadPrimaryfileSupplement(Long id, MultipartFile file);
//
//	/**
//	 * Upload the given file for the given primaryfileSupplement.
//	 * @param primaryfileSupplement the given primaryfileSupplement
//	 * @param file the media file to be uploaded
//	 * @return the primaryfileSupplement with media file uploaded
//	 */
//	public PrimaryfileSupplement uploadPrimaryfileSupplement(PrimaryfileSupplement primaryfileSupplement, MultipartFile file);
	
//	/**
//	 * Return the target storage directory path name relative to the storage root for the specified unit.
//	 * @param unit the specified unit
//	 * @return the target storage directory path name
//	 */
//	public String getDirPathname(Unit unit);
//
//	/**
//	 * Return the target storage directory path name relative to the storage root for the specified collection.
//	 * @param collection the specified collection
//	 * @return the target storage directory path name
//	 */
//	public String getDirPathname(Collection collection);
//
//	/**
//	 * Return the target storage directory path name relative to the storage root for the specified item.
//	 * @param item the specified item
//	 * @return the target storage directory path name
//	 */
//	public String getDirPathname(Item item);
//
//	/**
//	 * Return the target storage directory path name relative to the storage root for the specified primaryfile.
//	 * @param primaryfile the specified primaryfile
//	 * @return the target storage directory path name
//	 */
//	public String getDirPathname(Primaryfile primaryfile);
//	
//	/**
//	 * Return the target storage file path name relative to the storage root for the specified primaryfile with its originalFilename populated.
//	 * @param primaryfile the specified primaryfile
//	 * @return the target storage file path name
//	 */
//	public String getFilePathname(Primaryfile primaryfile);
//
//	/**
//	 * Return the target storage file path name relative to the storage root for the specified supplement with its originalFilename populated.
//	 * @param supplement the specified supplement
//	 * @return the target storage file path name
//	 */
//	public String getFilePathname(Supplement supplement);
	
}
