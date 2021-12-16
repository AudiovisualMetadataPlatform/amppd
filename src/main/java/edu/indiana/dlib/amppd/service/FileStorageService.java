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
	 * Upload the given media file for the given asset, and persist the updated asset to DB.
	 * @param asset the given asset
	 * @param file the media file to be uploaded
	 * @return the asset with media file uploaded
	 */
	public Asset uploadAsset(Asset asset, MultipartFile file);
	
	/**
	 * Move the media sub-directory (if exists) of the given dataentity, in case its parent is changed.
	 * @param dataentity the given dataentity
	 * @return the updated pathname of the media sub-directory 
	 */
	public String moveEntityDir(Dataentity dataentity);
			
	/**
	 * Move the media and the info files of the given asset, in case its parent is changed and persist the updated asset if indicated.
	 * @param asset the given asset
	 * @param persist if true, save the updated asset to DB; otherwise, do not save yet
	 * @return the updated pathname of the media file 
	 */
	public String moveAsset(Asset asset, boolean persist);
		
	/**
	 * Delete the media sub-directory (if exists) of the given data entity.
	 * @param dataentity the given dataentity
	 * @return the pathname of the media sub-directory 
	 */
	public String deleteEntityDir(Dataentity dataentity);

	/**
	 * Unload (delete) the media and the info files of the given asset.
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
	 * If the source doesn't exist, no action will be taken; if the target already exists, it will be replaced.
	 * @param sourcePathname the specified source pathname
	 * @param targetPathname the specified target pathname
	 * @return the path of the target pathname if moved, null otherwise
	 */
	public Path move(String sourcePathname, String targetPathname);

	/**
	 * Delete the file/directory recursively with the specified pathname if exists, no action otherwise.
	 * @param pathname the specified pathname
	 * @return the path of the file/directory if deleted, null otherwise
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
