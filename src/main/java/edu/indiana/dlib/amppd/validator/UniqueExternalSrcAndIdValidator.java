package edu.indiana.dlib.amppd.validator;

import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.service.DataentityService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

/**
 * Validator for uniqueness of the name, external id and source fields within its parent's scope for Item.
 * @author rimshakhalid
 */
public class UniqueExternalSrcAndIdValidator implements ConstraintValidator<UniqueExternalSrcAndId, Dataentity> {

    @Autowired
    private DataentityService dataentityService;

    @Override
    public void initialize(UniqueExternalSrcAndId uwp) {
    }

    @Override
    public boolean isValid(Dataentity dataentity, ConstraintValidatorContext context) {
        List<? extends Dataentity> desFound = null;
        if (dataentity instanceof Item) {
            try {
                if(((Item) dataentity).getExternalId() == null && ((Item) dataentity).getExternalSource() == null) return true;
                if(((Item) dataentity).getExternalId() == null && ((Item) dataentity).getExternalSource() != null) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("External ID is required with External Source.")
                            .addConstraintViolation();
                    return false;
                } else if(((Item) dataentity).getExternalId() != null && ((Item) dataentity).getExternalSource() == null) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("External Source is required with External ID.")
                            .addConstraintViolation();
                    return false;
                } else if(((Item) dataentity).getExternalId() != null && ((Item) dataentity).getExternalSource() != null) desFound = dataentityService.findDuplicateDataentitiesByExternalSrcAndId(dataentity);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Exception while validating UniqueName for dataentity " + dataentity.getId(), e);
            }
            // if none is found, it's valid
            if (desFound.size() == 0) return true;

            // otherwise, if only itself is found (during update), it's also valid
            if (desFound.size() >= 1 && dataentity.getId() != null) {
                for (Dataentity de : desFound) {
                    if(dataentity.getId().equals(de.getId())) return true;
                }
            }
            return false;
        }
        return true;
    }

}

