package edu.iu.dlib.amppd.service;

import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for storing/retrieving files, including primary files, supplement files, as well as intermediate files.
 * @author yingfeng
 *
 */
public interface FileStorageService {

	public void store(MultipartFile sourceFile, String targetPathname);

//	public Path load(String path);
//
//	public Resource loadAsResource(String path);
//
//	public void delete(String path);
	
}
