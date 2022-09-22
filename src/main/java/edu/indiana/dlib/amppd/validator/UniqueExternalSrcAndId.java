package edu.indiana.dlib.amppd.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Constraint for uniqueness of the item within its parent's scope.
 * @author rimshakhalid
 */
@Documented
@Constraint(validatedBy = UniqueExternalSrcAndIdValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueExternalSrcAndId {
    String message() default "External Source and ID must be unique within its parent collection!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}