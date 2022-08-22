package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.UnitSupplement;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.repository.UnitSupplementRepository;
import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.MediaService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of DataentityService.
 * @author yingfeng
 */
@Service
@Slf4j
public class DataentityServiceImpl implements DataentityService {
	
	@Autowired
	private AmppdPropertyConfig amppdPropertyConfig;
	
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
	private FileStorageService fileStorageService;

	@Autowired
	private MediaService mediaService;

	//	@Autowired
//	private EntityManager entityManager;
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getDataentityUrl(Dataentity)
	 */
	@Override
	public String getDataentityUrl(Dataentity dataentity) {
		String url = "";
		String destr = "";
		
		if (dataentity == null) {
			return url;
		}		
		else if (dataentity instanceof Unit) {
			destr = "units";
		}
		else if (dataentity instanceof Collection) {
			destr = "collections";
		}
		else if (dataentity instanceof Item) {
			destr = "items";
		}
		else if (dataentity instanceof Primaryfile) {
			destr = "primaryfiles";
		}
		else if (dataentity instanceof UnitSupplement) {
			destr = "unitSupplements";
		}
		else if (dataentity instanceof CollectionSupplement) {
			destr = "collectionSupplements";
		}
		else if (dataentity instanceof ItemSupplement) {
			destr = "itemSupplements";
		}
		else if (dataentity instanceof PrimaryfileSupplement) {
			destr = "primaryfileSupplements";
		}

		url = amppdPropertyConfig.getUrl() + "/" + destr + "/" + dataentity.getId();
		return url;
	}
		
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getParentDataentity(Dataentity)
	 */
	@Override
	public Dataentity getParentDataentity(Dataentity dataentity) {		
		if (dataentity == null) {
			throw new IllegalArgumentException("Failed to get dataentity's parent: the provided dataentity is null.");
		}

		// only handle entities allowing parent
		if (dataentity instanceof Collection) {
			return ((Collection)dataentity).getUnit();
		}
		else if (dataentity instanceof Item) {
			return ((Item)dataentity).getCollection();
		}
		else if (dataentity instanceof Primaryfile) {
			return ((Primaryfile)dataentity).getItem();
		}
		else if (dataentity instanceof UnitSupplement) {
			return ((UnitSupplement)dataentity).getUnit();
		}		
		else if (dataentity instanceof CollectionSupplement) {
			return ((CollectionSupplement)dataentity).getCollection();
		}		
		else if (dataentity instanceof ItemSupplement) {
			return ((ItemSupplement)dataentity).getItem();
		}		
		else if (dataentity instanceof PrimaryfileSupplement) {
			return ((PrimaryfileSupplement)dataentity).getPrimaryfile();
		}
		else {
			throw new IllegalArgumentException("Failed to get dataentity's parent: the provided dataentity " + dataentity.getId() + " is of invalid type.");
		}
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.setParentDataentity(Dataentity, Dataentity)
	 */
	@Override
	public void setParentDataentity(Dataentity dataentity, Dataentity parent) {		
		if (dataentity == null) {
			throw new IllegalArgumentException("Failed to set dataentity's parent: the provided dataentity is null.");
		}
		if (parent == null) {
			throw new IllegalArgumentException("Failed to set dataentity's parent: the provided parent is null.");
		}

		// only handle entities allowing parent
		try {
			if (dataentity instanceof Collection) {
				((Collection)dataentity).setUnit((Unit)parent);
			}
			else if (dataentity instanceof Item) {
				((Item)dataentity).setCollection((Collection)parent);
			}
			else if (dataentity instanceof Primaryfile) {
				((Primaryfile)dataentity).setItem((Item)parent);
			}
			else if (dataentity instanceof UnitSupplement) {
				((UnitSupplement)dataentity).setUnit((Unit)parent);
			}		
			else if (dataentity instanceof CollectionSupplement) {
				((CollectionSupplement)dataentity).setCollection((Collection)parent);
			}		
			else if (dataentity instanceof ItemSupplement) {
				((ItemSupplement)dataentity).setItem((Item)parent);
			}		
			else if (dataentity instanceof PrimaryfileSupplement) {
				((PrimaryfileSupplement)dataentity).setPrimaryfile((Primaryfile)parent);
			}
			else {
				throw new IllegalArgumentException("Failed to set dataentity's parent: the provided dataentity " + dataentity.getId() + " is of invalid type.");
			}
		}
		catch(ClassCastException e) {
			throw new IllegalArgumentException("Failed to set dataentity's parent: the provided parent " + parent.getId() + " is of invalid type.");			
		}
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.findParentDataEntity(Long, String)
	 */
	@Override
	public Dataentity findParentDataEntity(Long id, SupplementType type) {
		switch (type) {
		case PFILE:
			throw new StorageException("Failed to get parent dateentity: the provided type " + type + " is invalid.");
		case PRIMARYFILE:
			return primaryfileRepository.findById(id).orElseThrow(() -> new StorageException("Primaryfile <" + id + "> does not exist!"));        	
		case ITEM:
			return itemRepository.findById(id).orElseThrow(() -> new StorageException("Item <" + id + "> does not exist!"));        	
		case COLLECTION:
			return collectionRepository.findById(id).orElseThrow(() -> new StorageException("Collection <" + id + "> does not exist!"));        	
		case UNIT:
			return unitRepository.findById(id).orElseThrow(() -> new StorageException("Unit <" + id + "> does not exist!"));        	
		}
		return null;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.findOriginalDataentity(Dataentity)
	 */
	@Override
	public Dataentity findOriginalDataentity(Dataentity dataentity) {
		if (dataentity == null) {
			throw new IllegalArgumentException("Failed to find dataentity: the provided dataentity is null.");
		}
		
		Long id = dataentity.getId();		
		if (id == null) {
			throw new IllegalArgumentException("Failed to find dataentity: the provided dataentity ID is null.");				
		}

		/* TODO Note:
		 * Repository.find*** will return a cached object if previously retrieved within the same session;
		 * a new object will be retrieved from DB only after a commit (save) on the object happens.
		 * This means that once the retrieved object has fields updated, it's impossible to get the original values from DB.
		 * The only way to get around this is to call entityManager.clear() or session.refresh(object) with customized repositories.
		 * This method currently doesn't work as expected, thus the callers which rely on it doesn't fully work either
		 * (for ex, methods called by entity repository handlers to move dropbox/media subdirs). 
		 */		
//		entityManager.refresh(dataentity);

		// only handle non-supplement types
		if (dataentity instanceof Unit) {
			return unitRepository.findById(id).orElseThrow(() -> new StorageException("Unit <" + id + "> does not exist!"));
		}
		else if (dataentity instanceof Collection) {
			return collectionRepository.findById(id).orElseThrow(() -> new StorageException("Collection <" + id + "> does not exist!"));
		}
		else if (dataentity instanceof Item) {
			return itemRepository.findById(id).orElseThrow(() -> new StorageException("Item <" + id + "> does not exist!"));
		}
		else if (dataentity instanceof Primaryfile) {
			return primaryfileRepository.findById(id).orElseThrow(() -> new StorageException("Primaryfile <" + id + "> does not exist!"));
		}
		else {
			throw new IllegalArgumentException("Failed to find dataentity: the provided dataentity is of invalid type.");			
		}
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.findDuplicateDataentitiesByName(Dataentity)
	 */
	@Override
	public List<? extends Dataentity> findDuplicateDataentitiesByName(Dataentity dataentity) {
		if (dataentity == null) {
			throw new IllegalArgumentException("Failed to find dataentity: the provided dataentity is null.");
		}
		
		List<? extends Dataentity> desFound = new ArrayList<Dataentity>();
		String name = dataentity.getName();
		
		if (dataentity instanceof Unit) {
			desFound = unitRepository.findByName(name);
		}
		else if (dataentity instanceof Collection) {
			Unit unit = ((Collection)dataentity).getUnit();
			if (unit == null) return desFound;	
			desFound = collectionRepository.findByUnitIdAndName(unit.getId(), name);
		}
		else if (dataentity instanceof Item) {
			Collection collection = ((Item)dataentity).getCollection();
			if (collection == null) return desFound;		
			desFound = itemRepository.findByCollectionIdAndName(collection.getId(), name);
		}
		else if (dataentity instanceof Primaryfile) {
			Item item = ((Primaryfile)dataentity).getItem();			
			if (item == null) return desFound;		
			desFound = primaryfileRepository.findByItemIdAndName(item.getId(), name);
		}
		else if (dataentity instanceof UnitSupplement) {
			Unit unit = ((UnitSupplement)dataentity).getUnit();
			if (unit == null) return desFound;		
			desFound = unitSupplementRepository.findByUnitIdAndName(unit.getId(), name);
		}
		else if (dataentity instanceof CollectionSupplement) {
			Collection collection = ((CollectionSupplement)dataentity).getCollection();
			if (collection == null) return desFound;		
			desFound = collectionSupplementRepository.findByCollectionIdAndName(collection.getId(), name);
		}
		else if (dataentity instanceof ItemSupplement) {
			Item item = ((ItemSupplement)dataentity).getItem();
			if (item == null) return desFound;		
			desFound = itemSupplementRepository.findByItemIdAndName(item.getId(), name);
		}
		else if (dataentity instanceof PrimaryfileSupplement) {
			Primaryfile primaryfile = ((PrimaryfileSupplement)dataentity).getPrimaryfile();
			if (primaryfile == null) return desFound;		
			desFound = primaryfileSupplementRepository.findByPrimaryfileIdAndName(primaryfile.getId(), name);
		}
		else {
			// dataentity must be one of the above types applicable for UniqueName validation
			throw new IllegalArgumentException("Failed to find dataentity: the provided dataentity " + dataentity.getId() + " is of invalid type.");
		}
		
		return desFound;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.findDuplicateDataentitiesByExternalSrcAndId(Dataentity)
	 */
	@Override
	public List<? extends Dataentity> findDuplicateDataentitiesByExternalSrcAndId(Dataentity dataentity) {
		if (dataentity == null) {
			throw new IllegalArgumentException("Failed to find dataentity: the provided dataentity is null.");
		}

		List<? extends Dataentity> desFound = new ArrayList<Dataentity>();

		if (dataentity instanceof Item) {
			Collection collection = ((Item)dataentity).getCollection();
			if (collection == null) return desFound;
			String externalSource = ((Item)dataentity).getExternalSource();
			String externalId = ((Item)dataentity).getExternalId();
			desFound = itemRepository.findByCollectionIdAndExternalSourceAndExternalId(collection.getId(),externalSource, externalId);
		}
		else {
			throw new IllegalArgumentException("Failed to find dataentity: the provided dataentity " + dataentity.getId() + " is of invalid type.");
		}

		return desFound;
	}
		
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.findAsset(Long, SupplementType)
	 */
	@Override
	public Asset findAsset(Long id, SupplementType type) {
		switch (type) {
		case PFILE:
			return primaryfileRepository.findById(id).orElseThrow(() -> new StorageException("Primaryfile <" + id + "> does not exist!"));        	
		case PRIMARYFILE:
			return primaryfileSupplementRepository.findById(id).orElseThrow(() -> new StorageException("PrimaryfileSupplement <" + id + "> does not exist!"));        	
		case ITEM:
			return itemSupplementRepository.findById(id).orElseThrow(() -> new StorageException("ItemSupplement <" + id + "> does not exist!"));        	
		case COLLECTION:
			return collectionSupplementRepository.findById(id).orElseThrow(() -> new StorageException("CollectionSupplement <" + id + "> does not exist!"));        	
		case UNIT:
			return unitSupplementRepository.findById(id).orElseThrow(() -> new StorageException("UnitSupplement <" + id + "> does not exist!"));        	
		}
		return null;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.saveAsset(Asset)
	 */
	@Override
	public Asset saveAsset(Asset asset) {
		if (asset instanceof Primaryfile) {
			return primaryfileRepository.save((Primaryfile)asset);
		}
		else if (asset instanceof PrimaryfileSupplement) {
			return primaryfileSupplementRepository.save((PrimaryfileSupplement)asset);
		}
		else if (asset instanceof ItemSupplement) {
			return itemSupplementRepository.save((ItemSupplement)asset);
		}
		else if (asset instanceof CollectionSupplement) {
			return collectionSupplementRepository.save((CollectionSupplement)asset);
		}
		else if (asset instanceof UnitSupplement) {
			return unitSupplementRepository.save((UnitSupplement)asset);
		}
		return asset;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getSupplementType(Supplement)
	 */
	@Override
	public SupplementType getSupplementType(Supplement supplement) {
		if (supplement instanceof PrimaryfileSupplement) {
			return SupplementType.PRIMARYFILE;
		}
		else if (supplement instanceof ItemSupplement) {
			return SupplementType.ITEM;
		}
		else if (supplement instanceof CollectionSupplement) {
			return SupplementType.COLLECTION;
		}
		else if (supplement instanceof UnitSupplement) {
			return SupplementType.UNIT;
		}
		return null;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.changeSupplementParent(Supplement, Dataentity)
	 */
	@Override
	public Supplement changeSupplementParent(Supplement supplement, Dataentity parent) {		
		// if type doesn't change, just update the parent of the original supplement and return 
		if (getParentDataentity(supplement).getClass().equals(parent.getClass())) {
			setParentDataentity(supplement, parent);
			log.debug("Changed supplement " + supplement.getId() + " with new parent " + parent.getId() + " of the same type.");  
			return supplement;
		}
		
		// otherwise create a supplement of the new type
		Supplement newsup = null;
		if (parent instanceof Unit) {
			newsup = new UnitSupplement();
		}
		else if (parent instanceof Collection) {
			newsup = new CollectionSupplement();
		}
		else if (parent instanceof Item) {
			newsup = new ItemSupplement();
		}
		else if (parent instanceof Primaryfile) {
			newsup = new PrimaryfileSupplement();
		}
		else {
			throw new RuntimeException("Failed to change supplement " + supplement.getId() + " with new parent " + parent.getId() + ": invalid parent type.");
		}
		
		// and duplicate the contents of the supplement under the new parent
		newsup.copy(supplement);
		setParentDataentity(newsup, parent);
		
		log.debug("Changed supplement " + supplement.getId() + " with new parent " + parent.getId() + " of a different type.");  
		return newsup;
	}	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.moveSupplement(Supplement, Dataentity)
	 */
	@Override
	public Supplement moveSupplement(Long supplementId, SupplementType supplementType, Long parentId, String parentType) {
    	// retrieve supplement from DB
    	Supplement supplement = (Supplement)findAsset(supplementId, supplementType);
		
    	// if parentType not provided, then it defaults to the same as the current type 
    	SupplementType newType = StringUtils.isEmpty(parentType) ? supplementType : Supplement.getSupplementType(parentType);
		if (newType == null) {
			new RuntimeException("Invalid parent type provided: " + parentType);
		}

    	// retrieve new parent from DB
		Dataentity parent =	findParentDataEntity(parentId, newType);

    	// if parent doesn't change, no action
    	if (supplementType == newType && getParentDataentity(supplement).getId().equals(parent.getId())) {
    		log.warn("No need to move supplement " + supplementId + " to the same parent " + parentId);
    		return supplement;
    	}    	
    	
    	// otherwise, change the supplement's parent to the new parent
    	Supplement newsup = changeSupplementParent(supplement, parent);

    	// and move unitSupplement media/info files to new subdir
        fileStorageService.moveAsset(newsup, true);

    	log.info("Successfully moved " + supplementType + " supplement " + supplementId + " to new parent " + parentType + " " + parentId);
        return newsup;
	}
		
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getSupplementsForPrimaryfile(Primaryfile, String, String, String)
	 */
	@Override
	public List<Supplement> getSupplementsForPrimaryfile(Primaryfile primaryfile, String name, String category, String format) {
		List<Supplement> supplements = new ArrayList<Supplement>();
		
		// primaryfile must exist, category and format must not be blank
		if (primaryfile == null || StringUtils.isBlank(category) || StringUtils.isBlank(format)) {
			return supplements;
		}
					
		// find all supplements associated with the primaryfile at all parent levels by its category/format and optionally name,
		// along with associated parent's ID, assuming the original filenames have extensions that can be matched against format 
		if (StringUtils.isBlank(name)) {
			supplements.addAll(unitSupplementRepository.findByUnitIdAndCategoryAndOriginalFilenameEndsWithIgnoreCase(primaryfile.getItem().getCollection().getUnit().getId(), category, format));
			supplements.addAll(collectionSupplementRepository.findByCollectionIdAndCategoryAndOriginalFilenameEndsWithIgnoreCase(primaryfile.getItem().getCollection().getId(), category, format));
			supplements.addAll(itemSupplementRepository.findByItemIdAndCategoryAndOriginalFilenameEndsWithIgnoreCase(primaryfile.getItem().getId(), category, format));
			supplements.addAll(primaryfileSupplementRepository.findByPrimaryfileIdAndCategoryAndOriginalFilenameEndsWithIgnoreCase(primaryfile.getId(), category, format));
		}		
		else {
			supplements.addAll(unitSupplementRepository.findByUnitIdAndNameAndCategoryAndOriginalFilenameEndsWithIgnoreCase(primaryfile.getItem().getCollection().getUnit().getId(), name, category, format));
			supplements.addAll(collectionSupplementRepository.findByCollectionIdAndNameAndCategoryAndOriginalFilenameEndsWithIgnoreCase(primaryfile.getItem().getCollection().getId(), name, category, format));
			supplements.addAll(itemSupplementRepository.findByItemIdAndNameAndCategoryAndOriginalFilenameEndsWithIgnoreCase(primaryfile.getItem().getId(), name, category, format));
			supplements.addAll(primaryfileSupplementRepository.findByPrimaryfileIdAndNameAndCategoryAndOriginalFilenameEndsWithIgnoreCase(primaryfile.getId(), name, category, format));
		}		
		
		// set absolute path for each supplement, to be used for Supplement MGM path parameter
		for (Supplement supplement : supplements) {
			mediaService.setAssetAbsoluatePath(supplement);
		}
		
		log.info("Successfully retrieved " + supplements.size() + " supplements for primaryfile " + primaryfile.getId());
		return supplements;	
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getSupplementsForPrimaryfiles(Long[], String, String, String)
	 */
	@Override
	public List<List<Supplement>> getSupplementsForPrimaryfiles(Long[] primaryfileIds, String name, String category, String format) {
		List<List<Supplement>> supplementss = new ArrayList<List<Supplement>>();
		
		for (Long primaryfileId : primaryfileIds) {
			Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElse(null);
			List<Supplement> supplements = getSupplementsForPrimaryfile(primaryfile, name, category, format);
			supplementss.add(supplements);
		}
		
		log.info("Successfully retrieved supplements for primaryfiles " + primaryfileIds + ", name: " + name + ", category: " + category + ", format: " + format);
		return supplementss;
	}

}
