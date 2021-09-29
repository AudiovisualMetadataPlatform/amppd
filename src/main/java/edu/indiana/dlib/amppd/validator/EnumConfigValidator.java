package edu.indiana.dlib.amppd.validator;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.nimbusds.oauth2.sdk.util.StringUtils;

import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.service.impl.DataentityServiceImpl;

/**
 * Validator for field value that must be one of the enumerated ones defined in its corresponding configuration property.
 * @author yingfeng
 */
public class EnumConfigValidator implements ConstraintValidator<EnumConfig, String> {  

	@Autowired
	private DataentityService dataentityService;

	private String property;

	@Override
	public void initialize(EnumConfig uwp) {
		property = uwp.property();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext cxt) {
		// validation for externalSource
		if (DataentityServiceImpl.EXTERNAL_SOURCES.equals(property)) {
			// externalSource can be blank
			if (StringUtils.isBlank(value)) return true;
			// if externalSources property is not configured, then no constraint on externalSource
			List<String> externalSources = dataentityService.getExternalSources();
			return externalSources == null || externalSources.isEmpty() || externalSources.contains(value);
		}
		// validation for taskManager
		else if (DataentityServiceImpl.TASK_MANAGERS.equals(property)) {
			// if taskManagers property is not configured, then no constraint on taskManager
			List<String> taskManagers = dataentityService.getTaskManagers();
			return taskManagers == null || taskManagers.isEmpty() || taskManagers.contains(value);
		}

		// otherwise, it's invalid
		return false;
	}

}
