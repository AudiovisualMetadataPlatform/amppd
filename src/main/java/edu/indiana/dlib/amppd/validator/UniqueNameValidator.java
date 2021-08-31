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
	public boolean isValid(Dataentity dataentity,  ConstraintValidatorContext cxt) {
		List<? extends Dataentity> desFound = null;
		
		if (dataentity instanceof Unit) {
			desFound = unitRepository.findByName(dataentity.getName());
		}
		else if (dataentity instanceof Collection) {
			desFound = collectionRepository.findByUnitIdAndName(((Collection)dataentity).getUnit().getId(), dataentity.getName());
		}
		else if (dataentity instanceof Item) {
			desFound = itemRepository.findByCollectionIdAndName(((Item)dataentity).getCollection().getId(), dataentity.getName());
		}
		else if (dataentity instanceof Primaryfile) {
			desFound = primaryfileRepository.findByItemIdAndName(((Primaryfile)dataentity).getItem().getId(), dataentity.getName());
		}
		else if (dataentity instanceof CollectionSupplement) {
			desFound = collectionSupplementRepository.findByCollectionIdAndName(((CollectionSupplement)dataentity).getCollection().getId(), dataentity.getName());
		}
		else if (dataentity instanceof ItemSupplement) {
			desFound = itemSupplementRepository.findByItemIdAndName(((ItemSupplement)dataentity).getItem().getId(), dataentity.getName());
		}
		else if (dataentity instanceof PrimaryfileSupplement) {
			desFound = primaryfileSupplementRepository.findByPrimaryfileIdAndName(((PrimaryfileSupplement)dataentity).getPrimaryfile().getId(), dataentity.getName());
		}
		
		// dataentity must be one of the Dataentity type
		if (desFound == null) return false;
		
		// dataentity is valid if no existing dataentity has the same name
		if (desFound != null && desFound.size() == 0) return true;
		
		// otherwise, if one existing dataentity has the same name, it must be itself
		if (desFound.size() == 1 && dataentity.getId() == desFound.get(0).getId()) return true;
		
		// otherwise, it's invalid
		return false;
	}

}
