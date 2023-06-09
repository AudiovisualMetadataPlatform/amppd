package edu.indiana.dlib.amppd.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.UnitSupplement;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.projection.CollectionSupplementBrief;
import edu.indiana.dlib.amppd.model.projection.ItemBrief;
import edu.indiana.dlib.amppd.model.projection.ItemSupplementBrief;
import edu.indiana.dlib.amppd.model.projection.PrimaryfileSupplementBrief;
import edu.indiana.dlib.amppd.model.projection.SupplementBrief;
import edu.indiana.dlib.amppd.model.projection.UnitSupplementBrief;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.repository.UnitSupplementRepository;
import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.service.DropboxService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.PermissionService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for REST operations on Dataentity.
 * @author yingfeng
 */
@RestController
@Slf4j
public class DataentityController {
	
	@Autowired
	private Validator validator;
	
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
	private DataentityService dataentityService;
	
	@Autowired
	private FileStorageService fileStorageService;
	
	@Autowired
	private DropboxService dropboxService;

	@Autowired
	private PermissionService permissionService;


	/**
	 * Move the given primaryfile to the given parent item.
	 * @param itemId ID of the given item
	 * @param primaryfile the given primaryfile
	 * @param mediaFile the media file content to be uploaded for the primaryfile
	 * @return the added primaryfile
	 */
	@PostMapping(path = "/items/{itemId}/addPrimaryfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public Primaryfile addPrimaryfile(@PathVariable Long itemId, @Validated(WithoutReference.class) @RequestPart Primaryfile primaryfile, @RequestPart MultipartFile mediaFile) {		
	public Primaryfile addPrimaryfile(@PathVariable Long itemId, @RequestPart Primaryfile primaryfile, @RequestPart MultipartFile mediaFile) {		
    	log.info("Adding primaryfile " + primaryfile.getName() + " under item " + itemId);
    	
    	// populate primaryfile.item in case it's not specified in RequestPart
    	Long pid; // parent's ID
    	if (primaryfile.getItem() == null) {
    		primaryfile.setItem(itemRepository.findById(itemId).orElse(null));
    	}
    	else if ((pid = primaryfile.getItem().getId()) != itemId) {
    		log.warn("Primaryfile's item ID " + pid + " is different from the specified item ID " + itemId + ", will use the former.");    		
    	}
    	
//    	// validate with WithReference constraints, which were not invoked on primaryfile from RequestPart
//    	Set<ConstraintViolation<Primaryfile>> violations = validator.validate(primaryfile, WithReference.class);
        
    	// validate primaryfile after parent population and before persistence
    	Set<ConstraintViolation<Primaryfile>> violations = validator.validate(primaryfile);
    	if (!violations.isEmpty()) {
          throw new ConstraintViolationException(violations);
        }

    	// save primaryfile to DB 
		primaryfile = primaryfileRepository.save(primaryfile);
		
		// ingest media file after primaryfile is saved
		if (mediaFile == null) {
			throw new RuntimeException("No media file is provided for the primaryfile to be added.");
		}
		primaryfile = (Primaryfile)fileStorageService.uploadAsset(primaryfile, mediaFile);
		
    	log.info("Successfully added primaryfile " + primaryfile.getName() + " under item " + itemId);
    	return primaryfile;
    }
		
	/**
	 * Create the given unit supplement with the given media file and add it to the given parent unit.
	 * @param unitId ID of the given unit
	 * @param unitSupplement the given unit supplement
	 * @param mediaFile the media file content to be uploaded for the unit supplement
	 * @return the added unit supplement
	 */
	@PostMapping(path = "/units/{unitId}/addSupplement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public UnitSupplement addUnitSupplement(@PathVariable Long unitId, @Validated(WithoutReference.class) @RequestPart UnitSupplement unitSupplement, @RequestPart MultipartFile mediaFile) {		
	public UnitSupplement addUnitSupplement(@PathVariable Long unitId, @RequestPart UnitSupplement unitSupplement, @RequestPart MultipartFile mediaFile) {		
    	log.info("Adding unitSupplement " + unitSupplement.getName() + " under unit " + unitId);
    	
    	// populate unitSupplement.unit in case it's not specified in RequestPart
    	Long pid; // parent's ID
    	if (unitSupplement.getUnit() == null) {
    		unitSupplement.setUnit(unitRepository.findById(unitId).orElse(null));
    	}
    	else if ((pid = unitSupplement.getUnit().getId()) != unitId) {
    		log.warn("UnitSupplement's unit ID " + pid + " is different from the specified unit ID " + unitId + ", will use the former.");    		
    	}
    	
    	// validate unitSupplement after parent population and before persistence
    	Set<ConstraintViolation<UnitSupplement>> violations = validator.validate(unitSupplement);
    	if (!violations.isEmpty()) {
          throw new ConstraintViolationException(violations);
        }
    	
		// save unitSupplement to DB 
    	unitSupplement = unitSupplementRepository.save(unitSupplement);
		
		// ingest media file after unitSupplement is saved
		if (mediaFile == null) {
			throw new RuntimeException("No media file is provided for the unitSupplement to be added.");
		}
		unitSupplement = (UnitSupplement)fileStorageService.uploadAsset(unitSupplement, mediaFile);
		
    	log.info("Successfully addedunitSupplement " + unitSupplement.getName() + " under unit " + unitId);
    	return unitSupplement;
    }
	
	/**
	 * Create the given collection supplement with the given media file and add it to the given parent collection.
	 * @param collectionId ID of the given collection
	 * @param collectionSupplement the given collection supplement
	 * @param mediaFile the media file content to be uploaded for the collection supplement
	 * @return the added collection supplement
	 */
	@PostMapping(path = "/collections/{collectionId}/addSupplement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public CollectionSupplement addCollectionSupplement(@PathVariable Long collectionId, @Validated(WithoutReference.class) @RequestPart CollectionSupplement collectionSupplement, @RequestPart MultipartFile mediaFile) {		
	public CollectionSupplement addCollectionSupplement(@PathVariable Long collectionId, @RequestPart CollectionSupplement collectionSupplement, @RequestPart MultipartFile mediaFile) {		
    	log.info("Adding collectionSupplement " + collectionSupplement.getName() + " under collection " + collectionId);
    	
    	// populate collectionSupplement.collection in case it's not specified in RequestPart
    	Long pid; // parent's ID
    	if (collectionSupplement.getCollection() == null) {
    		collectionSupplement.setCollection(collectionRepository.findById(collectionId).orElse(null));
    	}
    	else if ((pid = collectionSupplement.getCollection().getId()) != collectionId) {
    		log.warn("CollectionSupplement's collection ID " + pid + " is different from the specified collection ID " + collectionId + ", will use the former.");    		
    	}
    	
    	// validate collectionSupplement after parent population and before persistence
    	Set<ConstraintViolation<CollectionSupplement>> violations = validator.validate(collectionSupplement);
    	if (!violations.isEmpty()) {
          throw new ConstraintViolationException(violations);
        }
    	
		// save collectionSupplement to DB 
    	collectionSupplement = collectionSupplementRepository.save(collectionSupplement);
		
		// ingest media file after collectionSupplement is saved
		if (mediaFile == null) {
			throw new RuntimeException("No media file is provided for the collectionSupplement to be added.");
		}
		collectionSupplement = (CollectionSupplement)fileStorageService.uploadAsset(collectionSupplement, mediaFile);
		
    	log.info("Successfully addedcollectionSupplement " + collectionSupplement.getName() + " under collection " + collectionId);
    	return collectionSupplement;
    }
	
	/**
	 * Create the given item supplement with the given media file and add it to the given parent item.
	 * @param itemId ID of the given item
	 * @param itemSupplement the given item supplement
	 * @param mediaFile the media file content to be uploaded for the item supplement
	 * @return the added item supplement
	 */
	@PostMapping(path = "/items/{itemId}/addSupplement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public ItemSupplement addItemSupplement(@PathVariable Long itemId, @Validated(WithoutReference.class) @RequestPart ItemSupplement itemSupplement, @RequestPart MultipartFile mediaFile) {		
	public ItemSupplement addItemSupplement(@PathVariable Long itemId, @RequestPart ItemSupplement itemSupplement, @RequestPart MultipartFile mediaFile) {		
    	log.info("Adding itemSupplement " + itemSupplement.getName() + " under item " + itemId);
    	
    	// populate itemSupplement.item in case it's not specified in RequestPart
    	Long pid; // parent's ID
    	if (itemSupplement.getItem() == null) {
    		Item item = itemRepository.findById(itemId).orElseThrow(() -> new StorageException("item <" + itemId + "> does not exist!"));
    		itemSupplement.setItem(item);
    	}
    	else if ((pid = itemSupplement.getItem().getId()) != itemId) {
    		log.warn("ItemSupplement's item ID " + pid + " is different from the specified item ID " + itemId + ", will use the former.");    		
    	}
    	
    	// validate itemSupplement after parent population and before persistence
    	Set<ConstraintViolation<ItemSupplement>> violations = validator.validate(itemSupplement);
    	if (!violations.isEmpty()) {
          throw new ConstraintViolationException(violations);
        }
    	    	
		// save itemSupplement to DB 
    	itemSupplement = itemSupplementRepository.save(itemSupplement);
		
		// ingest media file after itemSupplement is saved
		if (mediaFile == null) {
			throw new RuntimeException("No media file is provided for the itemSupplement to be added.");
		}
		itemSupplement = (ItemSupplement)fileStorageService.uploadAsset(itemSupplement, mediaFile);
		
    	log.info("Successfully addeditemSupplement " + itemSupplement.getName() + " under item " + itemId);
    	return itemSupplement;
    }
	
	/**
	 * Create the given primaryfile supplement with the given media file and add it to the given parent primaryfile.
	 * @param primaryfileId ID of the given primaryfile
	 * @param primaryfileSupplement the given primaryfile supplement
	 * @param mediaFile the media file content to be uploaded for the primaryfile supplement
	 * @return the added primaryfile supplement
	 */
	@PostMapping(path = "/primaryfiles/{primaryfileId}/addSupplement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public PrimaryfileSupplement addPrimaryfileSupplement(@PathVariable Long primaryfileId, @Validated(WithoutReference.class) @RequestPart PrimaryfileSupplement primaryfileSupplement, @RequestPart MultipartFile mediaFile) {		
	public PrimaryfileSupplement addPrimaryfileSupplement(@PathVariable Long primaryfileId, @RequestPart PrimaryfileSupplement primaryfileSupplement, @RequestPart MultipartFile mediaFile) {		
    	log.info("Adding primaryfileSupplement " + primaryfileSupplement.getName() + " under primaryfile " + primaryfileId);
    	
    	// populate primaryfileSupplement.primaryfile in case it's not specified in RequestPart
    	Long pid; // parent's ID
    	if (primaryfileSupplement.getPrimaryfile() == null) {
    		Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("primaryfile <" + primaryfileId + "> does not exist!"));
    		primaryfileSupplement.setPrimaryfile(primaryfile);
    	}
    	else if ((pid = primaryfileSupplement.getPrimaryfile().getId()) != primaryfileId) {
    		log.warn("PrimaryfileSupplement's primaryfile ID " + pid + " is different from the specified primaryfile ID " + primaryfileId + ", will use the former.");    		
    	}
    	
    	// validate primaryfileSupplement after parent population and before persistence
    	Set<ConstraintViolation<PrimaryfileSupplement>> violations = validator.validate(primaryfileSupplement);
    	if (!violations.isEmpty()) {
          throw new ConstraintViolationException(violations);
        }
    	    	
		// save primaryfileSupplement to DB 
    	primaryfileSupplement = primaryfileSupplementRepository.save(primaryfileSupplement);
		
		// ingest media file after primaryfileSupplement is saved
		if (mediaFile == null) {
			throw new RuntimeException("No media file is provided for the primaryfileSupplement to be added.");
		}
		primaryfileSupplement = (PrimaryfileSupplement)fileStorageService.uploadAsset(primaryfileSupplement, mediaFile);
		
    	log.info("Successfully addedprimaryfileSupplement " + primaryfileSupplement.getName() + " under primaryfile " + primaryfileId);
    	return primaryfileSupplement;
    }	
	
	/* TODO
	 * The moveCollection/Item/Primaryfile methods may not be needed, as the only actions needed are validation
	 * and moving entityDir/dropbox/asset, all of which can be done in the handlers' update methods.
	 * The caviar is that we need to resolve the issues in FileStorageService.moveEntityDir and DropboxSerice.renameSubdir,
	 * which relates to the issue in DateentityService.findOriginalDataentity.
	 */
	
	/**
	 * Move the given collection to the given parent unit if different from its original parent.
	 * @param collectionId ID of the given collection
	 * @param unitId ID of the given parent unit
	 * @return the updated collection
	 */
	@PostMapping(path = "/collections/{collectionId}/move")
	public Collection moveCollection(@PathVariable Long collectionId, @RequestParam Long unitId) {		
    	log.info("Moving collection " + collectionId + " to new unit " + unitId);
    	
    	// retrieve collection and unit from DB
    	Collection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new StorageException("Collection <" + collectionId + "> does not exist!"));
    	Unit unit =	unitRepository.findById(unitId).orElseThrow(() -> new StorageException("Unit <" + unitId + "> does not exist!"));

    	// if parent unit is the same, no action
    	if (collection.getUnit().getId().equals(unit.getId())) {
    		log.warn("No need to move collection " + collectionId + " to the same parent unit " + unitId);
    		return collection;
    	}    	
    	
    	// otherwise, record old parent unit and update parent to new unit 
    	Unit oldUnit = collection.getUnit();
    	collection.setUnit(unit);
    	
    	// validate collection uniqueness under new parent before further actions
    	Set<ConstraintViolation<Collection>> violations = validator.validate(collection);
    	if (!violations.isEmpty()) {
          throw new ConstraintViolationException(violations);
        }
    	
    	// if valid, reset parent back to the old one to facilitate moving entity dir
    	collection.setUnit(oldUnit);
    	
    	// move collection media subdir (if exist) to new subdir
    	// note that this operation updates collection's parent unit
    	fileStorageService.moveEntityDir(collection, unit);
    	
    	// reset collection's parent unit back to old value, so that we can repeat similar procedure for dropbox
    	collection.setUnit(oldUnit);

        // move the dropbox subdir and update its parent unit if changed
        dropboxService.moveSubdir(collection, unit); 

        // persist updated collection
        collectionRepository.save(collection);
        
        log.info("Successfully moved collection " + collectionId + " to new unit " + unitId);
        return collection;
    }	
	
	/**
	 * Move the given item to the given parent collection if different from its original parent.
	 * @param itemId ID of the given item
	 * @param collectionId ID of the given parent collection
	 * @return the updated item
	 */
	@PostMapping(path = "/items/{itemId}/move")
	public Item moveItem(@PathVariable Long itemId, @RequestParam Long collectionId) {		
    	log.info("Moving item " + itemId + " to new collection " + collectionId);
    	
    	// retrieve item and collection from DB
    	Item item = itemRepository.findById(itemId).orElseThrow(() -> new StorageException("Item <" + itemId + "> does not exist!"));
    	Collection collection =	collectionRepository.findById(collectionId).orElseThrow(() -> new StorageException("Collection <" + collectionId + "> does not exist!"));

    	// if parent collection is the same, no action
    	if (item.getCollection().getId().equals(collection.getId())) {
    		log.warn("No need to move item " + itemId + " to the same parent collection " + collectionId);
    		return item;
    	}    	

    	// otherwise, record old parent collection and update parent to new collection 
    	Collection oldCollection = item.getCollection();
    	item.setCollection(collection);
    	
    	// validate item uniqueness under new parent before further actions
    	Set<ConstraintViolation<Item>> violations = validator.validate(item);
    	if (!violations.isEmpty()) {
          throw new ConstraintViolationException(violations);
        }
    	
    	// if valid, reset parent back to the old one to facilitate moving entity dir
    	item.setCollection(oldCollection);
    	
    	// move item media subdir (if exists) to new subdir and update/save its parent collection
    	fileStorageService.moveEntityDir(item, collection);
    	itemRepository.save(item);

    	log.info("Successfully moved item " + itemId + " to new collection " + collectionId);
        return item;
    }	
	
	/**
	 * Move the given primaryfile to the given parent item if different from its original parent.
	 * @param primaryfileId ID of the given primaryfile
	 * @param itemId ID of the given parent item
	 * @return the updated primaryfile
	 */
	@PostMapping(path = "/primaryfiles/{primaryfileId}/move")
	public Primaryfile movePrimaryfile(@PathVariable Long primaryfileId, @RequestParam Long itemId) {		
    	log.info("Moving primaryfile " + primaryfileId + " to new item " + itemId);
    	
    	// retrieve primaryfile and item from DB
    	Primaryfile	primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfileId + "> does not exist!"));
    	Item item =	itemRepository.findById(itemId).orElseThrow(() -> new StorageException("Item <" + itemId + "> does not exist!"));

    	// if parent item is the same, no action
    	if (primaryfile.getItem().getId().equals(item.getId())) {
    		log.warn("No need to move primaryfile " + primaryfileId + " to the same parent item " + itemId);
    		return primaryfile;
    	}    	

    	// otherwise, record old parent item and update parent to new item 
    	Item oldItem = primaryfile.getItem();
    	primaryfile.setItem(item);
    	
    	// validate primaryfile uniqueness under new parent before further actions
    	Set<ConstraintViolation<Primaryfile>> violations = validator.validate(primaryfile);
    	if (!violations.isEmpty()) {
          throw new ConstraintViolationException(violations);
        }
    	
    	// if valid, reset parent back to the old one to facilitate moving entity dir
    	primaryfile.setItem(oldItem);

    	// move primaryfile media subdir (if exists) and media/info files to new subdir and update its parent item and save;
    	// note that moveEntityDir should be done before moveAsset; otherwise the former won't work properly 
    	// as the original parent would have been lost after primaryfile is saved by the latter
    	fileStorageService.moveEntityDir(primaryfile, oldItem);
        fileStorageService.moveAsset(primaryfile, true);

    	log.info("Successfully moved primaryfile " + primaryfileId + " to new item " + itemId);
        return primaryfile;
    }
			
	/**
	 * Move the given unitSupplement to the given parent dataentity if different from its original parent.
	 * @param unitSupplementId ID of the unitSupplement to be moved
	 * @param parentId ID of the given parent dataentity
	 * @return the updated supplement
	 */
	@PostMapping(path = "/unitSupplements/{unitSupplementId}/move")
	public Supplement moveUnitSupplement(
			@PathVariable Long unitSupplementId, 
			@RequestParam Long parentId, 
			@RequestParam(required = false) String parentType) {		
    	log.info("Moving unitSupplement " + unitSupplementId + " to new parent " + parentType + " " + parentId);
    	Supplement supplement = dataentityService.moveSupplement(unitSupplementId, SupplementType.UNIT, parentId, parentType);    	
    	log.info("Successfully moved unitSupplement " + unitSupplementId + " to new parent " + parentType + " "  + parentId);
        return supplement;
    }
			
	/**
	 * Move the given collectionSupplement to the given parent dataentity if different from its original parent.
	 * @param collectionSupplementId ID of the collectionSupplement to be moved
	 * @param parentId ID of the given parent dataentity
	 * @return the updated supplement
	 */
	@PostMapping(path = "/collectionSupplements/{collectionSupplementId}/move")
	public Supplement moveCollectionSupplement(
			@PathVariable Long collectionSupplementId, 
			@RequestParam Long parentId, 
			@RequestParam(required = false) String parentType) {		
    	log.info("Moving collectionSupplement " + collectionSupplementId + " to new parent " + parentType + " " + parentId);
    	Supplement supplement = dataentityService.moveSupplement(collectionSupplementId, SupplementType.COLLECTION, parentId, parentType);    	
    	log.info("Successfully moved collectionSupplement " + collectionSupplementId + " to new parent " + parentType + " "  + parentId);
        return supplement;
    }
					
	/**
	 * Move the given itemSupplement to the given parent dataentity if different from its original parent.
	 * @param itemSupplementId ID of the itemSupplement to be moved
	 * @param parentId ID of the given parent dataentity
	 * @return the updated supplement
	 */
	@PostMapping(path = "/itemSupplements/{itemSupplementId}/move")
	public Supplement moveItemSupplement(
			@PathVariable Long itemSupplementId, 
			@RequestParam Long parentId, 
			@RequestParam(required = false) String parentType) {		
    	log.info("Moving itemSupplement " + itemSupplementId + " to new parent " + parentType + " " + parentId);
    	Supplement supplement = dataentityService.moveSupplement(itemSupplementId, SupplementType.ITEM, parentId, parentType);    	
    	log.info("Successfully moved itemSupplement " + itemSupplementId + " to new parent " + parentType + " "  + parentId);
        return supplement;
    }
					
	/**
	 * Move the given primaryfileSupplement to the given parent dataentity if different from its original parent.
	 * @param primaryfileSupplementId ID of the primaryfileSupplement to be moved
	 * @param parentId ID of the given parent dataentity
	 * @return the updated supplement
	 */
	@PostMapping(path = "/primaryfileSupplements/{primaryfileSupplementId}/move")
	public Supplement movePrimaryfileSupplement(
			@PathVariable Long primaryfileSupplementId, 
			@RequestParam Long parentId, 
			@RequestParam(required = false) String parentType) {		
    	log.info("Moving primaryfileSupplement " + primaryfileSupplementId + " to new parent " + parentType + " " + parentId);
    	Supplement supplement = dataentityService.moveSupplement(primaryfileSupplementId, SupplementType.PRIMARYFILE, parentId, parentType);    	
    	log.info("Successfully moved primaryfileSupplement " + primaryfileSupplementId + " to new parent " + parentType + " "  + parentId);
        return supplement;
    }
			
	/**
	 * Get all supplements associated with the primaryfiles at all parent levels, with the given supplement name, category and format.
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @param name name of the supplement
	 * @param category category of the supplement
	 * @param format format of the supplement
	 * @return list of all supplements satisfying the criteria for each primaryfile
	 */
	@GetMapping(path = "/primaryfiles/supplements")
	public List<List<Supplement>> getSupplementsForPrimaryfiles(
			@RequestParam Long[] primaryfileIds, 
			@RequestParam(required = false) String name, 
			@RequestParam String category, 
			@RequestParam String format) {
		log.info("Retrieving supplements for primaryfiles " + primaryfileIds + ", name: " + name + ", category: " + category + ", format: " + format);
		return dataentityService.getSupplementsForPrimaryfiles(primaryfileIds, name, category, format);
	}
		
	/**
	 * Get all supplements at all levels, subject to authorization.
	 * @return list of supplements
	 */
	@GetMapping(path = "/supplements")
	public List<SupplementBrief> getSupplements() {
		// get accessible units for Read WorkflowResult, if none, access deny exception will be thrown
		Set<Long> acUnitIds = permissionService.getAccessibleUnits(ActionType.Read, TargetType.Supplement);

		// otherwise if acUnitIds is null, i.e. user is admin, then no AC prefilter is needed;  
		// otherwise apply AC prefilter to query criteria	

		List<UnitSupplementBrief> unitSupplements = acUnitIds == null ?
				unitSupplementRepository.findBy() :
				unitSupplementRepository.findByUnitIdIn(acUnitIds);
		List<CollectionSupplementBrief> collectionSupplements = acUnitIds == null ?
				collectionSupplementRepository.findBy() :
				collectionSupplementRepository.findByCollectionUnitIdIn(acUnitIds);
		List<ItemSupplementBrief> itemSupplements = acUnitIds == null ?
				itemSupplementRepository.findBy() :
				itemSupplementRepository.findByItemCollectionUnitIdIn(acUnitIds);	
		List<PrimaryfileSupplementBrief> primaryfileSupplements = acUnitIds == null ?
				primaryfileSupplementRepository.findBy() :
				primaryfileSupplementRepository.findByPrimaryfileItemCollectionUnitIdIn(acUnitIds);	

		List<SupplementBrief> supplements = new ArrayList<SupplementBrief>();
		supplements.addAll(unitSupplements);
		supplements.addAll(collectionSupplements);
		supplements.addAll(itemSupplements);
		supplements.addAll(primaryfileSupplements);
		
		log.info("Successfully retrieved " + supplements.size() + " supplements.");
		return supplements;
	}
	
	/**
	 * 
	 */
	@GetMapping(path = "/items/search")
	public List<ItemBrief> findItems(String keyword) {
	
}
