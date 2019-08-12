package edu.indiana.dlib.amppd.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.LibrariesClient;
import com.github.jmchilton.blend4j.galaxy.beans.FilesystemPathsLibraryUpload;
import com.github.jmchilton.blend4j.galaxy.beans.Library;
import com.github.jmchilton.blend4j.galaxy.beans.LibraryContent;
import com.sun.jersey.api.client.ClientResponse;

import edu.indiana.dlib.amppd.exception.GalaxyFileUploadException;
import lombok.extern.java.Log;

/**
 * Implementation of GalaxyDataService. 
 * This serves as an extension to the blend4j data related clients such as LibraryClientImpl, HistoryClientImpl etc.,
 * since we cannot extend such classes as they are not public. 
 * @author yingfeng
 *
 */
@Service
@Log
public class GalaxyDataServiceImpl implements GalaxyDataService {
	
	public static String GALAXY_LIBARY_NAME = "amppd";
	
	@Autowired
	private GalaxyApiService galaxyApiService;
	
	private GalaxyInstance galaxyInstance;
	private LibrariesClient libraryClient;
	private Library galaxyLibrary;

	/**
	 *  initialize Galaxy data library, which is shared by all Amppd users
	 */
	@PostConstruct
	public void init() {
		galaxyInstance = galaxyApiService.getInstance();
		libraryClient = galaxyInstance.getLibrariesClient();

		// if the amppd shared data library already exists, don't create another one
		Library library = getLibrary(GALAXY_LIBARY_NAME);
		if (library != null) {
			log.info("The shared Galaxy data library for AMP users already exists: " + GALAXY_LIBARY_NAME);
			galaxyLibrary = library;
			return;
		}
		
		// otherwise create a new data library shared by all Amppd users
		library = new Library(GALAXY_LIBARY_NAME);
		galaxyLibrary.setDescription("Amppd Shared Library");
		try {
			galaxyLibrary = libraryClient.createLibrary(galaxyLibrary);
			log.info("Initialized shared Galaxy data library for AMP users: " + galaxyLibrary.getName());
		}
		catch (Exception e) {
			String msg = "Cannot create shared Galaxy data library for AMP users.";
			log.severe(msg);
			throw new RuntimeException(msg, e);
		}		
	}

	
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyDataService.getLibrary(String)
	 */
	public Library getLibrary(String name) {
		Library matchingLibrary = null;		
		List<Library> libraries = libraryClient.getLibraries();

		for(Library library : libraries) {
			if (library.getName().equals(name)) {
				matchingLibrary = library;
				break;
			}
		}

		return matchingLibrary;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyDataService.uploadFileToGalaxy(String,String)
	 */
	public ClientResponse uploadFileToGalaxy(String filePath, String libraryName) {
		ClientResponse uploadResponse = null;
		String msg = "Uploading file from Amppd file system to Galaxy data library... File path: " + filePath + "\t Galaxy Library:" + libraryName;
		log.info(msg);

		// if the target library is the shared ammpd (i.e. galaxyLibrary), just no need to retrieve by name
		Library matchingLibrary = GALAXY_LIBARY_NAME.equalsIgnoreCase(libraryName) ? galaxyLibrary : getLibrary(libraryName);
		
		if (!matchingLibrary.equals(null)) {
			final LibraryContent rootFolder = libraryClient.getRootFolder(matchingLibrary.getId());
			final FilesystemPathsLibraryUpload upload = new FilesystemPathsLibraryUpload();
			upload.setContent(filePath);
			upload.setLinkData(true);
			upload.setFolderId(rootFolder.getId());
			try {
				uploadResponse = libraryClient.uploadFileFromUrl(matchingLibrary.getId(), upload);
				msg = "Upload completed.";
				log.info(msg);
			}
			catch (Exception e) {
				msg = "Upload failed. " + e.getMessage();
				log.severe(msg);
				throw new GalaxyFileUploadException(msg, e);
			}
		} else {
			msg = "Upload failed, unable to find the data library " + libraryName;
			log.severe(msg);
			throw new GalaxyFileUploadException(msg);
		}

		return uploadResponse;
	}	
			
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyDataService.uploadFileToGalaxy(String)
	 */
	public ClientResponse uploadFileToGalaxy(String filePath) {
		return uploadFileToGalaxy(filePath, GALAXY_LIBARY_NAME);
	}
	
}
