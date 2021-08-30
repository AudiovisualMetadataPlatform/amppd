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
	public boolean isValid(Dataentity de,  ConstraintValidatorContext cxt) {
		List<? extends Dataentity> desFound = null;
		
		if (de instanceof Unit) {
			desFound = unitRepository.findByName(de.getName());
		}
		else if (de instanceof Collection) {
			desFound = collectionRepository.findByUnitIdAndName(((Collection)de).getUnit().getId(), de.getName());
		}
		else if (de instanceof Item) {
			desFound = itemRepository.findByCollectionIdAndName(((Item)de).getCollection().getId(), de.getName());
		}
		else if (de instanceof Primaryfile) {
			desFound = primaryfileRepository.findByItemIdAndName(((Primaryfile)de).getItem().getId(), de.getName());
		}
		else if (de instanceof CollectionSupplement) {
			desFound = collectionSupplementRepository.findByCollectionIdAndName(((CollectionSupplement)de).getCollection().getId(), de.getName());
		}
		else if (de instanceof ItemSupplement) {
			desFound = itemSupplementRepository.findByItemIdAndName(((ItemSupplement)de).getItem().getId(), de.getName());
		}
		else if (de instanceof PrimaryfileSupplement) {
			desFound = primaryfileSupplementRepository.findByPrimaryfileIdAndName(((PrimaryfileSupplement)de).getPrimaryfile().getId(), de.getName());
		}
		
		// de must be one of the Dataentity type
		if (desFound == null) return false;
		
		// de is valid if no existing dataentity has the same name
		if (desFound != null && desFound.size() == 0) return true;
		
		// otherwise, if one existing dataentity has the same name, it must be itself
		if (desFound.size() == 1 && de.getId() == desFound.get(0).getId()) return true;
		
		// otherwise, it's invalid
		return false;
	}

}
