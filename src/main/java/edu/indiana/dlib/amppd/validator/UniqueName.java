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
@Constraint(validatedBy = UniqueNameValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueName {  
	String message() default "dataentity name must be unique within its parent's scope";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}

