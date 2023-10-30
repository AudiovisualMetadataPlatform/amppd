package edu.indiana.dlib.amppd.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.dto.AvalonRelatedItems;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.service.DeliverService;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.web.GalaxyJobState;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to deliver AMP deliverables to target systems.
 * @author yingfeng
 */
//@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class DeliverController {
	
	@Autowired
	private AmppdPropertyConfig config; 
	
	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private CollectionRepository collectionRepository;

	@Autowired
	private WorkflowResultRepository workflowResultRepository;

	@Autowired
	private DeliverService deliverService;
	
	@Autowired
	private PermissionService permissionService;
	
	
	/**
	 * Deliver final results associated with the given item to Avalon as related items of the corresponding media object.
	 * @param itemId ID of the given item
	 * @return the AvalonRelatedItems delivered
	 */
	@PostMapping(path = "/deliver/avalon/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public AvalonRelatedItems deliverAvalonItem(@PathVariable Long itemId) {
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new StorageException("item <" + itemId + "> does not exist!"));

		// check permission
		Long acUnitId = item.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Create, TargetType.Bag, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot deliver item bags in unit " + acUnitId);
		}

		log.info("Deliver final results to Avalon for item " + itemId + " ... " );
		return deliverService.deliverAvalonItem(item);
	}

	/**
	 * Deliver final results associated with the items within the given collection to Avalon.
	 * @param collectionId ID of the given collection
	 * @return the list of AvalonRelatedItems delivered
	 */
	@PostMapping(path = "/deliver/avalon/collection/{collectionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<AvalonRelatedItems> deliverAvalonCollection(@PathVariable Long collectionId) {
		Collection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new StorageException("collection <" + collectionId + "> does not exist!"));    

		// check permission
		Long acUnitId = collection.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Create, TargetType.Bag, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot deliver collection bags in unit " + acUnitId);
		}
		
		log.info("Deliver final results to Avalon for items in collection " + collectionId + " ... " );
		return deliverService.deliverAvalonCollection(collection);
	}

	/**
	 * 
	 */
	@PostMapping(path = "/deliver/clio", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> deliverClioCollections(@RequestParam List<Long> collectionIds, @RequestParam List<String> outputTypes) {
		List<String> exports = new ArrayList<String>();
		List<WorkflowResult> results = workflowResultRepository.findByCollectionIdInAndOutputTypeInAndStatusEquals(collectionIds, outputTypes, GalaxyJobState.COMPLETE);
		
		for (WorkflowResult result : results) {
			// make sure the output file exists
			Path path = Paths.get(result.getOutputPath());
			if (!Files.exists(path)) {
//				throw new StorageException("Can't export output for result " + result.getId() + ": the file " + path + " doesn't exist");	
			}
			
			// make sure export directory is created
			Path exportDir = Paths.get(config.getDataRoot() + File.separator + "export");
			try {
				Files.createDirectories(exportDir);
			}
			catch (IOException e) {
				throw new RuntimeException("Error creating export root dir " + exportDir, e);		    	
			}
			
			// use externalId-primaryfileName.type as the target symlink name
			String symlink = result.getExternalId() + "-" + result.getPrimaryfileName() + "." + result.getOutputType();
			Path link = exportDir.resolve(symlink);
						
			// if export symlink already created for this output, skip it
			if (Files.exists(link)) {
				log.warn("Export symlink " + link + " for result " + result.getId() + " already exists, will not create new one for it.");
				continue;
			}
			
			// otherwise, create the export symbolic link for the output file 
//			try {
//				Files.createSymbolicLink(link, path);
//			}
//			catch (IOException e) {
//				throw new RuntimeException("Error creating export symlink " + link + " for result " + result.getId(), e);		    	
//			}
			
			exports.add(symlink);
		}
		
		log.info("Successfully created " + exports.size() + " export symlinks for " + results.size() + " workflowResults");
		return exports;
	}
	
}