package edu.indiana.dlib.amppd.validator;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.service.DataentityService;


/**
 * Validator for uniqueness of the name field within its parent's scope for all Dataentities.
 * @author yingfeng
 */
public class UniqueNameValidator implements ConstraintValidator<UniqueName, Dataentity> {  
	
	@Autowired
	private DataentityService dataentityService;
	
	@Override
	public void initialize(UniqueName uwp) {
	}

	@Override
	public boolean isValid(Dataentity dataentity, ConstraintValidatorContext cxt) {
		List<? extends Dataentity> desFound = null;

		try {
			desFound = dataentityService.findDuplicateDataentities(dataentity);
		}
		catch (IllegalArgumentException e) {
			throw new RuntimeException("Exception while validating UniqueName for dataentity " + dataentity.getId(), e);
		}
		
		// dataentity is valid if no existing dataentity has the same name
		if (desFound.size() == 0) return true;
		
		// otherwise, if one existing dataentity has the same name, it must be itself
		if (desFound.size() == 1 && dataentity.getId() == desFound.get(0).getId()) return true;
		
		// otherwise, it's invalid
		return false;
	}

}
