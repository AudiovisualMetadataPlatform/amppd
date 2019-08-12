package edu.indiana.dlib.amppd.galaxy;

import com.github.jmchilton.blend4j.galaxy.LibrariesClient;
import com.github.jmchilton.blend4j.galaxy.beans.Library;

/**
 * Extension to blend4j LibrariesClient with some helper methods to interact with Galaxy data libary.
 * @author yingfeng
 *
 */
public interface GalaxyLibrariesClient extends LibrariesClient {

	/**
	 * Return the data library for the given name.
	 */
	public Library getLibrary(String name);
	
}
