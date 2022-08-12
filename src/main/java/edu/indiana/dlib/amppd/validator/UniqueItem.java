package edu.indiana.dlib.amppd.validator;

import java.lang.annotation.*;
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
@Constraint(validatedBy = UniqueItemValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueItem {
    String message() default "Item name must be unique within its parent's scope";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}