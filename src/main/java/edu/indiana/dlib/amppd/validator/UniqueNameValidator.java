package edu.indiana.dlib.amppd.validator;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;


/**
 * Validator for uniqueness of the name field within its parent's scope for all Dataentities.
 * @author yingfeng
 */
public class UniqueNameValidator implements ConstraintValidator<UniqueName, Dataentity> {  
	
	@Autowired
	private UnitRepository unitRepository;

	@Autowired
	private CollectionRepository collectionRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private PrimaryfileRepository primaryfileRepository;
		
	@Autowired
    private CollectionSupplementRepository collectionSupplementRepository;

	@Autowired
    private ItemSupplementRepository itemSupplementRepository;

	@Autowired
    private PrimaryfileSupplementRepository primaryfileSupplementRepository;

	
	@Override
	public void initialize(UniqueName uwp) {
	}

	@Override
	public boolean isValid(Dataentity dataentity, ConstraintValidatorContext cxt) {
		List<? extends Dataentity> desFound = null;
		
		// bypass validation (return true) if dataentity's parent is null
		if (dataentity == null) {
			// dataentity must not be null
			throw new RuntimeException("Exception while validating UniqueName for dataentity: it's null.");
		}
		else if (dataentity instanceof Unit) {
			desFound = unitRepository.findByName(dataentity.getName());
		}
		else if (dataentity instanceof Collection) {
			Unit unit = ((Collection)dataentity).getUnit();
			if (unit == null) return true;	
			desFound = collectionRepository.findByUnitIdAndName(unit.getId(), dataentity.getName());
		}
		else if (dataentity instanceof Item) {
			Collection collection = ((Item)dataentity).getCollection();
			if (collection == null) return true;		
			desFound = itemRepository.findByCollectionIdAndName(collection.getId(), dataentity.getName());
		}
		else if (dataentity instanceof Primaryfile) {
			Item item = ((Primaryfile)dataentity).getItem();			
			if (item == null) return true;		
			desFound = primaryfileRepository.findByItemIdAndName(item.getId(), dataentity.getName());
		}
		else if (dataentity instanceof CollectionSupplement) {
			Collection collection = ((CollectionSupplement)dataentity).getCollection();
			if (collection == null) return true;		
			desFound = collectionSupplementRepository.findByCollectionIdAndName(collection.getId(), dataentity.getName());
		}
		else if (dataentity instanceof ItemSupplement) {
			Item item = ((ItemSupplement)dataentity).getItem();
			if (item == null) return true;		
			desFound = itemSupplementRepository.findByItemIdAndName(item.getId(), dataentity.getName());
		}
		else if (dataentity instanceof PrimaryfileSupplement) {
			Primaryfile primaryfile = ((PrimaryfileSupplement)dataentity).getPrimaryfile();
			if (primaryfile == null) return true;		
			desFound = primaryfileSupplementRepository.findByPrimaryfileIdAndName(primaryfile.getId(), dataentity.getName());
		}
		else {
			// dataentity must be one of the above types applicable for UniqueName validation
			throw new RuntimeException("Exception while validating UniqueName for dataentity " + dataentity.getId() + ": it's of invalid type.");
		}
		
		// dataentity is valid if no existing dataentity has the same name
		if (desFound != null && desFound.size() == 0) return true;
		
		// otherwise, if one existing dataentity has the same name, it must be itself
		if (desFound.size() == 1 && dataentity.getId() == desFound.get(0).getId()) return true;
		
		// otherwise, it's invalid
		return false;
	}

}
