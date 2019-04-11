package edu.iu.dlib.amppd.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import edu.iu.dlib.amppd.exception.StorageException;
import edu.iu.dlib.amppd.exception.StorageFileNotFoundException;
import edu.iu.dlib.amppd.service.FileStorageService;

/**
 * Implementation of FileStorageService.
 * Directory hierarchy: Root - Unit - Collection - Item - Primary - Supplement
 * Naming convention: At each level of the above hierarchy, 
 * directory names abide to this format: <1st Letter of the entity class>-<ID of the entity>
 * file names abide to this format: <1st Letter of the entity class>-<ID of the entity>.<file extension>
 * @author yingfeng
 *
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

	@Value("${amppd.filesys.root:/tmp/amppd/}")
	private Path root;

	@Autowired
	public FileStorageServiceImpl() {
		try {
			Files.createDirectories(root);	// creates root directory if not already exists
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}

	public void store(MultipartFile file, String targetPathname) {
//		String filename = StringUtils.cleanPath(file.getOriginalFilename());
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file " + targetPathname);
			}
			if (targetPathname.contains("..")) {
				// This is a security check
				throw new StorageException(
						"Cannot store file with relative path outside current directory "
								+ targetPathname);
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, this.root.resolve(targetPathname),
						StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file " + targetPathname, e);
		}
	}

//	@Override
//	public Path load(String path) {
//		return root.resolve(path);
//	}
//
//	@Override
//	public Resource loadAsResource(String path) {
//		try {
//			Path file = load(path);
//			Resource resource = new UrlResource(file.toUri());
//			if (resource.exists() || resource.isReadable()) {
//				return resource;
//			}
//			else {
//				throw new StorageFileNotFoundException(
//						"Could not read file: " + path);
//
//			}
//		}
//		catch (MalformedURLException e) {
//			throw new StorageFileNotFoundException("Could not read file: " + path, e);
//		}
//	}
//
//    @Override
//    public void delete(String path) {
//        FileSystemUtils.deleteRecursively(rootLocation.toFile());
//    }

}
