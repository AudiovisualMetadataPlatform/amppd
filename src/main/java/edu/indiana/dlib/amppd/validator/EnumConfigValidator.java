package edu.indiana.dlib.amppd.validator;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Content;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.service.impl.DataentityServiceImpl;

/**
 * Validator for uniqueness of the name field within its parent's scope for all Dataentities.
 * @author yingfeng
 */
public class EnumConfigValidator implements ConstraintValidator<EnumConfig, Dataentity> {  

	@Autowired
	private DataentityService dataentityService;

	private String property;

	@Override
	public void initialize(EnumConfig uwp) {
		property = uwp.property();
	}

	@Override
	public boolean isValid(Dataentity dataentity, ConstraintValidatorContext cxt) {
		// validation for externalSource
		if (DataentityServiceImpl.EXTERNAL_SOURCES.equals(property)) {
			// dataentity must be of Content type for EnumConfig validation on externalSource
			try {
				String externalSource = ((Content)dataentity).getExternalSource();
				List<String> externalSources = dataentityService.getExternalSources();
				// if externalSource values enum is not configured, then no constraint on the property
				return externalSources == null || externalSources.isEmpty() || externalSources.contains(externalSource);
			}
			catch(Exception e) {
				throw new RuntimeException("Exception during EnumConfig validation on externalSource: dataentity " + dataentity.getId() + " is not a Content", e);
			}		
		}
		// validation for taskManager
		else if (DataentityServiceImpl.TASK_MANAGERS.equals(property)) {
			// dataentity must be of Collection type for EnumConfig validation on taskManager
			try {
				String taskManager = ((Collection)dataentity).getTaskManager();
				List<String> taskManagers = dataentityService.getTaskManagers();
				return taskManagers == null || taskManagers.isEmpty() || taskManagers.contains(taskManager);
			}
			catch(Exception e) {
				throw new RuntimeException("Exception while validating EnumConfig on taskManager: dataentity " + dataentity.getId() + ": is not a Collection.", e);
			}		
		}

		// otherwise, it's invalid
		return false;
	}

}
