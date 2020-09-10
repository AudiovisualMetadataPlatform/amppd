package edu.indiana.dlib.amppd.service;

import java.nio.file.Path;

import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.model.DashboardResult;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.web.ItemSearchResponse;

/**
 * Service for serving media files for primaryfiles and supplements. 
 * @author yingfeng
 *
 */
public interface MediaService {

	/**
	 * Get the media file download URL for the given primaryfile.
	 * @param primaryfile the given primaryfile
	 * @return the generated media URL
	 */
	public String getPrimaryfileMediaUrl(Primaryfile primaryfile);

	/**
	 * Get the media information JSON file path for the given asset.
	 * @param asset the given asset
	 * @return the absolute path of the media info JSON file
	 */
	public String getAssetMediaInfoPath(Asset asset);	
	
	/**
	 * Get the media symlink URL for the given primaryfile:
	 * create a new one if not existing yet; or reuse the existing symlink if already created.
	 * @param id ID of the given primaryfile
	 * @return the absolute path of the media symlink
	 */
	public String getPrimaryfileSymlinkUrl(Long id);
	
	/**
	 * Create an obscure symlink for the given asset, if it hasn't been created,
	 * in the symlink directory where static contents are served by AMPPD-UI Apache server.
	 * @param the given asset
	 * @return the created symlink.
	 */
	public String createSymlink(Asset asset);
	
	/**
	 * Get the output file access URL for the given DashboardResult.
	 * @param dashboardId ID of the given DashboardResult
	 * @return the generated output URL
	 */
	public String getDashboardOutputUrl(Long dashboardId);

	/**
	 * Get the output symlink URL for the given dashboardResult:
	 * create a new one if not existing yet; or reuse the existing symlink if already created.
	 * @param id ID of the given dashboardResult
	 * @return the absolute path of the output symlink
	 */
	public String getDashboardOutputSymlinkUrl(Long id);

	/**
	 * Get the output file extension for the given dashboardResult, based on its dataset type/extension.
	 * @param the given dashboardResult
	 * @return the file extension of the output file
	 */
	public String getDashboardOutputExtension(DashboardResult dashboardResult);
	
	/**
	 * Create an obscure symlink for the output of the given dashboardResult, if it hasn't been created,
	 * in the symlink directory where static contents are served by AMPPD-UI Apache server.
	 * @param the given dashboardResult
	 * @return the created symlink.
	 */
	public String createSymlink(DashboardResult dashboardResult);
	
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
	public void cleanup();
	
	public ItemSearchResponse findItemOrFile(String keyword, String mediaType);
	
}
