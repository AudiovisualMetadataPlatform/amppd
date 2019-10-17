package edu.indiana.dlib.amppd.service;

import com.github.jmchilton.blend4j.galaxy.HistoriesClient;
import com.github.jmchilton.blend4j.galaxy.LibrariesClient;
import com.github.jmchilton.blend4j.galaxy.beans.GalaxyObject;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.Library;

/**
 * Service to provide convenient helpers to operate on Galaxy data libraries, datasets, histories etc. 
 * @author yingfeng
 *
 */
public interface GalaxyDataService {

	/**
	 * Return the librariesClient instance.
	 */
	public LibrariesClient getLibrariesClient();
	
	/**
	 * Return the HistoriesClient instance.
	 */
	public HistoriesClient getHistoriesClient();
	
	/**
	 * Return the shared amppd data library.
	 */
	public Library getSharedLibrary();
	
	/**
	 * Return the shared history for all workflow executions.
	 */
	public History getSharedHistory();	

	/**
	 * Return the data library for the given name, or null if not found.
	 * @param name the the given library name
	 */
	public Library getLibrary(String name);

	/**
	 * Return the data history for the given name, or null if not found.
	 * @param name the the given history name
	 */
	public History getHistory(String name);

//	/**
//	 * Create output history for the given primaryfile, if not existing yet.
//	 * @param ID of the the given primaryfile.
//	 * @return the new history created, or existing history if found
//	 */
//	public History createHistory(Long primaryfileId);

	/**
	 * Upload a file/folder from AMP file system to a Galaxy data library without copying the physical file, which results in a dataset being created for the file in the library. 
	 * @param filePath the path of the source file/folder to be uploaded
	 * @param libraryName the name of the target library to upload file to  
	 * @return the GalaxyObject instance containing the ID and URL of the dataset being created in the library for the uploaded file 
	 */
	public GalaxyObject uploadFileToGalaxy(String filePath, String libraryName);

	/**
	 * Upload a file/folder from AMP file system to the shared amppd Galaxy data library without copying the physical file. 
	 * @param filePath the path of the source file/folder to be uploaded
	 * @return the GalaxyObject instance containing the ID and URL of the dataset being created in the library for the uploaded file 
	 */
	public GalaxyObject uploadFileToGalaxy(String filePath);
	
}
