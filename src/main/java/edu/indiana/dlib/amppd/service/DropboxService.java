package edu.indiana.dlib.amppd.service;

import java.nio.file.Path;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Unit;

/**
 * Service for operations related to dropbox, including creating, updating, deleting dropbox sub-directories.
 * @author yingfeng dfische3
 */
public interface DropboxService {

	/**
	 * Encodes characters not in [A-Z], [0-9], [.], [-], or [_] using a %[hex] format.  Note that passing in an entire path will result in a malformed path as '/' will be encoded
	 * @param path to encode
	 * @return URI encoded value
	 */
	public String encodeUri(String path);

	/**
	 * Gets the URI encoded dropbox path given the unit name.
	 * @param unitName name of the unit
	 * @return the dropbox sub-directory path for the unit
	 */
	public Path getDropboxPath(String unitName);

	/**
	 * Gets the URI encoded dropbox path given the unit and collection names. 
	 * @param unitName name of the unit
	 * @param collectionName name of the collection
	 * @return the dropbox sub-directory path for the collection
	 */
	public Path getDropboxPath(String unitName, String collectionName);

	/**
	 * Gets the URI encoded dropbox path for a given unit.
	 * @param unit the given unit
	 * @return the dropbox sub-directory path for the unit
	 */
	public Path getDropboxPath(Unit unit);

	/**
	 * Gets the URI encoded dropbox path for a given collection.
	 * @param collection the given collection
	 * @return the dropbox sub-directory path for the collection
	 */
	public Path getDropboxPath(Collection collection);

	/**
	 * Rename the sub-directory for the given unit; if the previous directory doesn't exists or unit name didn't change, do nothing.
	 * @param unit the given unit
	 * @return the path of the dropbox sub-directory renamed
	 */
	public Path renameUnitSubdir(Unit unit);
	
	/**
	 * Delete the sub-directory for the given unit; if the directory doesn't exists, do nothing.
	 * @param unit the given unit
	 * @return the path of the dropbox sub-directory deleted
	 */
	public Path deleteUnitSubdir(Unit unit);
	
	/**
	 * Create a sub-directory for the given collection; if the directory already exists, do nothing.
	 * @param collection the given collection
	 * @return the path of the dropbox sub-directory created
	 */
	public Path createCollectionSubdir(Collection collection);
	
	/**
	 * Rename the sub-directory for the given collection; 
	 * if the previous directory doesn't exists, create a new one;
	 * otherwise, if the collection name didn't change, do nothing.
	 * @param collection the given collection
	 * @return the path of the dropbox sub-directory renamed
	 */
	public Path renameCollectionSubdir(Collection collection);
	
	/**
	 * Delete the sub-directory for the given collection; if the directory doesn't exists, do nothing.
	 * @param collection the given collection
	 * @return the path of the dropbox sub-directory deleted
	 */
	public Path deleteCollectionSubdir(Collection collection);
	
	/**
	 * Create sub-directories as needed for all existing collections.
	 */
	public void createCollectionSubdirs();
	
	// TODO more methods can be added to handle update/delete dropbox subdirs as needed later.
}
