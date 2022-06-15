package edu.indiana.dlib.amppd.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.exception.MediaConversionException;
import edu.indiana.dlib.amppd.exception.PreprocessException;
import edu.indiana.dlib.amppd.exception.StorageFileNotFoundException;
import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.PreprocessService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implmentation of PreprocessService.
 * @author yingfeng
 */
@Service
@Slf4j
public class PreprocessServiceImpl implements PreprocessService {
	
	@Autowired
	private AmppdPropertyConfig amppdPropertyConfig;
	
	@Autowired
    private FileStorageService fileStorageService;		

	@Autowired
	private DataentityService dataentityService;

	@Autowired
	private ServletContext servletContext;

	/**
	 * @see edu.indiana.dlib.amppd.service.PreprocessServiceImpl.convertFlacToWav(String)
	 */
	@Override
	public String convertFlacToWav(String sourceFilepath) {
		String extension = FilenameUtils.getExtension(sourceFilepath);
		
		if (extension == null || !extension.equalsIgnoreCase("flac")) {
			log.info("No conversion as the given source file is not in FLAC format: " + sourceFilepath);
			return null;
		}
		
		// ffmpeg with -y so that it forces overwrite if target file already exists; 
		// otherwise the process will be blocked waiting for user to confirm overwriting
		String targetFilePath = FilenameUtils.getFullPath(sourceFilepath) + FilenameUtils.getBaseName(sourceFilepath) + ".wav";
		String command = "ffmpeg -y -i " + fileStorageService.absolutePathName(sourceFilepath) + " " + fileStorageService.absolutePathName(targetFilePath);
		
		try {
			Process process = Runtime.getRuntime().exec(command);
		    final int status = process.waitFor();
		    if (status != 0) {		    	
				// capture the error outputs into log
		    	BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		    	StringBuilder builder = new StringBuilder();
		    	String line = null;
		    	while ( (line = reader.readLine()) != null) {
		    		builder.append(line);
		    		builder.append(System.getProperty("line.separator"));
		    	}
				log.error(builder.toString());
		    	throw new MediaConversionException("Exception while converting " + sourceFilepath + " to " + targetFilePath + ": ffmpeg exited with status " + status);
		    }
		    
//			// workaround for local debugging in case ffmpeg/ffprobe is not available
//			// fake a the wav file by copying the original flac file
//			Files.copy(fileStorageService.resolve(sourceFilepath), fileStorageService.resolve(targetFilePath));
		}
		catch (Exception e) {
			throw new MediaConversionException("Exception while converting " + sourceFilepath + " to " + targetFilePath, e);
		}
		
		log.info("Converted source flac file to target wav file: " + sourceFilepath + "->" + targetFilePath);
		return targetFilePath;		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PreprocessServiceImpl.convertFlac(Asset)
	 */
	@Override
	public Asset convertFlac(Asset asset) {
		String targetFilePath = convertFlacToWav(asset.getPathname());
				
		// if conversion happened, update the extension of originalFilename and pathname,
		// the originalFilename's extension is used when computing asset file pathname,
		// note that we do not remove the original flac file just in case of future use;
		// TODO if desired we can remove flac file after conversion, if there is no need to keep for audit/troubleshooting purpose
		if (targetFilePath != null) {
			String originalFilename = FilenameUtils.getBaseName(asset.getOriginalFilename()) + ".wav";
			asset.setOriginalFilename(originalFilename);
			asset.setPathname(targetFilePath);
			log.info("Updated media file path after flac->wav conversion for asset: " + asset.getId());		
		}
		else {
			log.info("No conversion is needed for asset: " + asset.getId());
		}
		
		return asset;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PreprocessServiceImpl.retrieveMediaInfo(String)
	 */
	@Override
	public String retrieveMediaInfo(String filepath) {
		String jsonpath = getMediaInfoJsonPath(filepath);
		// MediaProbe.sif is in WEB-INF/classes/MediaProbe.sif, get the absolute path
		String mediaProbe = servletContext.getRealPath("WEB-INF/classes/MediaProbe.sif");
		ProcessBuilder pb = new ProcessBuilder("singularity", "run", mediaProbe, "--json", fileStorageService.absolutePathName(filepath));
		
		/* 
		ProcessBuilder pb = new ProcessBuilder(
				amppdPropertyConfig.getPythonPath(), 
				amppdPropertyConfig.getMediaprobeDir() + "media_probe.py", 
				"--json", 
				fileStorageService.absolutePathName(filepath));

		// This is a hack to allow ProcessBuilder running on operating systems using /usr/local/bin to find ffprobe and pdfinfo for MediaProbe
		Map<String, String> env = pb.environment();
		String path = env.get("PATH");
		if (path!=null && !path.contains("/usr/local/bin")) {
			path = path.concat(":/usr/local/bin");
			env.put("PATH", path);
		}
		*/

		// merges the standard error to standard output
		pb.redirectErrorStream(true);
		
		// redirect media_probe output into json file
		// alternatively we can write the json output directly into asset, but creating the json file could be useful for other purpose
		pb.redirectOutput(new File(fileStorageService.absolutePathName(jsonpath)));				
		
		try {
			Process process = pb.start();
		    final int status = process.waitFor();
		    if (status != 0) {		    	
				// capture the error outputs into log
				log.error(fileStorageService.readTextFile(jsonpath));
		    	throw new PreprocessException("Error while retrieving media info for " + filepath + ": MediaProbe exited with status " + status);
		    }

//			// workaround for local debugging in case ffmpeg/ffprobe is not available
//			// fake a the json file by writing some string to it
//			Files.write(fileStorageService.resolve(jsonpath), "{\"tags\": \"fake media info\"}".getBytes());
		}
		catch (IOException e) {
			throw new PreprocessException("Error while retrieving media info for " + filepath, e);
		}
		catch (InterruptedException e) {
			throw new PreprocessException("Error while retrieving media info for " + filepath, e);
		}	
		
		// read the json file content into a string
		String mediaInfo = null;
		try {
			mediaInfo = fileStorageService.readTextFile(jsonpath);
		}
		catch (StorageFileNotFoundException e) {
			throw new PreprocessException("Error while reading media info from " + jsonpath + ": the json file does not exist");
		}
		
		log.info("Retrieved media info for " + filepath + " into json file " + jsonpath);
		return mediaInfo;		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PreprocessServiceImpl.retrieveMediaInfo(Asset)
	 */
	@Override	
	public Asset retrieveMediaInfo(Asset asset) {
		String mediaInfo = retrieveMediaInfo(asset.getPathname());
		if (StringUtils.isEmpty(mediaInfo)) {
			throw new PreprocessException("Error retrieving media info for Asset " + asset.getId() + ": the result is empty");
		}

		asset.setMediaInfo(mediaInfo);
		log.info("Retrieved media info for asset: " + asset.getId());
		return asset;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PreprocessService.preprocess(Asset, boolean)
	 */
	@Override	
	@Transactional	
	public Asset preprocess(Asset asset, boolean persist) {
		convertFlac(asset);
		retrieveMediaInfo(asset); 
		if (persist) {
			asset = dataentityService.saveAsset(asset); 
		}
		log.info("Successfully preprocessed asset: " + asset.getId());
		return asset;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PreprocessService.getMediaInfoJsonPath(String)
	 */
	@Override
	public String getMediaInfoJsonPath(String mediaPathname) {
		return FilenameUtils.getFullPath(mediaPathname) + FilenameUtils.getBaseName(mediaPathname) + ".json";
	}
	
}
