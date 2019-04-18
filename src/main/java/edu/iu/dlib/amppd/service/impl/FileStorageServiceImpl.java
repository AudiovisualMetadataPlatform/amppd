package edu.iu.dlib.amppd.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.iu.dlib.amppd.exception.StorageException;
import edu.iu.dlib.amppd.model.Collection;
import edu.iu.dlib.amppd.model.Item;
import edu.iu.dlib.amppd.model.Primary;
import edu.iu.dlib.amppd.model.Supplement;
import edu.iu.dlib.amppd.model.SupplementOfCollection;
import edu.iu.dlib.amppd.model.SupplementOfItem;
import edu.iu.dlib.amppd.model.SupplementOfPrimary;
import edu.iu.dlib.amppd.model.Unit;
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
	private String rootPathName;

	private Path root;

	@Autowired
	public FileStorageServiceImpl() {
		try {
			root = Paths.get(rootPathName);
			Files.createDirectories(root);	// creates root directory if not already exists
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}

	/**
	 * @see edu.iu.dlib.amppd.service.FileStorageService.store(MultipartFile, String)
	 */
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
				Files.copy(inputStream, root.resolve(targetPathname),
						StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file " + targetPathname, e);
		}
	}

	/**
	 * @see edu.iu.dlib.amppd.service.FileStorageService.getDirPathName(Unit)
	 */
	public String getDirPathName(Unit unit) {
//		Path path = root.resolve("U-" + unit.getId());
//		return path.toString();		
		
		// directory path for unit: U-<unitID>
		return "U-" + unit.getId();		
	}

	/**
	 * @see edu.iu.dlib.amppd.service.FileStorageService.getDirPathName(Collection)
	 */
	public String getDirPathName(Collection collection) {
//		Path path = root.resolve("U-" + unit.getId());
//		return path.toString();		
		
		// directory path for collection: U-<unitID/C-<collectionId>
		return getDirPathName(collection.getUnit()) + File.pathSeparator + "C-" + collection.getId();
	}

	/**
	 * @see edu.iu.dlib.amppd.service.FileStorageService.getDirPathName(Item)
	 */
	public String getDirPathName(Item item) {
		// directory path for item: U-<unitID/C-<collectionId>/I-<itemId>
		return getDirPathName(item.getCollection()) + File.pathSeparator + "I-" + item.getId();
	}

	/**
	 * @see edu.iu.dlib.amppd.service.FileStorageService.getDirPathName(Primary)
	 */
	public String getDirPathName(Primary primary) {
		// directory path for primary: U-<unitID/C-<collectionId>/I-<itemId>/P--<primaryId>
		return getDirPathName(primary.getItem()) + File.pathSeparator + "P-" + primary.getId();
	}
	
	/**
	 * @see edu.iu.dlib.amppd.service.FileStorageService.getFilePathName(Primary)
	 */
	public String getFilePathName(Primary primary) {
		// file path for primary: U-<unitID/C-<collectionId>/I-<itemId>/P--<primaryId>.<primaryExtension>
		return getDirPathName(primary) + "."; // TODO add extension + primary.getExtension();
	}

	/**
	 * @see edu.iu.dlib.amppd.service.FileStorageService.getFilePathName(Supplement)
	 */
	public String getFilePathName(Supplement supplement) {
		// file name for supplement: S-<supplementId>
		String fileName = "S" + supplement.getId() + "."; // TODO add extension + primary.getExtension();
		
		// directory path for supplement depends on the parent of the supplement, could be in the directory of collection/item/primary
		String dirName = "";
		
		if (supplement instanceof SupplementOfCollection) {
			dirName = getDirPathName(supplement.getCollection());
		}
		else if (supplement instanceof SupplementOfItem) {
			dirName = getDirPathName(supplement.getItem());
		}
		else if (supplement instanceof SupplementOfPrimary) {
			dirName = getDirPathName(supplement.getPrimary());
		}
		
		return dirName + fileName;
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
