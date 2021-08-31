package edu.indiana.dlib.amppd.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.validation.Validator;


/* Note: 
 * It appears that this class is not needed if we call @Valid directly in BeforeCreateEventHandler (which is the case now)
 * either because the bug is fixed or the wiring is somewhat different than hooking the validator with beforeCreate event.
 */

/**
 * This is a workaround for a bug in Spring Data REST, which prevents beforeCreate event detection and validators 
 * being called. The solution is to insert all events into Spring Data REST ValidatingRepositoryEventListener class.
 * @author yingfeng
 */
@Configuration
public class ValidatorEventRegister implements InitializingBean {

	@Autowired
	ValidatingRepositoryEventListener validatingRepositoryEventListener;

	@Autowired
	private Map<String, Validator> validators;

	@Override
	public void afterPropertiesSet() throws Exception {
		List<String> events = Arrays.asList("beforeCreate");
		for (Map.Entry<String, Validator> entry : validators.entrySet()) {
			events.stream()
			.filter(p -> entry.getKey().startsWith(p))
			.findFirst()
			.ifPresent(
					p -> validatingRepositoryEventListener
					.addValidator(p, entry.getValue()));
		}
	}
	
}