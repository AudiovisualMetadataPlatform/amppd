package edu.indiana.dlib.amppd.service;

import java.nio.file.Path;

import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.web.ItemSearchResponse;

/**
 * Service for serving media files for primaryfiles and supplements. 
 * @author yingfeng
 */
public interface MediaService {

	/**
	 * Get the media file download URL for the given primaryfile ID.
	 * Note that this method does not verify that the primaryfile for this ID exists in the system.
	 * @param primaryfileId the given primaryfile ID
	 * @return the generated media URL
	 */
	public String getPrimaryfileMediaUrl(Long primaryfileId);

	/**
	 * Get the media file download URL for the given primaryfile.
	 * @param primaryfile the given primaryfile
	 * @return the generated media URL
	 */
	public String getPrimaryfileMediaUrl(Primaryfile primaryfile);
			
	/**
	 * Get the absolute pathname of the supplement, given its name, type, and parent to which the given primaryfile belongs. 
	 * @param primaryfile the given primaryfile whose ancestor is the parent associated with the supplement 
	 * @param name name of the supplement
	 * @param type association type of the supplement
	 * @return absolute pathname of the supplement if found, or null otherwise
	 */
	public String getSupplementPath(Primaryfile primaryfile, String name, SupplementType type);
	
	/**
	 * Get the media information JSON file pathname for the given asset.
	 * @param asset the given asset
	 * @return the absolute pathname of the media info JSON file
	 */
	public String getAssetMediaInfoPath(Asset asset);	

	/**
	 * Set the absolute pathname for the given asset.
	 * @param asset the given asset
	 * @return the absolute pathname of the asset
	 */
	public String setAssetAbsoluatePath(Asset asset);	

	/**
	 * Get the output file access URL for the given WorkflowResult.
	 * @param WorkflowResultId ID of the given WorkflowResult
	 * @return the generated output URL
	 */
	public String getWorkflowResultOutputUrl(Long workflowResultId);

	/**
	 * Get the output file extension for the given WorkflowResult, based on its dataset type/extension.
	 * @param the given WorkflowResult
	 * @return the file extension of the output file
	 */
	public String getWorkflowResultOutputExtension(WorkflowResult workflowResult);

	/**
	 * Get the media symlink URL for the given primaryfile:
	 * create a new one if not existing yet; or reuse the existing symlink if already created.
	 * @param id ID of the given primaryfile
	 * @return the absolute path of the media symlink
	 */
	public String getPrimaryfileSymlinkUrl(Long id);

	/**
	 * Get the output symlink URL for the given WorkflowResult:
	 * create a new one if not existing yet; or reuse the existing symlink if already created.
	 * @param id ID of the given WorkflowResult
	 * @return the absolute path of the output symlink
	 */
	public String getWorkflowResultOutputSymlinkUrl(Long id);
	
	/**
	 * Reset the symlink of the given asset to null, delete the symlink from the symlink directoy,
	 * and persist the updated asset if indicated.
	 * @param the given asset
	 * @return the deleted symlink.
	 */
	public String resetSymlink(Asset asset, boolean persist);

	/**
	 * Create an obscure symlink for the given asset, if it hasn't been created,
	 * in the symlink directory where static contents are served by AMPPD-UI Apache server.
	 * @param the given asset
	 * @return the created symlink.
	 */
	public String createSymlink(Asset asset);
	
	/**
	 * Create an obscure symlink for the output of the given WorkflowResult, if it hasn't been created,
	 * in the symlink directory where static contents are served by AMPPD-UI Apache server.
	 * @param the given WorkflowResult
	 * @return the created symlink.
	 */
	public String createSymlink(WorkflowResult workflowResult);
	
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
	
	/**
	 * Find items and/or primaryfiles with names containing the given keyword, and with media of the given media type. 
	 * @param keyword the given keyword
	 * @param mediaType the given media type
	 * @return an instance of ItemSearchResponse containing information of the found items/primaryfiles
	 */
	public ItemSearchResponse findItemOrFile(String keyword, String mediaType);
	
}
