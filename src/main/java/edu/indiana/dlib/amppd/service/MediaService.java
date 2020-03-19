package edu.indiana.dlib.amppd.service;

import java.nio.file.Path;

import edu.indiana.dlib.amppd.model.Asset;

/**
 * Service for serving media files for primaryfiles and supplements. 
 * @author yingfeng
 *
 */
public interface MediaService {

	/**
	 * Get the media symlink URL for the given primaryfile:
	 * create a new one if not existing yet; or reuse the existing symlink if already created.
	 * @param id ID of the given primaryfile
	 * @return the absolute path of the media symlink
	 */
	public String getPrimaryfileSymlinkUrl(Long id);
	
	/**
	 * Create an obscure symlink for the given asset, if it hasn't been created. 
	 * in the media directory where static contents are served by AMPPD-UI Apache server.
	 * @param the given asset
	 * @return the created symlink.
	 */
	public String createSymlink(Asset asset);
	
	/**
	 * Resolve the path for the given pathname relative to the AMPPD media symlink root.
	 * @param pathname the given pathname 
	 * @return the path object for the given pathname
	 */
	public Path resolve(String pathname);	
	
	/**
	 * Deletes the specified symlink.
	 * @param symlink the specified symlink
	 */
	public void delete(String symlink);
	
	/**
	 * Clean up all symlinks under the media symlink root.
	 */
	public void cleanAll();	
	
}
