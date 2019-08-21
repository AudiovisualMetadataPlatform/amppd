package edu.indiana.dlib.amppd.service.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.HistoriesClient;
import com.github.jmchilton.blend4j.galaxy.LibrariesClient;
import com.github.jmchilton.blend4j.galaxy.beans.FilesystemPathsLibraryUpload;
import com.github.jmchilton.blend4j.galaxy.beans.GalaxyObject;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.Library;
import com.github.jmchilton.blend4j.galaxy.beans.LibraryContent;

import edu.indiana.dlib.amppd.exception.GalaxyFileUploadException;
import edu.indiana.dlib.amppd.service.GalaxyApiService;
import edu.indiana.dlib.amppd.service.GalaxyDataService;
import lombok.Getter;
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
	
	public static final String SHARED_LIBARY_NAME = "amppd";
	public static final String SHARED_HISTORY_NAME = "amppd";
	
	@Autowired
	private GalaxyApiService galaxyApiService;
	
	private GalaxyInstance galaxyInstance;
	
	@Getter
	private LibrariesClient librariesClient;
	
	@Getter
	private HistoriesClient historiesClient;
	
	@Getter
	private Library sharedLibrary;

	@Getter
	private History sharedHistory;

	/**
	 *  initialize Galaxy data library, which is shared by all AMPPD users.
	 */
	@PostConstruct
	public void init() {
		galaxyInstance = galaxyApiService.getGalaxyInstance();
		librariesClient = galaxyInstance.getLibrariesClient();

		// if the amppd shared data library already exists, don't create another one
		Library library = getLibrary(SHARED_LIBARY_NAME);
		if (library != null) {
			log.info("The shared Galaxy data library for AMPPD users already exists: " + SHARED_LIBARY_NAME);
			sharedLibrary = library;
			return;
		}
		
		// otherwise create a new data library shared by all Amppd users
		library = new Library(SHARED_LIBARY_NAME);
		library.setDescription("AMPPD Shared Library");
		try {
			sharedLibrary = librariesClient.createLibrary(library);
			log.info("Initialized shared Galaxy data library for AMPPD users: " + sharedLibrary.getName());
		}
		catch (Exception e) {
			String msg = "Cannot create shared Galaxy data library for AMPPD users.";
			log.severe(msg);
			throw new RuntimeException(msg, e);
		}	
		
		// if the amppd shared data history already exists, don't create another one
		History history = getHistory(SHARED_HISTORY_NAME);
		if (history != null) {
			log.info("The shared Galaxy data history for AMPPD users already exists: " + SHARED_HISTORY_NAME);
			sharedHistory = history;
			return;
		}
		
		// otherwise create a new data history shared by all Amppd users
		history = new History(SHARED_HISTORY_NAME);
		try {
			sharedHistory = historiesClient.create(history);
			log.info("Initialized shared Galaxy data history for AMPPD users: " + sharedHistory.getName());
		}
		catch (Exception e) {
			String msg = "Cannot create shared Galaxy data history for AMPPD users.";
			log.severe(msg);
			throw new RuntimeException(msg, e);
		}		
		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyDataService.getLibrary(String)
	 */
	public Library getLibrary(String name) {
		Library matchingLibrary = null;		
		List<Library> libraries = librariesClient.getLibraries();

		for(Library library : libraries) {
			if (library.getName().equals(name)) {
				matchingLibrary = library;
				break;
			}
		}

		return matchingLibrary;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyDataService.getHistory(String)
	 */
	public History getHistory(String name) {
		History matchingHistory = null;		
		List<History> histories = historiesClient.getHistories();

		for(History history : histories) {
			if (history.getName().equals(name)) {
				matchingHistory = history;
				break;
			}
		}

		return matchingHistory;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyDataService.uploadFileToGalaxy(String,String)
	 */
	public GalaxyObject uploadFileToGalaxy(String filePath, String libraryName) {
		GalaxyObject uploadData = null;
		String msg = "Uploading file from Amppd file system to Galaxy data library... File path: " + filePath + "\t Galaxy Library:" + libraryName;
		log.info(msg);

		// if the target library is the shared amppd (i.e. sharedLibrary), no need to retrieve by name
		Library matchingLibrary = SHARED_LIBARY_NAME.equals(libraryName) ? sharedLibrary : getLibrary(libraryName);
		
		if (matchingLibrary != null) {
			final LibraryContent rootFolder = librariesClient.getRootFolder(matchingLibrary.getId());
			final FilesystemPathsLibraryUpload upload = new FilesystemPathsLibraryUpload();
			upload.setContent(filePath);
			upload.setLinkData(true);
			upload.setFolderId(rootFolder.getId());
			try {
				uploadData = librariesClient.uploadFilesystemPaths(matchingLibrary.getId(), upload);
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

		return uploadData;
	}		
			
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyDataService.uploadFileToGalaxy(String)
	 */
	public GalaxyObject uploadFileToGalaxy(String filePath) {
		return uploadFileToGalaxy(filePath, SHARED_LIBARY_NAME);
	}
	
}
