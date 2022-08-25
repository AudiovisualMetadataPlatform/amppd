package edu.indiana.dlib.amppd.validator;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.nimbusds.oauth2.sdk.util.StringUtils;

import edu.indiana.dlib.amppd.service.ConfigService;
import edu.indiana.dlib.amppd.service.impl.ConfigServiceImpl;

/**
 * Validator for field value that must be one of the enumerated ones defined in its corresponding configuration property.
 * @author yingfeng
 */
public class EnumConfigValidator implements ConstraintValidator<EnumConfig, String> {  

	@Autowired
	private ConfigService configService;

	private String property;

	@Override
	public void initialize(EnumConfig uwp) {
		property = uwp.property();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext cxt) {
		// validation for supplement category
		if (ConfigServiceImpl.SUPPLEMENT_CATEGORIES.equals(property)) {
			// if supplementCategories property is not configured, then no constraint on supplement category
			List<String> supplementCategories = configService.getSupplementCategories();
			return supplementCategories == null || supplementCategories.isEmpty() || supplementCategories.contains(value);
		}
		// validation for externalSource
		if (ConfigServiceImpl.EXTERNAL_SOURCES.equals(property)) {
			// externalSource can be blank
			if (StringUtils.isBlank(value)) return true;
			// if externalSources property is not configured, then no constraint on externalSource
			List<String> externalSources = configService.getExternalSources();
			return externalSources == null || externalSources.isEmpty() || externalSources.contains(value);
		}
		// validation for taskManager
		else if (ConfigServiceImpl.TASK_MANAGERS.equals(property)) {
			// if taskManagers property is not configured, then no constraint on taskManager
			List<String> taskManagers = configService.getTaskManagers();
			return taskManagers == null || taskManagers.isEmpty() || taskManagers.contains(value);
		}

		// otherwise, it's invalid
		return false;
	}

}
