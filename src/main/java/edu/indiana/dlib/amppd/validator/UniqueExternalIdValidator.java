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
public class UniqueExternalIdValidator implements ConstraintValidator<UniqueExternalId, Dataentity> {

    @Autowired
    private DataentityService dataentityService;

    @Override
    public void initialize(UniqueExternalId uwp) {
    }

    @Override
    public boolean isValid(Dataentity dataentity, ConstraintValidatorContext context) {
        List<? extends Dataentity> desFound = null;
        try {
            if(((Item) dataentity).getExternalId() != null && ((Item) dataentity).getExternalSource() != null) desFound = dataentityService.findDuplicateDataentitiesByExternalSrcAndId(dataentity);
            else desFound = dataentityService.findDuplicateDataentities(dataentity);
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

        if(desFound.size() >= 1 && ((Item) dataentity).getExternalId() != null && ((Item) dataentity).getExternalSource() != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("External Source and ID already exists in parent collection!")
                .addConstraintViolation();
        }
//
        // otherwise, it's invalid
        return false;
    }

}

