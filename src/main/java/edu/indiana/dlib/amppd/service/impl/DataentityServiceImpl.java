package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.DataentityService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of DataentityService.
 * @author yingfeng
 */
@Service
@Slf4j
public class DataentityServiceImpl implements DataentityService {
	
	public static String EXTERNAL_SOURCES = "externalSources";
	public static String TASK_MANAGERS = "taskManagers";

	@Autowired
	private AmppdPropertyConfig amppdPropertyConfig;
	
	@Value("#{'${amppd.externalSources}'.split(',')}")
	private List<String> externalSources;
	
	@Value("#{'${amppd.taskManagers}'.split(',')}")
	private List<String> taskManagers;
	
	@Autowired
	private UnitRepository unitRepository;

	@Autowired
	private CollectionRepository collectionRepository;
	
	@Autowired
	private ItemRepository itemRepository;
	
	@Autowired
	private PrimaryfileRepository primaryfileRepository;

	@Autowired
	private PrimaryfileSupplementRepository primaryfileSupplementRepository;

	@Autowired
	private ItemSupplementRepository itemSupplementRepository;

	@Autowired
	private CollectionSupplementRepository collectionSupplementRepository;
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getExternalSources()
	 */
	public List<String> getExternalSources() {
		return externalSources;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getTaskManagers()
	 */
	public List<String> getTaskManagers() {
		return taskManagers;
	}
	
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
		
		// only handle non-supplement types
		if (dataentity instanceof Unit) {
			return unitRepository.findById(id).orElse(null);
		}
		else if (dataentity instanceof Collection) {
			return collectionRepository.findById(id).orElse(null);
		}
		else if (dataentity instanceof Item) {
			return itemRepository.findById(id).orElse(null);
		}
		else if (dataentity instanceof Primaryfile) {
			return primaryfileRepository.findById(id).orElse(null);
		}
		
		// ignore supplement types
		return null;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.findDuplicateDataentities(Dataentity)
	 */
	@Override
	public List<? extends Dataentity> findDuplicateDataentities(Dataentity dataentity) {
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
			throw new RuntimeException("Failed to find dataentity: the provided dataentity " + dataentity.getId() + " is of invalid type.");
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
			return null;        	
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
		return asset;
	}
	
	
}
