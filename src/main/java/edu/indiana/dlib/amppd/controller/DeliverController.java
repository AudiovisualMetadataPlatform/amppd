package edu.indiana.dlib.amppd.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.dto.AvalonRelatedItems;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.MgmEvaluationTestRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.repository.UnitSupplementRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.service.DeliverService;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.web.DataentityStatistics;
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
	private UnitRepository unitRepository;
	
	@Autowired
	private CollectionRepository collectionRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
	private UnitSupplementRepository unitSupplementRepository;

	@Autowired
	private CollectionSupplementRepository collectionSupplementRepository;

	@Autowired
	private ItemSupplementRepository itemSupplementRepository;
	
	@Autowired
	private PrimaryfileSupplementRepository primaryfileSupplementRepository;

	@Autowired
	private WorkflowResultRepository workflowResultRepository;

	@Autowired
	private MgmEvaluationTestRepository mgmEvaluationTestRepository;

	@Autowired
	private DeliverService deliverService;
	
	@Autowired
	private PermissionService permissionService;
	
	
	/**
	 * Get the DataentityStatistics about all children within the given unit.
	 * @param id ID of the given unit
	 * @return the DataentityStatistics for the given unit
	 */
	@GetMapping(path = "/units/{id}/statistics")
	public DataentityStatistics getUnitStatistics(@PathVariable Long id) {
		// check permission
		// Note: getting DataentityStatistics for an entity requires the Read permission on that entity
		Long acUnitId = id;
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Unit, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot retrieve unit statistics in unit " + acUnitId);
		}

		Integer countCollections = collectionRepository.countByUnitId(id);
		Integer countItems = itemRepository.countByCollectionUnitId(id);
		Integer countPrimaryfiles = primaryfileRepository.countByItemCollectionUnitId(id);
		Integer countUnitSupplements = unitSupplementRepository.countByUnitId(id);
		Integer countCollectionSupplements = collectionSupplementRepository.countByCollectionUnitId(id);
		Integer countItemSupplements = itemSupplementRepository.countByItemCollectionUnitId(id);
		Integer countPrimaryfileSupplements = primaryfileSupplementRepository.countByPrimaryfileItemCollectionUnitId(id);
		Integer countWorkflowResults = workflowResultRepository.countByUnitId(id);
		Integer countMgmEvaluationTests = mgmEvaluationTestRepository.countByUnitId(id);
		
		DataentityStatistics statistics = new DataentityStatistics(
				countCollections, countItems, countPrimaryfiles, 
				countUnitSupplements, countCollectionSupplements, countItemSupplements, countPrimaryfileSupplements,
				countWorkflowResults, countMgmEvaluationTests); 
		
		log.info("Successfully retrieved content statistics for unit " + id + ": " + statistics);
		return statistics;
	}
	
	/**
	 * Get the DataentityStatistics about all children within the given collection.
	 * @param id ID of the given collection
	 * @return the DataentityStatistics for the given collection
	 */
	@GetMapping(path = "/collections/{id}/statistics")
	public DataentityStatistics getCollectionStatistics(@PathVariable Long id) {
		// check permission
		// Note: getting DataentityStatistics for an entity requires the Read permission on that entity
		Collection collection = collectionRepository.findById(id).orElseThrow(() -> new StorageException("collection <" + id + "> does not exist!"));    		
		Long acUnitId = collection.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Collection, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot retrieve collection statistics in unit " + acUnitId);
		}

		Integer countItems = itemRepository.countByCollectionId(id);
		Integer countPrimaryfiles = primaryfileRepository.countByItemCollectionId(id);
		Integer countCollectionSupplements = collectionSupplementRepository.countByCollectionId(id);
		Integer countItemSupplements = itemSupplementRepository.countByItemCollectionId(id);
		Integer countPrimaryfileSupplements = primaryfileSupplementRepository.countByPrimaryfileItemCollectionId(id);
		Integer countWorkflowResults = workflowResultRepository.countByCollectionId(id);
		Integer countMgmEvaluationTests = mgmEvaluationTestRepository.countByCollectionId(id);
		
		DataentityStatistics statistics = new DataentityStatistics(
				null, countItems, countPrimaryfiles, 
				null, countCollectionSupplements, countItemSupplements, countPrimaryfileSupplements,
				countWorkflowResults, countMgmEvaluationTests); 
		
		log.info("Successfully retrieved content statistics for collection " + id + ": " + statistics);
		return statistics;
	}
	
	/**
	 * Get the DataentityStatistics about all children within the given item.
	 * @param id ID of the given item
	 * @return the DataentityStatistics for the given item
	 */
	@GetMapping(path = "/items/{id}/statistics")
	public DataentityStatistics getItemStatistics(@PathVariable Long id) {
		// check permission
		// Note: getting DataentityStatistics for an entity requires the Read permission on that entity
		Item item = itemRepository.findById(id).orElseThrow(() -> new StorageException("item <" + id + "> does not exist!"));    		
		Long acUnitId = item.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Item, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot retrieve item statistics in unit " + acUnitId);
		}

		Integer countPrimaryfiles = primaryfileRepository.countByItemId(id);
		Integer countItemSupplements = itemSupplementRepository.countByItemId(id);
		Integer countPrimaryfileSupplements = primaryfileSupplementRepository.countByPrimaryfileItemId(id);
		Integer countWorkflowResults = workflowResultRepository.countByItemId(id);
		Integer countMgmEvaluationTests = mgmEvaluationTestRepository.countByItemId(id);
		
		DataentityStatistics statistics = new DataentityStatistics(
				null, null, countPrimaryfiles, 
				null, null, countItemSupplements, countPrimaryfileSupplements,
				countWorkflowResults, countMgmEvaluationTests); 
		
		log.info("Successfully retrieved content statistics for item " + id + ": " + statistics);
		return statistics;
	}
	
	/**
	 * Get the DataentityStatistics about all children within the given primaryfile.
	 * @param id ID of the given primaryfile
	 * @return the DataentityStatistics for the given primaryfile
	 */
	@GetMapping(path = "/primaryfiles/{id}/statistics")
	public DataentityStatistics getPrimaryfileStatistics(@PathVariable Long id) {
		// check permission
		// Note: getting DataentityStatistics for an entity requires the Read permission on that entity
		Primaryfile primaryfile = primaryfileRepository.findById(id).orElseThrow(() -> new StorageException("primaryfile <" + id + "> does not exist!"));    		
		Long acUnitId = primaryfile.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Primaryfile, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot retrieve primaryfile statistics in unit " + acUnitId);
		}

		Integer countPrimaryfileSupplements = primaryfileSupplementRepository.countByPrimaryfileId(id);
		Integer countWorkflowResults = workflowResultRepository.countByPrimaryfileId(id);
		Integer countMgmEvaluationTests = mgmEvaluationTestRepository.countByPrimaryfileId(id);
		
		DataentityStatistics statistics = new DataentityStatistics(
				null, null, null, 
				null, null, null, countPrimaryfileSupplements,
				countWorkflowResults, countMgmEvaluationTests); 
		
		log.info("Successfully retrieved content statistics for primaryfile " + id + ": " + statistics);
		return statistics;
	}
	
	/**
	 * Get the DataentityStatistics about all children within the given primaryfileSupplement.
	 * @param id ID of the given primaryfileSupplement
	 * @return the DataentityStatistics for the given primaryfileSupplement
	 */
	@GetMapping(path = "/primaryfileSupplements/{id}/statistics")
	public DataentityStatistics getPrimaryfileSupplementStatistics(@PathVariable Long id) {
		// check permission
		// Note: getting DataentityStatistics for an entity requires the Read permission on that entity
		PrimaryfileSupplement primaryfileSupplement = primaryfileSupplementRepository.findById(id).orElseThrow(() -> new StorageException("primaryfile <" + id + "> does not exist!"));    		
		Long acUnitId = primaryfileSupplement.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Supplement, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot retrieve groundtruth statistics in unit " + acUnitId);
		}

		Integer countMgmEvaluationTests = mgmEvaluationTestRepository.countByGroundtruthSupplementId(id);
		
		DataentityStatistics statistics = new DataentityStatistics(
				null, null, null, 
				null, null, null, null,
				null, countMgmEvaluationTests); 
		
		log.info("Successfully retrieved content statistics for groundtruth " + id + ": " + statistics);
		return statistics;
	}
	
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
	 * Deliver results within the specified collection IDs and output types to Clio,
	 * by creating symlinks to the output files in the export directory.
	 */
	@PostMapping(path = "/deliver/clio", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> deliverClioCollections(@RequestParam List<Long> collectionIds, @RequestParam List<String> outputTypes) {
		List<String> exports = new ArrayList<String>();

		// only retrieve successful result
		List<WorkflowResult> results = workflowResultRepository.findByCollectionIdInAndOutputTypeInAndStatusEqualsOrderByDateCreatedDesc(collectionIds, outputTypes, GalaxyJobState.COMPLETE);
		
		// export root dir
		Path exportDir = Paths.get(config.getDataRoot() + File.separator + "export");
		
		// create export symlink for each result
		for (WorkflowResult result : results) {
			// make sure the output file exists
			Path path = Paths.get(result.getOutputPath());
			if (!Files.exists(path)) {
				throw new StorageException("Can't export output for result " + result.getId() + ": the file " + path + " doesn't exist");	
			}
			
			// make sure the collection export subdir is created
			Path coldir = exportDir.resolve(result.getCollectionName());
			try {
				Files.createDirectories(coldir);
			}
			catch (IOException e) {
				throw new RuntimeException("Error creating collection export subdir " + coldir, e);		    	
			}
			
			// if getExternalId is empty give warning and use item id instead
			String id = result.getExternalId();
			if (StringUtils.isEmpty(id)) {
				id = result.getItemId().toString();
			}
			
			// use externalId-primaryfileName.type as the target symlink name
			String symlink = id + "-" + result.getPrimaryfileName() + "." + result.getOutputType();
			Path link = coldir.resolve(symlink);
						
			// if export symlink already created for this output, skip it
			if (Files.exists(link)) {
				log.warn("Export symlink " + link + " for result " + result.getId() + " already exists, will not create new one for it.");
				continue;
			}
			
			// otherwise, create the export symbolic link for the output file 
			try {
				Files.createSymbolicLink(link, path);
				log.debug("Created export symlink " + link + " for result " + result.getId());
			}
			catch (IOException e) {
				throw new RuntimeException("Error creating export symlink " + link + " for result " + result.getId(), e);		    	
			}
			
			exports.add(symlink);
		}
		
		log.info("Successfully created " + exports.size() + " export symlinks for " + results.size() + " workflowResults");
		return exports;
	}
	
}