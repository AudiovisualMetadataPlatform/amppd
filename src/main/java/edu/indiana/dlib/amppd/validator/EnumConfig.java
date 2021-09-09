package edu.indiana.dlib.amppd.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Constraint for uniqueness of the name field within its parent's scope for all Dataentities.
 * @author yingfeng
 */
@Documented
@Constraint(validatedBy = EnumConfigValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumConfig {
	String property();
	String message() default "must be one of the enumerated values defined in configuration property";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
